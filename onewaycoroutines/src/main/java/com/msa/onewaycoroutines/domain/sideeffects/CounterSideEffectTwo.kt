package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.ControlledRunner
import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.base.two.BaseSideEffectTwo
import com.msa.onewaycoroutines.base.two.BaseStoreTwo
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterSideEffectTwo(
    store: BaseStoreTwo<*>,
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcherProvider
) : BaseSideEffectTwo(store, scope, dispatchers) {

    private val controlledRunner by lazy { ControlledRunner<Long>() }

    init {

        /* scope.launch(Dispatchers.IO) {
             while (isActive) {
                 delay(4000)
                 val i = state<CounterState>().counter + 10
                 Log.d(TAG, "emitting value = $i")
                 dispatch(CounterAction.ForceUpdateAction(i))
             }
         }*/
    }

    override suspend fun handle(action: Action) {

        when (action) {
            is CounterAction.DecrementAction -> {
                //delay(5000)
                //dispatch(CounterAction.IncrementAction)
                //cancelExistingJob(action)
                val beforeState = state<CounterState>()
                dispatch(CounterAction.ForceUpdateAction(beforeState.counter * 10))
                val currentState = state<CounterState>()
                Log.d(
                    TAG,
                    "beforeState = ${beforeState.counter} vs currentState = ${currentState.counter}"
                )
            }
            is CounterAction.ResetAction -> {
                /* delay(8000)
                 dispatch(CounterAction.ForceUpdateAction(10))
                 scope.launch {
                     val time = getCurrentTime()
                     Log.d(TAG, "time = $time")
                 }*/
            }
        }
    }

    private suspend fun getCurrentTime(): Long {
        return controlledRunner.cancelPreviousThenRun {
            delay(5000)
            System.currentTimeMillis()
        }
    }
}