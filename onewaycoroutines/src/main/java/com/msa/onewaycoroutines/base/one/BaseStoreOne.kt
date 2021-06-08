package com.msa.onewaycoroutines.base.one

import android.util.Log
import com.msa.core.Action
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

abstract class BaseStoreOne<S : State>(
    initialState: S,
    private val scope: CoroutineScope
) {

    protected val TAG: String = this.javaClass.simpleName

    private val mutableActions = MutableSharedFlow<Action>()

    private val mutableActionsSideEffect = MutableSharedFlow<Action>()
    val actions: SharedFlow<Action> = mutableActionsSideEffect

    private val mutableStates = MutableStateFlow(initialState)
    val states: StateFlow<S> = mutableStates

    init {

        scope.launch {
            mutableActions.buffer().collect { action ->
                Log.d(TAG, "collect = ${action.javaClass.simpleName}")
                val updatedState = withTimeoutOrNull(10) { reduce(action, mutableStates.value) }
                if (updatedState != null) {
                    mutableStates.value = updatedState
                } else {
                    println("Taking too much to time compute new state!")
                }
                mutableActionsSideEffect.emit(action)
            }
        }
    }

    suspend fun dispatch(action: Action) {
        val b = mutableActions.emit(action)
        Log.d(TAG, "dispatch = ${action.javaClass.simpleName} | ${b}")
    }

    protected abstract fun reduce(action: Action, currentState: S): S

    fun clear() {
        scope.cancel()
    }
}
