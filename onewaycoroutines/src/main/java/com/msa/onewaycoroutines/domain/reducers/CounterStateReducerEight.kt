package com.msa.onewaycoroutines.domain.reducers

import android.util.Log
import com.msa.core.Action
import com.msa.core.Reducer
import com.msa.core.name
import com.msa.onewaycoroutines.base.TAG_REDUCER
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.reducerForAction
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

object CounterStateReducerEight : Reducer<CounterState> {

    private val TAG: String = javaClass.simpleName

    /**
     * To use with [combineReducers].
     */
    val r21 = reducerForAction<CounterAction.IncrementAction, CounterState> { action, state ->
        Log.d(TAG_REDUCER, "r21 ${action.name()} = $state")
        state.copy(counter = state.counter + 1)
    }

    val r22 = reducerForAction<CounterAction.DecrementAction, CounterState> { action, state ->
        Log.d(TAG_REDUCER, "r22 ${action.name()} = $state")
        state.copy(counter = state.counter - 1)
    }

    val r23 = reducerForAction<CounterAction.ResetAction, CounterState> { action, state ->
        Log.d(TAG_REDUCER, "r23 ${action.name()} = $state")
        state.copy(counter = 0)
    }

    /**
     * Not recommended to use with combineReducers. Use [reducerForAction].
     * Since this lead to iterating of all reducers.
     */
    val r1: Reduce<CounterState> = { action, state ->
        Log.d(TAG_REDUCER, "r1 ${action.name()} = $state")
        when (action) {

            is CounterAction.IncrementAction -> state.copy(counter = state.counter + 1)
            is CounterAction.DecrementAction -> state.copy(counter = state.counter - 1)

            else -> state
        }
    }

    val r3: Reduce<CounterState> = { action, state ->
        Log.d(TAG_REDUCER, "r3 ${action.name()} = $state")
        when (action) {

            is CounterAction.ResetAction -> state.copy(counter = 0)

            else -> state
        }
    }

    fun getReducers() = arrayOf(r21, r22, r23)

    override fun reduce(action: Action, state: CounterState): CounterState {
        Log.d("CSRE", "reduce action = ${action.name()} | $state | ${Thread.currentThread()}")
        return when (action) {

            is CounterAction.IncrementAction -> state.copy(counter = state.counter + 1)

            is CounterAction.DecrementAction -> {
                /* for (i in 0..10000) {
                     Log.d(TAG, "i = $i")
                 }*/
                state.copy(counter = state.counter - 1)
            }

            is CounterAction.ForceUpdateAction -> state.copy(counter = action.count)

            is CounterAction.ResetAction -> state.copy(counter = 0)

            else -> state
        }
    }
}