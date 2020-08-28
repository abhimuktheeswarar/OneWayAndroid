package com.msa.oneway.core

import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

interface SideEffect : ActionSubscriber

interface StateHandler<in S : State> : StateSubscriber<S>

interface ActionSubscriber : CoroutineScope {

    fun onNext(action: Action) {
        getActionThreadExecutor()?.execute { handle(action) } ?: handle(action)
    }

    fun handle(action: Action)

    fun getActionThreadExecutor(): ThreadExecutor?
}

interface StateSubscriber<in S> {

    fun onNext(state: S) {
        getStateThreadExecutor()?.execute { handle(state) } ?: handle(state)
    }

    fun handle(state: S)

    fun getStateThreadExecutor(): ThreadExecutor?
}

fun CopyOnWriteArrayList<SideEffect>.dispatch(action: Action) {
    forEach { it.onNext(action) }
}

fun <S : State> CopyOnWriteArrayList<StateHandler<S>>.dispatch(state: S) {
    forEach { it.onNext(state) }
}
