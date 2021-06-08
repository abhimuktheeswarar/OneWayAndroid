package com.msa.onewaycoroutines.domain.stores

import com.msa.core.Action
import com.msa.onewaycoroutines.base.three.BaseStoreThree
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterStoreThree(
    initialState: CounterState,
    scope: CoroutineScope
) : BaseStoreThree<CounterState>(initialState, scope) {

    override suspend fun reduce(action: Action, currentState: CounterState): CounterState {
        return when (action) {

            is CounterAction.IncrementAction -> currentState.copy(counter = currentState.counter + 1)

            is CounterAction.DecrementAction -> currentState.copy(counter = currentState.counter - 1)

            is CounterAction.ForceUpdateAction -> currentState.copy(counter = action.count)

            else -> currentState
        }
    }
}