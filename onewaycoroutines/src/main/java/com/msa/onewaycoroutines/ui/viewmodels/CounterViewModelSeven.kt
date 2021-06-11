package com.msa.onewaycoroutines.ui.viewmodels

import android.util.Log
import com.msa.core.Action
import com.msa.core.SideEffect
import com.msa.onewaycoroutines.base.seven.BaseViewModelSeven
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

class CounterViewModelSeven : BaseViewModelSeven<CounterState>(CounterState()), SideEffect {

    init {
        relayActions.onEach(::handle).launchIn(scope)
    }

    override fun handle(action: Action) {
        //Log.d(TAG, "handle = ${action.name()}")

        when (action) {

            is CounterAction.IncrementAction -> {
                scope.launch {
                    //val currentCount = getState().counter
                    //dispatch(CounterAction.ForceUpdateAction(currentCount - 1))
                    Log.d(TAG, "counter = ${state()} | ${getState()}")
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

    override fun reduce(action: Action, state: CounterState): CounterState {
        return when (action) {

            is CounterAction.IncrementAction -> state.copy(counter = state.counter + 1)

            is CounterAction.DecrementAction -> state.copy(counter = state.counter - 1)

            is CounterAction.ForceUpdateAction -> state.copy(counter = action.count)

            is CounterAction.ResetAction -> state.copy(counter = 0)

            else -> state
        }
    }
}