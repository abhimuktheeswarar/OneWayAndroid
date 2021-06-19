package com.msa.onewaycoroutines.domain.middlewares

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.core.State
import com.msa.core.name
import com.msa.onewaycoroutines.common.BaseMiddleware
import com.msa.onewaycoroutines.common.Dispatcher
import com.msa.onewaycoroutines.common.GetState
import com.msa.onewaycoroutines.common.ShowToastAction
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Abhi Muktheeswarar on 13-June-2021.
 */

class EventMiddleware(
    private val id: Any,
    private val context: Context,
    scope: CoroutineScope,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
) :
    BaseMiddleware<State>(scope, coroutineDispatcherProvider) {

    override fun handle(
        action: Action,
        state: GetState<State>,
        next: Dispatcher,
        dispatch: Dispatcher,
    ) {

        Log.d(TAG, "$id: ${action.name()} | ${Thread.currentThread()}")

        when (action) {

            is CounterAction.ResetAction -> {
                next(action)
                dispatch(ShowToastAction("Reset completed"))
            }

            is ShowToastAction -> {
                Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
            }

            else -> next(action)
        }
    }
}