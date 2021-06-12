package com.msa.onewaycoroutines.base.seven

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.core.Action
import com.msa.core.EventAction
import com.msa.core.NavigateAction
import com.msa.core.State
import com.msa.onewaycoroutines.BuildConfig
import com.msa.onewaycoroutines.base.coroutineExceptionHandler
import com.msa.onewaycoroutines.utilities.assertImmutability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 12-June-2021.
 */

open class BaseViewModelSeven<S : State>(
    private val initialState: S,
    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineExceptionHandler),
    private val reduce: Reduce<S>? = null,
    store: BaseStoreSeven<S>? = null,
) : ViewModel() {

    protected val TAG by lazy<String> { javaClass.simpleName }

    private val store = store ?: createStore()

    protected val actions: Flow<Action> = this.store.actions

    val states: Flow<S> = this.store.states
    val eventActions: Flow<EventAction> = actions.filterIsInstance()
    val navigateActions: Flow<NavigateAction> = actions.filterIsInstance()

    init {

        if (this.store.config.debugMode) {
            viewModelScope.launch(Dispatchers.Default) {
                initialState::class.assertImmutability()
            }
        }
    }

    private fun createStore(): BaseStoreSeven<S> = BaseStoreSeven(
        initialState = initialState,
        reduce = reduce ?: ::reduce,
        config = StoreConfig(scope, BuildConfig.DEBUG, 8)
    )

    fun state() = store.state()

    suspend fun awaitState() = store.awaitState()

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