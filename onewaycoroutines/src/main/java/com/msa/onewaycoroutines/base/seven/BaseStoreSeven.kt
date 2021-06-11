package com.msa.onewaycoroutines.base.seven

import android.util.Log
import com.msa.core.Action
import com.msa.core.SkipReducer
import com.msa.core.State
import com.msa.core.name
import com.msa.onewaycoroutines.base.Store
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
    reduce: Reducer<S>
) = launch {

    var state = initialState
    var count = 1

    while (isActive) {

        select<Unit> {

            inputActions.onReceive { action ->

                /*measureTimeMillis {
                    Log.d(TAG_STORE, "onReceive action = ${action.name()}")
                    state = reduce(action, state)
                    setStates.emit(state)
                }.let { timeTakenToComputeNewState ->
                    //To make sure we are not doing any heavy work in reducer
                    if (timeTakenToComputeNewState > 8) {
                        //Log.w(TAG_STORE, "$count - Took: ${timeTakenToComputeNewState}ms for ${action.name()}  |  $state")
                        //throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                    } else {
                        //Log.d(TAG_STORE, "$count - Took: ${timeTakenToComputeNewState}ms for ${action.name()}  |  $state")
                    }

                    if (action is CounterAction.ResetAction) {
                        count = 1
                    } else {
                        count++
                    }
                }*/

                Log.d(TAG_STORE, "onReceive action = ${action.name()}")
                val newState = reduce(action, state)
                state = newState
                setStates.emit(state)
            }

            requestStates.onReceive {
                Log.d(TAG_STORE, "onReceive Request State = $state")
                sendStates.send(state)
                //it.complete(state)
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

    private val requestStatesChannel: Channel<Unit> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)
    private val sendStatesChannel: Channel<S> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)

    private val inputActions: MutableSharedFlow<Action> = MutableSharedFlow(
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

    init {

        scope.stateMachine(
            initialState = initialState,
            inputActions = inputActionsChannel,
            requestStates = requestStatesChannel,
            sendStates = sendStatesChannel,
            setStates = setStates,
            reduce = reduce
        )
    }

    override fun dispatch(action: Action) {

        if (action !is SkipReducer && !actionsToSkipReduce.contains(action::class)) {
            Log.d("DISPATCH", "${action.name()} | ${action is SkipReducer}")
            inputActionsChannel.trySend(action)
        } else {
            Log.w("DISPATCH", "${action.name()} | ${action is SkipReducer}")
        }
        inputActions.tryEmit(action)
    }

    override fun state(): S = setStates.value

    override suspend fun getState(): S {
        requestStatesChannel.send(Unit)
        return sendStatesChannel.receive()
    }

    override fun terminate() {
        scope.cancel()
    }
}