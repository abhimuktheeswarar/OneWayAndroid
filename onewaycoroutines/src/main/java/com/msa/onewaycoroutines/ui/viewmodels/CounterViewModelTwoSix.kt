package com.msa.onewaycoroutines.ui.viewmodels

import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.base.six.BaseStoreSix
import com.msa.onewaycoroutines.base.six.BaseViewModelSix
import com.msa.onewaycoroutines.common.ShowToastAction
import com.msa.onewaycoroutines.domain.reducers.CounterStateReducerSix
import com.msa.onewaycoroutines.domain.sideeffects.CounterSideEffectSix
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

class CounterViewModelTwoSix(
    store: BaseStoreSix<CounterState>
) :
    BaseViewModelSix<CounterState>(initialState = CounterState(), store = store) {

    companion object {

        fun get(): CounterViewModelTwoSix {
            val scope =
                CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
                    throwable.printStackTrace()
                })

            val reducer = CounterStateReducerSix()
            val actionsToSkipReducer = setOf(ShowToastAction::class)
            val store = BaseStoreSix(
                initialState = CounterState(),
                scope = scope,
                reducer = reducer,
                actionsToSkipReduce = actionsToSkipReducer
            )

            CounterSideEffectSix(store, scope, CoroutineDispatcherProvider(scope.coroutineContext))

            return CounterViewModelTwoSix(store)
        }
    }
}