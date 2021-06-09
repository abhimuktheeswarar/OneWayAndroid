package com.msa.onewaycoroutines.base.two

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        store.relayActions.onEach {
            handle(it)
        }.launchIn(scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> state(): S = store.currentState() as S

    abstract suspend fun handle(action: Action)
}

