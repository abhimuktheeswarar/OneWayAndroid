package com.msa.oneway.core


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseOneWayViewModel<S : State>(
    private val store: Store<S>,
    private val mainThread: ThreadExecutor,
    override val coroutineContext: CoroutineContext,
    private val compositeDisposable: CompositeDisposable?
) : ViewModel(), StateHandler<S>, SideEffect {

    private val stateMutable = MutableLiveData<S>()
    val state: LiveData<S> = stateMutable.default(store.state)

    private val eventMutableLiveData = MutableLiveData<EventAction>()
    val eventLiveData: LiveData<EventAction> = eventMutableLiveData

    private val navigateMutableLiveData = MutableLiveData<NavigateAction>()
    val navigateLiveData: LiveData<NavigateAction> = navigateMutableLiveData

    init {
        store.stateHandlers.add(this)
        store.sideEffects.add(this)
    }

    override fun handle(state: S) {
        stateMutable.value = state
    }

    final override fun handle(action: Action) {
        when (action) {

            is EventAction -> {
                eventMutableLiveData.value = action
                eventMutableLiveData.value = EventConsumedAction
            }

            is NavigateAction -> {
                navigateMutableLiveData.value = action
                navigateMutableLiveData.value = NavigateConsumedAction
            }
        }
    }

    override fun getActionThreadExecutor() = mainThread

    override fun getStateThreadExecutor() = mainThread

    override fun onCleared() {
        super.onCleared()
        store.stateHandlers.clear()
        coroutineContext.cancel(CancellationException("ViewModel onCleared() called"))
        compositeDisposable?.dispose()
        store.sideEffects.clear()
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }
}

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }