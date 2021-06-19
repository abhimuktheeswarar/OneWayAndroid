package com.msa.onewaycoroutines.ui.viewmodels

import android.content.Context
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.SideEffect
import com.msa.onewaycoroutines.base.nine.BaseStoreNine
import com.msa.onewaycoroutines.base.nine.BaseViewModelNine
import com.msa.onewaycoroutines.common.combineReducers
import com.msa.onewaycoroutines.common.getDefaultStoreConfig
import com.msa.onewaycoroutines.common.skipMiddleware
import com.msa.onewaycoroutines.domain.middlewares.EventMiddleware
import com.msa.onewaycoroutines.domain.reducers.CounterStateReducer
import com.msa.onewaycoroutines.domain.sideeffects.CounterSideEffectNine
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

class CounterViewModelNine(store: BaseStoreNine<CounterState>) :
    BaseViewModelNine<CounterState>(store = store), SideEffect {

    init {
        hotActions.onEach(::handle).launchIn(scope)
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

                /*scope.launch {
                    val s = state()
                    val gS = awaitState()
                    Log.d(
                        TAG,
                        "state = $s vs getState = $gS"
                    )
                }*/
            }
        }
    }

    companion object {

        fun get(context: Context): CounterViewModelNine {

            val initialState = CounterState()
            val config = getDefaultStoreConfig()
            //val reduce = CounterStateReducerEight::reduce
            val coroutineDispatcherProvider =
                CoroutineDispatcherProvider(config.scope.coroutineContext)

            val eventMiddleware =
                EventMiddleware(1, context, config.scope, coroutineDispatcherProvider).get()

            val middlewares = listOf(eventMiddleware, skipMiddleware)

            /*val combinedReducer = reducers.fold(initialState) { state: CounterState, reduce ->
                reduce(action, state)
            }*/

            val rootReducer = combineReducers(*CounterStateReducer.getReducers())

            val store = BaseStoreNine(
                initialState = initialState,
                config = config,
                reduce = rootReducer,
                middlewares = middlewares
            )

            CounterSideEffectNine(store, coroutineDispatcherProvider)

            return CounterViewModelNine(
                store = store
            )
        }
    }
}

