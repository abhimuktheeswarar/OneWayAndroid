package com.msa.onewaycoroutines.base.six

import androidx.lifecycle.ViewModel
import com.msa.core.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

open class BaseViewModelSix<S : State>(
    initialState: S,
    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineExceptionHandler),
    reducer: Reducer<S>? = null,
    private val store: BaseStoreSix<S> = BaseStoreSix(
        initialState = initialState,
        scope = scope,
        reducer = reducer
    ),
) : ViewModel() {

    protected val TAG: String = javaClass.simpleName

    protected val relayActions: Flow<Action> = store.relayActions

    val states: Flow<S> = store.states
    val eventActions: Flow<EventAction> = relayActions.filterIsInstance()
    val navigateActions: Flow<NavigateAction> = relayActions.filterIsInstance()

    protected fun setupReducer(reduce: (action: Action, state: S) -> S) {
        store.setupReducer(reduce)
    }

    fun state() = store.state<S>()

    suspend fun getState() = store.getState<S>()

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    override fun onCleared() {
        super.onCleared()
        store.cancel()
    }
}

private val coroutineExceptionHandler =
    CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    }