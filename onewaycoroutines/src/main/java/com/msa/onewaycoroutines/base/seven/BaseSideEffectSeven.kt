package com.msa.onewaycoroutines.base.seven

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.SideEffect
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

abstract class BaseSideEffectSeven(
    private val store: BaseStoreSeven<*>,
    protected val scope: CoroutineScope,
    protected val dispatchers: CoroutineDispatcherProvider
) : SideEffect {

    protected val TAG: String = this.javaClass.simpleName

    init {
        store.actions.onEach(::handle).launchIn(scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state() as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> getState(): S = store.getState() as S
}