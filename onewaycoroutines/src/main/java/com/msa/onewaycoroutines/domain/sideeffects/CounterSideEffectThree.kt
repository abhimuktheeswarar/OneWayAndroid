package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.base.three.BaseSideEffectThree
import com.msa.onewaycoroutines.base.three.BaseStoreThree
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.*

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterSideEffectThree(
    store: BaseStoreThree<*>,
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcherProvider
) : BaseSideEffectThree(store, scope, dispatchers) {

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
            delay(5000)
            dispatch(CounterAction.IncrementAction)
        }
    }
}