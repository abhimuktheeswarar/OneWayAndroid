package com.msa.onewaycoroutines.ui

import androidx.lifecycle.ViewModel
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.domain.sideeffects.CounterSideEffectTwo
import com.msa.onewaycoroutines.domain.stores.CounterStoreTwo
import com.msa.onewaycoroutines.entities.CounterState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

class CounterViewModel(private val store: CounterStoreTwo) :
    ViewModel() {

    private val tag = "PureViewModel"
    val state: Flow<CounterState> = store.states

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

            val scope = CoroutineScope(
                SupervisorJob() + Dispatchers.Default + coroutineExceptionHandler
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