package com.msa.onewaycoroutines.base.eight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.core.*
import com.msa.onewaycoroutines.BuildConfig
import com.msa.onewaycoroutines.common.*
import com.msa.onewaycoroutines.utilities.assertImmutability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

open class BaseViewModelEight<S : State>(
    private val initialState: S? = null,
    private val reduce: Reduce<S>? = null,
    store: BaseStoreEight<S>? = null,
) : ViewModel() {

    protected val TAG by lazy<String> { javaClass.simpleName }

    private val store = store ?: createStore()

    protected val hotActions: Flow<Action> = this.store.hotActions
    protected val coldActions: Flow<Action> = this.store.coldActions
    protected val scope = this.store.config.scope

    val states: Flow<S> = this.store.states
    val eventActions: Flow<EventAction> = hotActions.filterIsInstance()
    val navigateActions: Flow<NavigateAction> = hotActions.filterIsInstance()

    init {

        if (this.store.config.debugMode) {
            viewModelScope.launch(Dispatchers.Default) {
                this@BaseViewModelEight.store.initialState::class.assertImmutability()
            }
        }
    }

    private fun createStore(): BaseStoreEight<S> = BaseStoreEight(
        initialState = initialState!!,
        reduce = reduce ?: ::reduce,
        middlewares = null,
        config = StoreConfig(scope = scope,
            debugMode = BuildConfig.DEBUG,
            reducerTimeLimitInMilliSeconds = 8)
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