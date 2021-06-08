package com.msa.onewaycoroutines.domain.sideeffects

import android.util.Log
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.base.one.BaseSideEffectOne
import com.msa.onewaycoroutines.base.one.BaseStoreOne
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.*

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterSideEffectOne(
    store: BaseStoreOne<*>,
    scope: CoroutineScope,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : BaseSideEffectOne(store, scope, coroutineDispatcherProvider) {

    init {

        scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(4000)
                val i = state<CounterState>().counter + 10
                Log.d(tag, "emitting value = $i")
                dispatch(CounterAction.ForceUpdateAction(i))
            }
        }
    }

    override suspend fun handle(action: Action) {
        Log.d(tag, "handle = ${action.javaClass.simpleName}")

        if (action is CounterAction.DecrementAction) {
            delay(5000)
            dispatch(CounterAction.IncrementAction)
        }
    }
}