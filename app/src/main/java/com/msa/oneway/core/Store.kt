package com.msa.oneway.core


import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

abstract class Store<S : State>(
    initialState: S,
    val sideEffects: CopyOnWriteArrayList<SideEffect> = CopyOnWriteArrayList(),
    val stateHandlers: CopyOnWriteArrayList<StateHandler<S>> = CopyOnWriteArrayList(),
    private val storeThread: ThreadExecutor? = null,
    private val logger: (String, String) -> Unit = { _, _ -> Unit }
) {

    private val actions = LinkedBlockingQueue<Action>()

    var state: S = initialState
        protected set

    @Synchronized
    fun dispatch(action: Action) {
        actions.offer(action)
        when {
            storeThread != null -> storeThread.execute {
                actions.poll()?.let {
                    handle(it)
                }
            }
            else -> {
                actions.poll()?.let {
                    handle(it)
                }
            }
        }
    }

    private fun handle(action: Action) {
        val newState = reduce(action, state)
        dispatch(newState)
        sideEffects.dispatch(action)
    }

    private fun dispatch(state: S) {
        if (this.state != state) {
            this.state = state
            stateHandlers.dispatch(state)
        }
    }

    protected abstract fun reduce(action: Action, currentState: S): S
}