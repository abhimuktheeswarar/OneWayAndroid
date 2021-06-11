package com.msa.onewaycoroutines.ui.viewmodels

import android.util.Log
import com.msa.core.Action
import com.msa.core.Reducer
import com.msa.core.SideEffect
import com.msa.core.name
import com.msa.onewaycoroutines.base.six.BaseViewModelSix
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

class CounterViewModelSix :
    BaseViewModelSix<CounterState>(initialState = CounterState()),
    Reducer<CounterState>, SideEffect {

    init {
        setupReducer(::reduce)
        relayActions.onEach(::handle).launchIn(scope)
    }

    override fun reduce(action: Action, state: CounterState): CounterState {
        Log.d("Reducer", "reduce action = ${action.name()} | $state")
        return when (action) {

            is CounterAction.IncrementAction -> state.copy(counter = state.counter + 1)

            is CounterAction.DecrementAction -> state.copy(counter = state.counter - 1)

            is CounterAction.ForceUpdateAction -> state.copy(counter = action.count)

            is CounterAction.ResetAction -> state.copy(counter = 0)

            else -> state
        }
    }

    override fun handle(action: Action) {
        Log.d(TAG, "handle = ${action.name()}")

        when (action) {

            is CounterAction.IncrementAction -> {
                scope.launch {
                    val currentCount = getState().counter
                    dispatch(CounterAction.ForceUpdateAction(currentCount - 1))
                }
            }

            is CounterAction.DecrementAction -> {
                /*scope.launch {
                    val beforeState = getState()
                    dispatch(CounterAction.ForceUpdateAction(beforeState.counter * 10))
                    val currentState = getState()
                    Log.d(
                        TAG,
                        "beforeState = ${beforeState.counter} vs currentState = ${currentState.counter}"
                    )
                }*/
            }

            is CounterAction.ForceUpdateAction -> {

            }

            is CounterAction.ResetAction -> {

            }
        }
    }
}