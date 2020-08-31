package com.msa.oneway.core


import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseCoroutineSideEffect(
    private val store: Store<*>,
    private val threadExecutor: ThreadExecutor,
    protected val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : SideEffect, CoroutineScope {

    override val coroutineContext: CoroutineContext = coroutineDispatcherProvider.coroutineContext

    init {
        store.sideEffects.add(this)
    }

    override fun getActionThreadExecutor() = threadExecutor

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state as S
}