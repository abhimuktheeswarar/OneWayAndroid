package com.msa.onewaycoroutines.base.two

import android.util.Log
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

abstract class BaseSideEffectTwo(
    private val store: BaseStoreTwo<*>,
    protected val scope: CoroutineScope,
    protected val dispatchers: CoroutineDispatcherProvider
) {
    protected val TAG: String = this.javaClass.simpleName

    init {
        scope.launch {
            store.relayActions.collect {
                Log.i(
                    TAG,
                    "collect relay actions: ${it.javaClass.simpleName} | ${state<CounterState>().counter}"
                )
                handle(it)
            }
        }
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> state(): S = store.currentState() as S

    abstract suspend fun handle(action: Action)
}

