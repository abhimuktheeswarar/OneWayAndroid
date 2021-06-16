package com.msa.onewaycoroutines.common

import com.msa.core.Action
import com.msa.core.SkipReducer
import com.msa.core.State
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Created by Abhi Muktheeswarar on 12-June-2021.
 */

typealias Reduce<S> = (action: Action, state: S) -> S

typealias Reducer<A, S> = (A, S) -> S

typealias Dispatcher = (Action) -> Unit

typealias GetState<S> = () -> S

typealias Middleware<S> = (Dispatcher, GetState<S>) -> (Dispatcher) -> Dispatcher

//typealias CombinedReducer<S> = (Reduce<S>) -> (Reduce<S>) -> Reduce<S>

class StoreConfig(
    val scope: CoroutineScope,
    val debugMode: Boolean,
    val assertStateValues: Boolean = debugMode,
    val mutableStateChecker: Boolean = debugMode,
    val reducerTimeLimitInMilliSeconds: Long = 8L,
)

fun getDefaultScope() =
    CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    })

fun getDefaultStoreConfig() =
    StoreConfig(scope = getDefaultScope(), debugMode = true, reducerTimeLimitInMilliSeconds = 8L)

val skipMiddleware: Middleware<State> = { _, _ ->
    { next ->

        {
            if (it !is SkipReducer) {
                next(it)
            }
        }
    }
}

fun <S : State> combineReducers(vararg reducers: Reduce<S>): Reduce<S> =
    { action, state ->
        //Log.d("combineReducers", "outer:  ${action.name()} | ${state}")
        reducers.fold(state, { s, reducer ->
            //Log.d("combineReducers", "inner:  ${action.name()} | ${s}")
            reducer(action, s)
        })
    }

operator fun <S> Reduce<S>.plus(other: Reduce<S>): Reduce<S> = { action, state ->
    other(action, this(action, state))
}

inline fun <reified A : Action, S> reducerForAction(crossinline reducer: Reducer<A, S>): Reduce<S> =
    { action, state ->
        when (action) {
            is A -> reducer(action, state)
            else -> state
        }
    }

