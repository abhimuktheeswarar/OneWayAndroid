package com.msa.onewaycoroutines.base.three

import com.msa.core.Action
import com.msa.core.State
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

internal class StateAction<S : State>(
    val action: Action,
    val deferred: CompletableDeferred<State>
) : Action

@ObsoleteCoroutinesApi
private fun <S : State> CoroutineScope.actorStore(
    initialState: S,
    stateFlow: MutableStateFlow<S>,
    actionFlow: MutableSharedFlow<Action>,
    reduce: suspend (action: Action, state: S) -> S
) = actor<Action> {

    var state = initialState

    select<Unit> {

        channel.onReceive {

        }
    }

    for (action in channel) {

        val updatedState = withTimeoutOrNull(10) { reduce(action, state) }
        if (updatedState != null) {
            state = updatedState
            stateFlow.emit(state)
            actionFlow.emit(action)
        } else {
            println("Taking too much to time compute new state!")
        }
    }
}

@ObsoleteCoroutinesApi
abstract class BaseStoreThree<S : State>(
    initialState: S,
    private val scope: CoroutineScope
) {

    private val mutableStates: MutableStateFlow<S> = MutableStateFlow(initialState)
    val states: StateFlow<S> = mutableStates

    private val mutableActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val actions: SharedFlow<Action> = mutableActions

    private val actor =
        scope.actorStore(initialState, mutableStates, mutableActions) { action, state ->
            reduce(
                action,
                state
            )
        }

    suspend fun dispatch(action: Action) {
        actor.send(action)
    }

    protected abstract suspend fun reduce(action: Action, currentState: S): S

    fun cancel() {
        scope.cancel()
    }
}