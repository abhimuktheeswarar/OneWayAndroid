package com.msa.onewaycoroutines.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.EventAction
import com.msa.core.NavigateAction
import com.msa.onewaycoroutines.base.two.BaseStoreTwo
import com.msa.onewaycoroutines.domain.sideeffects.CounterSideEffectTwo
import com.msa.onewaycoroutines.domain.stores.CounterStoreTwo
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterViewModel(private val store: BaseStoreTwo<CounterState>) :
    ViewModel() {

    private val TAG = "PureViewModel"
    val state: Flow<CounterState> = store.states

    val eventActions: Flow<EventAction> = store.relayActions.filterIsInstance()
    val navigateActions: Flow<NavigateAction> = store.relayActions.filterIsInstance()

    init {
        store.relayActions.onEach {
            Log.i(
                "CounterViewModel",
                "collect relay actions: ${it.javaClass.simpleName} | ${store.state().counter}"
            )

        }.launchIn(viewModelScope)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    override fun onCleared() {
        super.onCleared()
        store.cancel()
    }


    companion object {

        private val coroutineExceptionHandler =
            CoroutineExceptionHandler { coroutineContext, throwable ->
                throwable.printStackTrace()
            }


        fun getViewModel(): CounterViewModel {

            val threadLocal = ThreadLocal<Int>()
            threadLocal.set(2)

            val job = SupervisorJob()

            val scope = CoroutineScope(
                job + Dispatchers.Default + coroutineExceptionHandler
            )

            /*val store = CounterBaseStoreOne(
                job = job,
                initialState = CounterState(),
                scope = scope
            )

            CounterSideEffectOne(
                store = store,
                scope = scope,
                CoroutineDispatcherProvider(scope.coroutineContext)
            )*/

            val store = CounterStoreTwo(
                initialState = CounterState(),
                scope = scope
            )

            CounterSideEffectTwo(
                store = store,
                scope = scope,
                CoroutineDispatcherProvider(scope.coroutineContext)
            )

            /* val store = CounterStoreThree(
                 initialState = CounterState(),
                 scope = scope
             )

             CounterSideEffectThree(
                 store = store,
                 scope = scope,
                 CoroutineDispatcherProvider(scope.coroutineContext)
             )*/

            /*val store = CounterStoreFour(
               initialState = CounterState(),
               scope = scope
           )

           CounterSideEffectFour(
               store = store,
               scope = scope,
               CoroutineDispatcherProvider(scope.coroutineContext)
           )*/

            return CounterViewModel(
                store = store
            )
        }
    }
}