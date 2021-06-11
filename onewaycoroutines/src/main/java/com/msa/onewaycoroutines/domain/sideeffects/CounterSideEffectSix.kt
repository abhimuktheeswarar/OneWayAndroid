package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.name
import com.msa.onewaycoroutines.base.six.BaseSideEffectSix
import com.msa.onewaycoroutines.base.six.BaseStoreSix
import com.msa.onewaycoroutines.common.ShowToastAction
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

class CounterSideEffectSix(
    store: BaseStoreSix<*>,
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcherProvider
) : BaseSideEffectSix(store, scope, dispatchers) {

    override fun handle(action: Action) {
        Log.d(TAG, "handle = ${action.name()} |  ${Thread.currentThread()}")
        when (action) {
            is CounterAction.DecrementAction -> {
                //delay(5000)
                //dispatch(CounterAction.IncrementAction)
                //cancelExistingJob(action)
                /*val beforeState = state<CounterState>()
                dispatch(CounterAction.ForceUpdateAction(beforeState.counter * 10))
                val currentState = state<CounterState>()
                Log.d(
                    TAG,
                    "beforeState = ${beforeState.counter} vs currentState = ${currentState.counter}"
                )*/
            }

            is ShowToastAction -> {
                scope.launch {
                    measureTimeMillis {
                        Log.d(
                            TAG,
                            "Current state = ${getState<CounterState>()}"
                        )
                    }.let {
                        Log.d(TAG, "Took ${it}ms to getState")
                    }
                }
            }
        }
    }
}