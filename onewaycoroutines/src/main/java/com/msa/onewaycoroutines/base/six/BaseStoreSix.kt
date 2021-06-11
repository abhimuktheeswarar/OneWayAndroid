package com.msa.onewaycoroutines.base.six

import android.util.Log
import com.msa.core.Action
import com.msa.core.Reducer
import com.msa.core.State
import com.msa.core.name
import com.msa.onewaycoroutines.base.ExceededTimeLimitToComputeNewStatException
import com.msa.onewaycoroutines.base.TAG_REDUCER
import com.msa.onewaycoroutines.base.TAG_STORE
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

private fun <S : State> CoroutineScope.stateMachine(
    initialState: S,
    inputActions: ReceiveChannel<Action>,
    computeStates: SendChannel<StateTransporter<S>>,
    newStates: ReceiveChannel<S>,
    requestStates: ReceiveChannel<Unit>,
    sendStates: SendChannel<S>,
    setStates: MutableStateFlow<S>
) = launch {

    var state = initialState

    while (isActive) {

        select<Unit> {

            inputActions.onReceive { action ->

                measureTimeMillis {
                    Log.d(TAG_STORE, "onReceive action = ${action.name()}")
                    computeStates.send(StateTransporter(action, state))
                    state = newStates.receive()
                    setStates.emit(state)
                }.let { timeTakenToComputeNewState ->
                    //To make sure we are not doing any heavy work in reducer
                    if (timeTakenToComputeNewState > 8) {
                        //Log.w(TAG_STORE, "Took ${timeTakenToComputeNewState}ms for ${action.name()}  |  $state")
                        throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                    } else {
                        //Log.d(TAG_STORE, "Took ${timeTakenToComputeNewState}ms for ${action.name()}  |  $state")
                    }
                }
            }

            requestStates.onReceive {
                Log.d(TAG_STORE, "onReceive Request State")
                sendStates.send(state)
            }
        }
    }
}

private fun <S : State> CoroutineScope.reducer(
    store: BaseStoreSix<S>,
    reduce: (action: Action, state: S) -> S
) = launch {

    var rAction: Action
    var iState: S

    while (isActive) {
        measureTimeMillis {
            val request = store.computeStatesChannel.receive()
            val (action, state) = request
            rAction = action
            val newState = reduce(action, state)
            iState = newState
            //Log.d("Reducer", "request action = ${action.name()} | old = $state | new = $newState")
            store.newStatesChannel.send(newState)
        }.let { timeTakenToComputeNewState ->
            //To make sure we are not doing any heavy work in reducer
            if (timeTakenToComputeNewState > 8) {
                Log.w(
                    TAG_REDUCER,
                    "Took ${timeTakenToComputeNewState}ms for ${rAction.name()}  |  $iState"
                )
                //throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
            } else {
                //Log.d("Reducer", "$count Took ${timeTakenToComputeNewState}ms for ${rAction.name()}  |  $iState")
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
    val newStatesChannel: Channel<S> = Channel()

    private val requestStatesChannel: Channel<Unit> =
        Channel()
    private val sendStatesChannel: Channel<S> =
        Channel()

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
            newStates = newStatesChannel,
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