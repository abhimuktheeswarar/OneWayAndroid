package com.msa.oneway.core


import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseMixSideEffect(
    private val store: Store<*>,
    private val threadExecutor: ThreadExecutor,
    protected val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    protected val schedulerProvider: SchedulerProvider,
    private val compositeDisposable: CompositeDisposable
) : SideEffect, CoroutineScope {

    override val coroutineContext: CoroutineContext = coroutineDispatcherProvider.coroutineContext

    init {
        store.sideEffects.add(this)
    }

    override fun getActionThreadExecutor() = threadExecutor

    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun dispatch(action: Action) {
        store.dispatch(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : State> state(): S = store.state as S
}