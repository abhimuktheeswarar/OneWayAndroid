package com.msa.onewaycoroutines.base.nine

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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 16-June-2021.
 */

class BaseStoreNine<S : State>(
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

    private val restoreStateChannel: Channel<S> = Channel()

    private val mutableHotActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private val mutableColdActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private val mutableStateSharedFlow = MutableSharedFlow<S>(
        replay = 1,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ).apply { tryEmit(initialState) }

    private val middlewares =
        middlewares?.foldRight({ action: Action -> this.dispatcher(action) }) { middleware, dispatcher ->
            middleware(::dispatch, ::state)(dispatcher)
        }

    private val mutableStateChecker =
        if (config.debugMode && config.mutableStateChecker) MutableStateChecker(initialState) else null

    val hotActions: Flow<Action> = mutableHotActions
    val coldActions: Flow<Action> = mutableColdActions

    val states: Flow<S> = mutableStateSharedFlow.asSharedFlow()

    @Volatile
    var state = initialState
        private set

    init {
        startStartMachine()
    }

    private fun startStartMachine() {
        if (config.synchronous) return
        config.scope.launch {
            while (isActive) {
                triggerStateMachine()
            }
        }
    }

    private suspend fun triggerStateMachine() {

        select<Unit> {

            restoreStateChannel.onReceive { state ->
                this@BaseStoreNine.state = state
                mutableStateSharedFlow.emit(this@BaseStoreNine.state)
            }

            inputActionsChannel.onReceive { action ->

                measureTimeMillis {
                    //Log.d(TAG_STORE, "onReceive action = ${action.name()}")

                    val newState = reduce(action, state)
                    if (newState != state) {
                        state = newState
                        mutableStateSharedFlow.emit(newState)
                        mutableColdActions.emit(action)
                    }

                }.let { timeTakenToComputeNewState ->
                    //Log.i(TAG_STORE, "Took ${timeTakenToComputeNewState}ms for ${action.name()} | $state")
                    if (timeTakenToComputeNewState > config.reducerTimeLimitInMilliSeconds) {
                        val exception =
                            ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                        if (config.debugMode) {
                            exception.printStackTrace()
                        } else {
                            mutableColdActions.tryEmit(StoreReducerExceededTimeLimitAction(exception))
                        }
                    }
                }
            }

            requestStatesChannel.onReceive {
                //Log.d(TAG_STORE, "onReceive Request State = $state")
                sendStatesChannel.send(state)
            }
        }
    }

    private fun runStartMachineInBlockingMode() {
        if (config.scope.isActive) {
            runBlocking { triggerStateMachine() }
        }
    }

    private fun dispatcher(action: Action) {
        if (config.debugMode && config.assertStateValues) {
            assertStateValues(action, state, reduce, mutableStateChecker)
        }
        inputActionsChannel.trySend(action)
        if (config.synchronous) {
            runStartMachineInBlockingMode()
        }
    }

    fun dispatch(action: Action) {
        mutableHotActions.tryEmit(action)
        middlewares?.invoke(action) ?: dispatcher(action)
    }

    fun restoreState(state: S) {
        config.scope.launch { restoreStateChannel.send(state) }
    }

    suspend fun awaitState(): S {
        requestStatesChannel.send(Unit)
        val state = sendStatesChannel.receive()
        if (config.synchronous) {
            runStartMachineInBlockingMode()
        }
        return state
    }

    fun terminate() {
        config.scope.cancel()
    }
}

data class RestoreStateAction<S : State>(val state: S) : Action