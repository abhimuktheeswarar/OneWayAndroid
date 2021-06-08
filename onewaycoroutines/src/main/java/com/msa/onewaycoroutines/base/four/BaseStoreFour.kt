package com.msa.onewaycoroutines.base.four

import android.util.Log
import com.msa.core.Action
import com.msa.core.State
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.selects.select

/**
 * Created by Abhi Muktheeswarar on 08-June-2021.
 */


private fun <S : State> CoroutineScope.storeFour(
    initialState: S,
    inputActions: Channel<Action>,
    getStates: Channel<CompletableDeferred<S>>,
    states: MutableStateFlow<S>,
    relayActions: MutableSharedFlow<Action>,
    reduce: suspend (action: Action, state: S) -> S
) = launch {

    val TAG = "StoreFour"

    var state = initialState

    select<Unit> {

        inputActions.onReceive { action ->
            Log.d(TAG, "action = ${action.javaClass.simpleName} | $state")
            val updatedState = withTimeoutOrNull(1000) { reduce(action, state) }
            if (updatedState != null && updatedState != state) {
                state = updatedState
                states.emit(state)
            } else {
                Log.w(TAG, "Taking too much to time compute new state!")
            }

            relayActions.emit(action)
        }

        getStates.onReceive { deferred ->
            Log.d(TAG, "getState = $state")
            deferred.complete(state)
        }
    }
}

abstract class BaseStoreFour<S : State>(
    initialState: S,
    private val scope: CoroutineScope
) {

    private val inputActions = Channel<Action>(capacity = Channel.UNLIMITED)
    private val getStates = Channel<CompletableDeferred<S>>(capacity = Channel.UNLIMITED)

    private val mutableStates: MutableStateFlow<S> = MutableStateFlow(initialState).apply {
        buffer(capacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    }
    val states: Flow<S> = mutableStates

    private val relayActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    val actions: Flow<Action> = relayActions

    init {

        scope.storeFour(
            initialState = initialState,
            inputActions = inputActions,
            getStates = getStates,
            states = mutableStates,
            relayActions = relayActions
        ) { action, state -> reduce(action, state) }
    }

    suspend fun dispatch(action: Action) {
        inputActions.send(action)
    }

    fun state(): S = mutableStates.value

    suspend fun currentState(): S {
        val deferred = CompletableDeferred<S>()
        getStates.send(deferred)
        return deferred.await()
    }

    protected abstract suspend fun reduce(action: Action, currentState: S): S

    fun cancel() {
        scope.cancel()
    }
}