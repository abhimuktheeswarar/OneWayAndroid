package com.msa.oneway.core

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */
class TestStateHandler<S : State>(
    private val store: Store<S>,
    private val threadExecutorService: ThreadExecutorService
) : StateHandler<S> {

    init {
        store.stateHandlers.add(this)
    }

    override fun handle(state: S) {
        println("handle state = $state")
    }

    override fun getStateThreadExecutor(): ThreadExecutor? = threadExecutorService
}