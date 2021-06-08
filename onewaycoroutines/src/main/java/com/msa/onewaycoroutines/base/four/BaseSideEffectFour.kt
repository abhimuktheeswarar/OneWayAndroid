package com.msa.onewaycoroutines.base.four

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 08-June-2021.
 */

abstract class BaseSideEffectFour(
    private val store: BaseStoreFour<*>,
    private val scope: CoroutineScope,
    protected val dispatchers: CoroutineDispatcherProvider
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
    suspend fun <S : State> state(): S = store.currentState() as S

    abstract suspend fun handle(action: Action)
}