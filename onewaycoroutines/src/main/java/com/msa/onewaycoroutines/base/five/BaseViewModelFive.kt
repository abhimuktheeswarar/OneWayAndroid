package com.msa.onewaycoroutines.base.five

import androidx.lifecycle.ViewModel
import com.msa.core.Action
import com.msa.core.EventAction
import com.msa.core.NavigateAction
import com.msa.core.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Created by Abhi Muktheeswarar on 09-June-2021.
 */

abstract class BaseViewModelFive<S : State>(
    initialState: S,
    reducer: (action: Action, state: S) -> S,
    protected val scope: CoroutineScope
) : ViewModel() {

    protected val TAG: String = javaClass.simpleName

    private val store: BaseStoreFive<S> = BaseStoreFive(initialState, reducer, scope)

    val states: Flow<S> = store.states
    val relayActions: Flow<Action> = store.relayActions
    val eventActions: Flow<EventAction> = relayActions.filterIsInstance()
    val navigateActions: Flow<NavigateAction> = relayActions.filterIsInstance()

    fun state() = store.state()

    suspend fun getState() = store.getState<S>()

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    override fun onCleared() {
        super.onCleared()
        store.cancel()
    }
}

