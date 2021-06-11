package com.msa.onewaycoroutines.base.five

import android.util.Log
import com.msa.core.Action
import com.msa.core.SkipReducer
import com.msa.core.State
import com.msa.core.name
import com.msa.onewaycoroutines.base.ExceededTimeLimitToComputeNewStatException
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 09-June-2021.
 */

internal class GetStateAction(
    val deferred: CompletableDeferred<State>
) : Action

private fun <S : State> CoroutineScope.store(
    initialState: S,
    inputActions: Flow<Action>,
    setStates: MutableStateFlow<S>,
    reduce: (action: Action, state: S) -> S
) = launch {

    var state = initialState

    var count = 1

    inputActions
        .collect { action ->
            if (action !is GetStateAction) {
                measureTimeMillis {
                    val updatedState = reduce(action, state)
                    state = updatedState
                    setStates.emit(state)
                }.let { timeTakenToComputeNewState ->
                    //To make sure we are not doing any heavy work in reducer
                    if (timeTakenToComputeNewState > 8) {
                        //Log.w("Store", "$count Took ${timeTakenToComputeNewState}ms for $action")
                        throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
                    } else {
                        Log.d(
                            "Store",
                            "$count Took ${timeTakenToComputeNewState}ms for ${action.name()}"
                        )
                    }

                    if (action is CounterAction.ResetAction) {
                        count = 1
                    } else count++
                }

            } else {
                action.deferred.complete(state)
                Log.i("Store", "$count Took for ${action.name()}")
                count++
            }
        }
}

class BaseStoreFive<S : State>(
    initialState: S,
    reducer: (action: Action, state: S) -> S,
    private val scope: CoroutineScope
) {

    private val mutableInputActions: MutableSharedFlow<Action> = MutableSharedFlow(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private val mutableStates: MutableStateFlow<S> = MutableStateFlow(initialState).apply {
        buffer(capacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)
    }

    val states: Flow<S> = mutableStates
    val relayActions: Flow<Action> = mutableInputActions

    init {
        scope.store(
            initialState = initialState,
            inputActions = mutableInputActions.filterNot { it is SkipReducer },
            setStates = mutableStates,
            reduce = reducer
        )
    }

    fun dispatch(action: Action) {
        mutableInputActions.tryEmit(action)
    }

    fun state(): S = mutableStates.value

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> getState(): S {
        val deferred = CompletableDeferred<State>()
        mutableInputActions.emit(GetStateAction(deferred))
        return deferred.await() as S
    }

    fun cancel() {
        scope.cancel()
    }
}