package com.msa.onewaycoroutines.domain.stores

import android.util.Log
import com.msa.core.Action
import com.msa.onewaycoroutines.base.one.BaseStoreOne
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.*

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterStoreOne(
    job: Job,
    initialState: CounterState,
    scope: CoroutineScope,
    logger: (String, String) -> Unit = { _, _ -> Unit }
) : BaseStoreOne<CounterState>(initialState, scope) {

    init {

        scope.launch {
            while (isActive) {
                delay(1000)
                Log.i(TAG, "children count = ${job.children.count()}")
            }
        }
    }

    override fun reduce(action: Action, currentState: CounterState): CounterState {
        return when (action) {

            is CounterAction.IncrementAction -> currentState.copy(counter = currentState.counter + 1)

            is CounterAction.DecrementAction -> currentState.copy(counter = currentState.counter - 1)

            is CounterAction.ForceUpdateAction -> currentState.copy(counter = action.count)

            else -> currentState
        }
    }
}