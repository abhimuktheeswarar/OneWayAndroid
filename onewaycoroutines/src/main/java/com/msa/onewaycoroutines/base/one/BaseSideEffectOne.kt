package com.msa.onewaycoroutines.base.one

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

abstract class BaseSideEffectOne(
    private val store: BaseStoreOne<*>,
    protected val scope: CoroutineScope,
    protected val coroutineDispatcherProvider: CoroutineDispatcherProvider
) {
    protected val tag = this.javaClass.simpleName

    init {
        scope.launch {
            store.actions.collect {
                handle(it)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.states.value as S

    protected suspend fun dispatch(action: Action) {
        store.dispatch(action)
    }

    abstract suspend fun handle(action: Action)
}