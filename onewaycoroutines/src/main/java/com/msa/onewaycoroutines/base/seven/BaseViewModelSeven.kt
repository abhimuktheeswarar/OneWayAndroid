package com.msa.onewaycoroutines.base.seven

import androidx.lifecycle.ViewModel
import com.msa.core.Action
import com.msa.core.EventAction
import com.msa.core.NavigateAction
import com.msa.core.State
import com.msa.onewaycoroutines.base.coroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Created by Abhi Muktheeswarar on 12-June-2021.
 */

open class BaseViewModelSeven<S : State>(
    private val initialState: S,
    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineExceptionHandler),
    private val reduce: ((action: Action, state: S) -> S)? = null,
    store: BaseStoreSeven<S>? = null,
) : ViewModel() {

    private val store = store ?: createStore()

    protected val TAG: String = javaClass.simpleName
    protected val actions: Flow<Action> = this.store.actions

    val states: Flow<S> = this.store.states
    val eventActions: Flow<EventAction> = actions.filterIsInstance()
    val navigateActions: Flow<NavigateAction> = actions.filterIsInstance()

    private fun createStore(): BaseStoreSeven<S> = BaseStoreSeven(
        initialState = initialState,
        scope = scope,
        reduce = reduce ?: ::reduce
    )

    fun state() = store.state()

    suspend fun getState() = store.getState()

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    protected open fun reduce(action: Action, state: S): S {
        throw NotImplementedError("Either provide a reducer in constructor or override this function")
    }

    override fun onCleared() {
        super.onCleared()
        store.terminate()
    }
}