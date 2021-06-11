package com.msa.oneway.core


import android.util.Log
import com.msa.core.Action
import com.msa.core.State
import com.msa.core.name
import com.msa.oneway.sample.counter.CounterAction
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.measureTimeMillis

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

abstract class Store<S : State>(
    initialState: S,
    val sideEffects: CopyOnWriteArrayList<SideEffect> = CopyOnWriteArrayList(),
    val stateHandlers: CopyOnWriteArrayList<StateHandler<S>> = CopyOnWriteArrayList(),
    private val storeThread: ThreadExecutor = StoreThreadService(),
    private val logger: (String, String) -> Unit = { _, _ -> Unit }
) {

    private val actions = LinkedBlockingQueue<Action>()

    var state: S = initialState
        private set


    var count = 1

    @Synchronized
    fun dispatch(action: Action) {
        actions.offer(action)
        measureTimeMillis {
            storeThread.execute {
                actions.poll()?.let {
                    handle(it)
                }
            }
        }.let { timeTakenToComputeNewState ->
            //To make sure we are not doing any heavy work in reducer
            if (timeTakenToComputeNewState > 8) {
                Log.w("Store", "$count Took ${timeTakenToComputeNewState}ms for $action")
                //throw ExceededTimeLimitToComputeNewStatException("Took ${timeTakenToComputeNewState}ms for ${action.name()}")
            } else {
                Log.d(
                    "Store",
                    " $count Took ${timeTakenToComputeNewState}ms for ${action.name()}"
                )
            }

            if (action is CounterAction.ResetAction) {
                count = 1
            } else count++
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