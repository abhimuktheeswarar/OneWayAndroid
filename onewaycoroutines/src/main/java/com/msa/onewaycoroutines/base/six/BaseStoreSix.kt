package com.msa.onewaycoroutines.base.six

import android.util.Log
import com.msa.core.Action
import com.msa.core.Reducer
import com.msa.core.State
import com.msa.core.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 09-June-2021.
 */

data class StateTransporter<S : State>(
    val action: Action,
    val state: S
)

class ExceededTimeLimitToComputeNewStatException(override val message: String) : Exception()

const val TAG = "Store"

private fun <S : State> CoroutineScope.stateMachine(
    initialState: S,
    inputActions: ReceiveChannel<Action>,
    computeStates: Channel<StateTransporter<S>>,
    requestStates: ReceiveChannel<Unit>,
    sendStates: SendChannel<S>,
    setStates: MutableStateFlow<S>
) = launch {

    var state = initialState

    while (isActive) {

        select<Unit> {

            inputActions.onReceive { action ->
                Log.d(TAG, "onReceive action = ${action.name()}")
                computeStates.send(StateTransporter(action, state))
                state = computeStates.receive().state
                setStates.emit(state)
            }

            requestStates.onReceive {
                Log.d(TAG, "onReceive Request State")
                sendStates.send(state)
            }
        }
    }
}

private fun <S : State> CoroutineScope.reducer(
    store: BaseStoreSix<S>,
    reduce: (action: Action, state: S) -> S
) = launch {

    while (isActive) {
        val request = store.computeStatesChannel.receive()
        val (action, state) = request
        Log.d("Reducer", "request action = ${action.name()} | $state")
        measureTimeMillis {
            val newState = reduce(action, state)
            store.computeStatesChannel.send(request.copy(state = newState))
        }.let { timeTakenToComputeNewState ->
            //To make sure we are not doing any heavy work in reducer
            if (timeTakenToComputeNewState > 8) {
                Log.w("Reducer", "Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
            } else {
                Log.d("Reducer", "Took ${timeTakenToComputeNewState}ms for ${action.name()}")
            }
        }
    }
}

class BaseStoreSix<S : State>(
    initialState: S,
    reducer: Reducer<S>? = null,
    private val scope: CoroutineScope,
    private val actionsToSkipReduce: Set<KClass<out Action>>? = null
) {

    private val inputActions: Channel<Action> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)

    val computeStatesChannel: Channel<StateTransporter<S>> = Channel()

    private val requestStatesChannel: Channel<Unit> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)
    private val sendStatesChannel: Channel<S> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)

    private val mutableRelayActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val setStates: MutableStateFlow<S> = MutableStateFlow(initialState).apply {
        buffer(
            capacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    }

    val states: Flow<S> = setStates
    val relayActions: Flow<Action> = mutableRelayActions

    init {

        scope.stateMachine(
            initialState = initialState,
            inputActions = inputActions,
            computeStates = computeStatesChannel,
            setStates = setStates,
            requestStates = requestStatesChannel,
            sendStates = sendStatesChannel
        )

        reducer?.let { setupReducer(it::reduce) }
    }

    fun dispatch(action: Action) {
        if (actionsToSkipReduce.isNullOrEmpty() || !actionsToSkipReduce.contains(action::class)) {
            inputActions.trySend(action)
        }
        mutableRelayActions.tryEmit(action)
    }

    fun setupReducer(reduce: (action: Action, state: S) -> S) {
        scope.reducer(store = this, reduce = reduce)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = setStates.value as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> getState(): S {
        requestStatesChannel.send(Unit)
        return sendStatesChannel.receive() as S
    }

    fun cancel() {
        scope.cancel()
    }
}