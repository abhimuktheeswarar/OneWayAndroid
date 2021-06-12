package com.msa.onewaycoroutines.base.seven

import android.util.Log
import com.msa.core.*
import com.msa.onewaycoroutines.base.ExceededTimeLimitToComputeNewStatException
import com.msa.onewaycoroutines.base.Store
import com.msa.onewaycoroutines.base.TAG_STORE
import com.msa.onewaycoroutines.utilities.MutableStateChecker
import com.msa.onewaycoroutines.utilities.assertStateValues
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
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

private fun <S : State> CoroutineScope.stateMachine(
    initialState: S,
    inputActions: ReceiveChannel<Action>,
    requestStates: ReceiveChannel<Unit>,
    sendStates: SendChannel<S>,
    setStates: MutableStateFlow<S>,
    relayActions: MutableSharedFlow<Action>,
    reduce: Reduce<S>,
    config: StoreConfig
) = launch {

    var state = initialState

    while (isActive) {

        select<Unit> {

            inputActions.onReceive { action ->

                measureTimeMillis {
                    Log.d(TAG_STORE, "onReceive action = ${action.name()}")
                    state = reduce(action, state)
                    setStates.emit(state)
                }.let { timeTakenToComputeNewState ->
                    if (timeTakenToComputeNewState > config.reducerTimeLimitInMilliSeconds) {
                        val exception =
                            ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                        if (config.debugMode) {
                            exception.printStackTrace()
                        } else {
                            relayActions.tryEmit(StoreReducerExceededTimeLimitAction(exception))
                        }
                    }
                }
            }

            requestStates.onReceive {
                Log.d(TAG_STORE, "onReceive Request State = $state")
                sendStates.send(state)
            }
        }
    }
}

typealias Reduce<S> = (action: Action, state: S) -> S

class BaseStoreSeven<S : State>(
    initialState: S,
    private val reduce: Reduce<S>,
    val config: StoreConfig
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

    private val mutableStateChecker =
        if (config.debugMode) MutableStateChecker(initialState) else null

    init {

        config.scope.stateMachine(
            initialState = initialState,
            inputActions = inputActionsChannel,
            requestStates = requestStatesChannel,
            sendStates = sendStatesChannel,
            setStates = setStates,
            relayActions = inputActions,
            reduce = reduce,
            config = config
        )

        mutableStateChecker?.let { states.onEach(it::onStateChanged).launchIn(config.scope) }
    }

    override fun dispatch(action: Action) {
        if (action !is SkipReducer) {
            if (config.debugMode) {
                assertStateValues(action, state(), reduce)
            }
            inputActionsChannel.trySend(action)
        }
        inputActions.tryEmit(action)
    }

    override fun state(): S = setStates.value

    override suspend fun awaitState(): S {
        requestStatesChannel.send(Unit)
        return sendStatesChannel.receive()
    }

    override fun terminate() {
        config.scope.cancel()
    }
}

data class StoreConfig(
    val scope: CoroutineScope,
    val debugMode: Boolean,
    val reducerTimeLimitInMilliSeconds: Long = 8L
)

data class StoreReducerExceededTimeLimitAction(override val exception: ExceededTimeLimitToComputeNewStatException) :
    ErrorAction
