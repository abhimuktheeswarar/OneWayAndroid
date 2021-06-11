package com.msa.onewaycoroutines.base.seven

import android.util.Log
import com.msa.core.Action
import com.msa.core.SkipReducer
import com.msa.core.State
import com.msa.core.name
import com.msa.onewaycoroutines.base.Store
import com.msa.onewaycoroutines.base.TAG_STORE
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

typealias Reducer<S> = (action: Action, state: S) -> S

private fun <S : State> CoroutineScope.stateMachine(
    initialState: S,
    inputActions: ReceiveChannel<Action>,
    requestStates: ReceiveChannel<Unit>,
    sendStates: SendChannel<S>,
    setStates: MutableStateFlow<S>,
    relayActions: MutableSharedFlow<Action>,
    reduce: Reducer<S>
) = launch {

    var state = initialState
    var count = 1

    while (isActive) {

        select<Unit> {

            inputActions.onReceive { action ->

                measureTimeMillis {
                    Log.d(TAG_STORE, "onReceive action = ${action.name()}")
                    state = reduce(action, state)
                    setStates.emit(state)
                    relayActions.emit(action)
                }.let { timeTakenToComputeNewState ->
                    //To make sure we are not doing any heavy work in reducer
                    if (timeTakenToComputeNewState > 8) {
                        Log.w(
                            TAG_STORE,
                            "$count - Took: ${timeTakenToComputeNewState}ms for ${action.name()}  |  $state"
                        )
                        //throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                    } else {
                        Log.d(
                            TAG_STORE,
                            "$count - Took: ${timeTakenToComputeNewState}ms for ${action.name()}  |  $state"
                        )
                    }

                    if (action is CounterAction.ResetAction) {
                        count = 1
                    } else {
                        count++
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


class BaseStoreSeven<S : State>(
    initialState: S,
    reduce: (action: Action, state: S) -> S,
    private val scope: CoroutineScope,
    private val actionsToSkipReduce: Set<KClass<out Action>> = setOf(SkipReducer::class)
) : Store<S> {

    private val inputActionsChannel: Channel<Action> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)

    private val requestStatesChannel: Channel<Unit> = Channel()
    private val sendStatesChannel: Channel<S> = Channel()

    private val inputActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

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

    override val states: Flow<S> = setStates
    override val actions: Flow<Action> = inputActions
    override val relayActions: Flow<Action> = mutableRelayActions

    init {

        scope.stateMachine(
            initialState = initialState,
            inputActions = inputActionsChannel,
            requestStates = requestStatesChannel,
            sendStates = sendStatesChannel,
            setStates = setStates,
            relayActions = mutableRelayActions,
            reduce = reduce
        )

        actions
            .onEach {
                if (!actionsToSkipReduce.contains(it::class)) {
                    inputActionsChannel.send(it)
                } else {
                    mutableRelayActions.emit(it)
                }
            }
            .launchIn(scope)
    }

    override fun dispatch(action: Action) {
        inputActions.tryEmit(action)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : State> state(): S = setStates.value as S

    @Suppress("UNCHECKED_CAST")
    override suspend fun <S : State> getState(): S {
        requestStatesChannel.send(Unit)
        return sendStatesChannel.receive() as S
    }

    override fun terminate() {
        scope.cancel()
    }
}