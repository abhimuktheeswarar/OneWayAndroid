package com.msa.oneway.core


import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseSideEffect(
    private val store: Store<*>,
    private val threadExecutor: ThreadExecutor? = null,
    protected val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val compositeDisposable: CompositeDisposable? = null
) : SideEffect {

    override val coroutineContext: CoroutineContext = coroutineDispatcherProvider.coroutineContext

    init {
        store.sideEffects.add(this)
    }

    override fun getActionThreadExecutor() = threadExecutor

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable?.add(disposable)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }
}