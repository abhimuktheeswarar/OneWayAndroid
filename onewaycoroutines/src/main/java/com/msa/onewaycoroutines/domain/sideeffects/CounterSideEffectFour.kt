package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.base.four.BaseSideEffectFour
import com.msa.onewaycoroutines.base.four.BaseStoreFour
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.*

/**
 * Created by Abhi Muktheeswarar on 08-June-2021.
 */

class CounterSideEffectFour(
    store: BaseStoreFour<*>,
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcherProvider
) : BaseSideEffectFour(store, scope, dispatchers) {

    init {

        scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(4000)
                val i = state<CounterState>().counter + 10
                Log.d(TAG, "emitting value = $i")
                dispatch(CounterAction.ForceUpdateAction(i))
            }
        }
    }

    override suspend fun handle(action: Action) {

        if (action is CounterAction.DecrementAction) {
            //delay(5000)
            //dispatch(CounterAction.IncrementAction)
            val beforeState = state<CounterState>()
            dispatch(CounterAction.ForceUpdateAction(beforeState.counter * 10))
            val currentState = state<CounterState>()
            Log.d(
                TAG,
                "beforeState = ${beforeState.counter} vs currentState = ${currentState.counter}"
            )

        }
    }
}