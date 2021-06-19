package com.msa.onewaycoroutines.base.nine

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.SideEffect
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Abhi Muktheeswarar on 16-June-2021.
 */

@ExperimentalStdlibApi
abstract class BaseSideEffectHotNine(
    private val store: BaseStoreNine<*>,
    protected val dispatchers: CoroutineDispatcherProvider,
) : SideEffect {

    protected val TAG: String = this.javaClass.simpleName
    protected val scope: CoroutineScope = store.config.scope

    init {
        store.hotActions.onEach(::handle).launchIn(scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> awaitState(): S = store.awaitState() as S
}

abstract class BaseSideEffectColdNine(
    private val store: BaseStoreNine<*>,
    protected val dispatchers: CoroutineDispatcherProvider,
) : SideEffect {

    protected val TAG: String = this.javaClass.simpleName
    protected val scope: CoroutineScope = store.config.scope

    init {
        store.coldActions.onEach(::handle).launchIn(scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> awaitState(): S = store.awaitState() as S
}

abstract class BaseSideEffectHotColdNine(
    private val store: BaseStoreNine<*>,
    protected val dispatchers: CoroutineDispatcherProvider,
) {

    protected val TAG: String = this.javaClass.simpleName
    protected val scope: CoroutineScope = store.config.scope

    init {
        store.hotActions.onEach(::handleHot).launchIn(scope)
        store.coldActions.onEach(::handleCold).launchIn(scope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state as S

    @Suppress("UNCHECKED_CAST")
    suspend fun <S : State> awaitState(): S = store.awaitState() as S

    abstract fun handleHot(action: Action)

    abstract fun handleCold(action: Action)
}