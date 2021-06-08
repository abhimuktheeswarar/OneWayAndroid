package com.msa.onewaycoroutines.base.three

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

abstract class BaseSideEffectThree(
    private val store: BaseStoreThree<*>,
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcherProvider
) {

    protected val TAG: String = this.javaClass.simpleName

    init {
        scope.launch {
            store.actions.collect { handle(it) }
        }
    }

    suspend fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.states.value as S

    abstract suspend fun handle(action: Action)
}