package com.msa.onewaycoroutines.ui.viewmodels

import android.util.Log
import com.msa.core.Action
import com.msa.core.SideEffect
import com.msa.core.name
import com.msa.onewaycoroutines.base.TAG_REDUCER
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
        actions.onEach(::handle).launchIn(scope)
    }

    override fun handle(action: Action) {
        //Log.d(TAG, "handle = ${action.name()}")

        when (action) {

            is CounterAction.IncrementAction -> {
                scope.launch {
                    //val currentCount = getState().counter
                    //dispatch(CounterAction.ForceUpdateAction(currentCount - 1))
                    //Log.d(TAG, "counter = ${state()} | ${awaitState()}")
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

                scope.launch {
                    val s = state()
                    val gS = awaitState()
                    Log.d(
                        TAG,
                        "state = $s vs getState = $gS"
                    )
                }
            }
        }
    }

    override fun reduce(action: Action, state: CounterState): CounterState = with(state) {
        Log.d(TAG_REDUCER, "${action.name()} | $state | ${Thread.currentThread()}")
        when (action) {

            is CounterAction.IncrementAction -> copy(counter = state.counter + 1)

            is CounterAction.DecrementAction -> copy(counter = counter - 1)

            is CounterAction.ForceUpdateAction -> copy(counter = action.count)

            is CounterAction.ResetAction -> copy(counter = 0, updateOn = System.currentTimeMillis())

            else -> state
        }
    }
}