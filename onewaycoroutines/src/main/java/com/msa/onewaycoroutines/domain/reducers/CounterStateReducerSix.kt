package com.msa.onewaycoroutines.domain.reducers

import android.util.Log
import com.msa.core.Action
import com.msa.core.Reducer
import com.msa.core.name
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

class CounterStateReducerSix : Reducer<CounterState> {

    private val TAG: String = javaClass.simpleName

    override fun reduce(action: Action, state: CounterState): CounterState {
        Log.d(TAG, "reduce action = ${action.name()} | $state | ${Thread.currentThread()}")
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