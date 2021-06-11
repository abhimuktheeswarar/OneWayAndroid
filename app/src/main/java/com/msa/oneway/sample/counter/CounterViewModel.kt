package com.msa.oneway.sample.counter

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.msa.core.*
import com.msa.oneway.common.ShowToastAction
import com.msa.oneway.core.*
import com.msa.oneway.core.SideEffect
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

sealed interface CounterAction : Action {

    object IncrementAction : CounterAction

    object DecrementAction : CounterAction

    object ResetAction : CounterAction

    data class ForceUpdateAction(val count: Int) : CounterAction
}

data class CounterState(val counter: Int = 0) : State

class CounterStore(
    sideEffects: CopyOnWriteArrayList<SideEffect> = CopyOnWriteArrayList(),
    stateHandlers: CopyOnWriteArrayList<StateHandler<CounterState>> = CopyOnWriteArrayList(),
    logger: (String, String) -> Unit = { _, _ -> Unit }
) : Store<CounterState>(CounterState(), sideEffects, stateHandlers, StoreThreadService(), logger) {

    private val TAG: String = javaClass.simpleName

    override fun reduce(action: Action, currentState: CounterState): CounterState {
        Log.d(TAG, "reduce action = ${action.name()} | $currentState | ${Thread.currentThread()}")
        return when (action) {

            is CounterAction.IncrementAction -> currentState.copy(counter = currentState.counter + 1)

            is CounterAction.DecrementAction -> currentState.copy(counter = currentState.counter - 1)

            is CounterAction.ForceUpdateAction -> currentState.copy(counter = action.count)

            is CounterAction.ResetAction -> currentState.copy(counter = 0)

            else -> currentState
        }
    }
}

class CounterSideEffect(
    store: Store<*>,
    threadExecutor: ThreadExecutor,
    scope: CoroutineScope,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : BaseCoroutineSideEffect(store, threadExecutor, scope, coroutineDispatcherProvider) {

    override fun handle(action: Action) {
        when (action) {

            is CounterAction.IncrementAction -> {
                //dispatch(CounterAction.ForceUpdateAction(state<CounterState>().counter - 1))
                //dispatch(CounterAction.DecrementAction)
            }

            is CounterAction.DecrementAction -> {

            }

            is CounterAction.ForceUpdateAction -> {

            }

            is CounterAction.ResetAction -> {
                dispatch(ShowToastAction("Reset completed"))


            }
        }
    }
}

class CounterViewModel(
    store: Store<CounterState>,
    mainThread: ThreadExecutor,
    coroutineContext: CoroutineContext,
    compositeDisposable: CompositeDisposable?
) : BaseOneWayViewModel<CounterState>(store, mainThread, coroutineContext, compositeDisposable) {

    companion object {

        fun get(context: Context): CounterViewModel {

            val threadExecutorService = SideEffectThreadService()
            val mainThread = MainThread(WeakReference(context))
            val coroutineContext = CoroutineScopeProvider.getOneWayViewModelCoroutineContext()
            val coroutineDispatcherProvider = CoroutineDispatcherProvider(coroutineContext)
            val schedulerProvider = SchedulerProvider(Schedulers.from(ExecutorServices.sideEffect))
            val compositeDisposable = CompositeDisposable()

            val store = CounterStore()

            val counterViewModel = CounterViewModel(
                store,
                mainThread,
                coroutineDispatcherProvider.coroutineContext,
                compositeDisposable
            )

            CounterSideEffect(
                store,
                threadExecutorService,
                counterViewModel.viewModelScope,
                coroutineDispatcherProvider
            )

            return counterViewModel
        }
    }
}