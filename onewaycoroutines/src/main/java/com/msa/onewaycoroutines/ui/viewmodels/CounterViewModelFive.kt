package com.msa.onewaycoroutines.ui.viewmodels

import android.util.Log
import com.msa.core.Action
import com.msa.core.Reducer
import com.msa.core.SideEffect
import com.msa.core.name
import com.msa.onewaycoroutines.base.five.BaseViewModelFive
import com.msa.onewaycoroutines.domain.reducers.CounterStateReducerFive
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 09-June-2021.
 */

class CounterViewModelFive(reducer: Reducer<CounterState> = CounterStateReducerFive) :
    BaseViewModelFive<CounterState>(
        CounterState(),
        reducer::reduce,
        CoroutineScope(SupervisorJob())
    ),
    SideEffect, Reducer<CounterState> {

    init {
        relayActions.onEach(::handle).launchIn(scope)
    }


    override fun handle(action: Action) {
        Log.d(TAG, "handle = ${action.name()}")

        when (action) {

            is CounterAction.IncrementAction -> {
                /*//dispatch(CounterAction.ForceUpdateAction(beforeState.counter * 10))
                scope.launch {
                    Log.d(
                        TAG,
                        "currentState 0 = ${state()}"
                    )
                    Log.d(
                        TAG,
                        "currentState 1 = ${getState()} "
                    )
                }*/

                scope.launch {
                    val currentCount = getState().counter
                    dispatch(CounterAction.ForceUpdateAction(currentCount - 1))
                }
            }

            is CounterAction.DecrementAction -> {
                /*val beforeState = getState()
                dispatch(CounterAction.ForceUpdateAction(beforeState.counter * 10))
                val currentState = getState()
                Log.d(TAG, "beforeState = ${beforeState.counter} vs currentState = ${currentState.counter}")*/
            }

            is CounterAction.ForceUpdateAction -> {

            }

            is CounterAction.ResetAction -> {

            }
        }
    }

    override fun reduce(action: Action, state: CounterState): CounterState {
        TODO()
    }
}