package com.msa.onewaycoroutines.base.eight

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import com.msa.onewaycoroutines.common.Dispatcher
import com.msa.onewaycoroutines.common.GetState
import com.msa.onewaycoroutines.common.Middleware
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

abstract class BaseMiddleware<S : State>(
    protected val scope: CoroutineScope,
    protected val coroutineDispatcherProvider: CoroutineDispatcherProvider,
) {

    protected val TAG: String = javaClass.simpleName

    private val middleware: Middleware<S> = { dispatch, getState ->

        { next ->

            { action ->

                handle(action, getState, next, dispatch)
            }
        }
    }

    fun get() = middleware

    abstract fun handle(action: Action, state: GetState<S>, next: Dispatcher, dispatch: Dispatcher)
}