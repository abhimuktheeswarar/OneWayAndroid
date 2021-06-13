package com.msa.onewaycoroutines.base.eight

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.SideEffect
import com.msa.core.State
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

abstract class BaseSideEffectHotEight(
    private val store: BaseStoreEight<*>,
    protected val dispatchers: CoroutineDispatcherProvider,
) : SideEffect {

    protected val TAG: String = this.javaClass.simpleName

    init {
        store.hotActions.onEach(::handle).launchIn(store.config.scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state() as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> awaitState(): S = store.awaitState() as S
}

abstract class BaseSideEffectColdEight(
    private val store: BaseStoreEight<*>,
    protected val dispatchers: CoroutineDispatcherProvider,
) : SideEffect {

    protected val TAG: String = this.javaClass.simpleName

    init {
        store.coldActions.onEach(::handle).launchIn(store.config.scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state() as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> awaitState(): S = store.awaitState() as S
}

abstract class BaseSideEffectHotColdEight(
    private val store: BaseStoreEight<*>,
    protected val dispatchers: CoroutineDispatcherProvider,
) {

    protected val TAG: String = this.javaClass.simpleName

    init {
        store.hotActions.onEach(::handleHot).launchIn(store.config.scope)
        store.coldActions.onEach(::handleCold).launchIn(store.config.scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state() as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> awaitState(): S = store.awaitState() as S

    abstract fun handleHot(action: Action)

    abstract fun handleCold(action: Action)
}