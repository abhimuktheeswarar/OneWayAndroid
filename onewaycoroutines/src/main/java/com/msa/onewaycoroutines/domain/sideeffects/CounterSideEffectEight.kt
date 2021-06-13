package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.name
import com.msa.onewaycoroutines.base.eight.BaseSideEffectHotColdEight
import com.msa.onewaycoroutines.base.eight.BaseStoreEight
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

class CounterSideEffectEight(
    store: BaseStoreEight<*>,
    dispatchers: CoroutineDispatcherProvider,
) : BaseSideEffectHotColdEight(store, dispatchers) {

    override fun handleHot(action: Action) {
        scope.launch {
            Log.d(TAG,
                "handle HOT : ${action.name()} | ${state<CounterState>()} | ${awaitState<CounterState>()}")
        }
    }

    override fun handleCold(action: Action) {
        Log.d(TAG, "handle COLD : ${action.name()} | ${state<CounterState>()}")
    }
}