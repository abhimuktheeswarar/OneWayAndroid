package com.msa.onewaycoroutines.base.six

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.SideEffect
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

abstract class BaseSideEffectSix(
    private val store: BaseStoreSix<*>,
    protected val scope: CoroutineScope,
    protected val dispatchers: CoroutineDispatcherProvider
) : SideEffect {

    protected val TAG: String = this.javaClass.simpleName

    init {
        store.relayActions.onEach(::handle).launchIn(scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state()

    suspend fun <S : State> getState(): S = store.getState()
}