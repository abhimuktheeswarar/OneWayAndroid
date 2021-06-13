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

typealias Dispatcher = (Action) -> Unit

typealias GetState<S> = () -> S

typealias Middleware<S> = (Dispatcher, GetState<S>) -> (Dispatcher) -> Dispatcher

class StoreConfig(
    val scope: CoroutineScope,
    val debugMode: Boolean,
    val reducerTimeLimitInMilliSeconds: Long = 8L,
)

fun getDefaultScope() =
    CoroutineScope(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    })

fun getDefaultStoreConfig() = StoreConfig(getDefaultScope(), true, 8L)

val skipMiddleware: Middleware<State> = { _, _ ->
    { next ->

        {
            if (it !is SkipReducer) {
                next(it)
            }
        }
    }
}
