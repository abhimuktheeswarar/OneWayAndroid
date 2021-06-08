package com.msa.oneway.core

import com.msa.core.Action
import com.msa.core.State


/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseSideEffect(
    private val store: Store<*>,
    private val threadExecutor: ThreadExecutor
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