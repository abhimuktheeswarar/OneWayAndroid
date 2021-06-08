package com.msa.oneway.core


import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseCoroutineSideEffect(
    private val store: Store<*>,
    private val threadExecutor: ThreadExecutor,
    protected val scope: CoroutineScope,
    protected val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : SideEffect {

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