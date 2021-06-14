package com.msa.onewaycoroutines.base.eight

import com.msa.core.Action
import com.msa.core.State
import com.msa.core.name
import com.msa.onewaycoroutines.base.ExceededTimeLimitToComputeNewStatException
import com.msa.onewaycoroutines.common.Middleware
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.common.StoreReducerExceededTimeLimitAction
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
 * Created by Abhi Muktheeswarar on 12-June-2021.
 */

private fun <S : State> CoroutineScope.stateMachine(
    initialState: S,
    inputActions: ReceiveChannel<Action>,
    requestStates: ReceiveChannel<Unit>,
    sendStates: SendChannel<S>,
    setStates: MutableStateFlow<S>,
    coldActions: MutableSharedFlow<Action>,
    reduce: Reduce<S>,
    config: StoreConfig,
) = launch {

    var state = initialState

    while (isActive) {

        select<Unit> {

            inputActions.onReceive { action ->

                measureTimeMillis {
                    //Log.d(TAG_STORE, "onReceive action = ${action.name()}")
                    state = reduce(action, state)
                    setStates.emit(state)
                    coldActions.tryEmit(action)

                }.let { timeTakenToComputeNewState ->
                    //Log.i(TAG_STORE, "Took ${timeTakenToComputeNewState}ms for ${action.name()} | $state")
                    if (timeTakenToComputeNewState > config.reducerTimeLimitInMilliSeconds) {
                        val exception =
                            ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                        if (config.debugMode) {
                            exception.printStackTrace()
                        } else {
                            coldActions.tryEmit(StoreReducerExceededTimeLimitAction(exception))
                        }
                    }
                }
            }

            requestStates.onReceive {
                //Log.d(TAG_STORE, "onReceive Request State = $state")
                sendStates.send(state)
            }
        }
    }
}


class BaseStoreEight<S : State>(
    val initialState: S,
    private val reduce: Reduce<S>,
    middlewares: List<Middleware<S>>?,
    val config: StoreConfig,
) {

    private val inputActionsChannel: Channel<Action> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)

    private val requestStatesChannel: Channel<Unit> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)
    private val sendStatesChannel: Channel<S> =
        Channel(capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.SUSPEND)

    private val mutableHotActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private val mutableColdActions: MutableSharedFlow<Action> = MutableSharedFlow(
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
    val hotActions: Flow<Action> = mutableHotActions
    val coldActions: Flow<Action> = mutableColdActions

    private val middlewares =
        middlewares?.foldRight({ action: Action -> this.dispatcher(action) }) { middleware, dispatcher ->
            middleware(::dispatch, ::state)(dispatcher)
        }

    private val mutableStateChecker =
        if (config.debugMode && config.mutableStateChecker) MutableStateChecker(initialState) else null

    init {

        config.scope.stateMachine(
            initialState = initialState,
            inputActions = inputActionsChannel,
            requestStates = requestStatesChannel,
            sendStates = sendStatesChannel,
            setStates = setStates,
            coldActions = mutableColdActions,
            reduce = reduce,
            config = config
        )

        mutableStateChecker?.let { states.onEach(it::onStateChanged).launchIn(config.scope) }
    }

    private fun dispatcher(action: Action) {
        if (config.debugMode && config.assertStateValues) {
            assertStateValues(action, state(), reduce)
        }
        inputActionsChannel.trySend(action)
    }

    fun dispatch(action: Action) {
        mutableHotActions.tryEmit(action)
        middlewares?.invoke(action) ?: dispatcher(action)
    }

    fun state(): S = setStates.value

    suspend fun awaitState(): S {
        requestStatesChannel.send(Unit)
        return sendStatesChannel.receive()
    }

    fun terminate() {
        config.scope.cancel()
    }
}
