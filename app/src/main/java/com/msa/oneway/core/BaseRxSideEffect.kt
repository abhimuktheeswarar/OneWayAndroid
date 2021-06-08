package com.msa.oneway.core


import com.msa.core.Action
import com.msa.core.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

@Suppress("LeakingThis")
abstract class BaseRxSideEffect(
    private val store: Store<*>,
    private val threadExecutor: ThreadExecutor,
    protected val schedulerProvider: SchedulerProvider,
    private val compositeDisposable: CompositeDisposable
) : SideEffect {

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