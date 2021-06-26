package com.msa.onewaycoroutines

import com.msa.core.State
import com.msa.onewaycoroutines.base.eight.BaseStoreEight
import com.msa.onewaycoroutines.common.Middleware
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Created by Abhi Muktheeswarar on 19-June-2021.
 */

@ExperimentalCoroutinesApi
class MiddlewareTest {

    data class TestState(val count: Int = 1) : State

    private val reduce: Reduce<TestState> = { action, state ->
        when (action) {
            is CounterAction.IncrementAction -> state.copy(count = state.count + 1)
            is CounterAction.DecrementAction -> state.copy(count = state.count - 1)
            is CounterAction.ForceUpdateAction -> state.copy(count = action.count)
            is CounterAction.ResetAction -> state.copy(count = 0)
            else -> state
        }
    }

    private val plainMiddleware: Middleware<TestState> = { dispatch, getState ->

        { next ->

            { action ->

                next(action)
            }
        }
    }

    private val dispatchingMiddleware: Middleware<TestState> = { dispatch, getState ->

        { next ->

            { action ->

                when (action) {
                    is CounterAction.IncrementAction -> {
                        dispatch(CounterAction.ForceUpdateAction(5))
                    }
                    is CounterAction.DecrementAction -> {
                        dispatch(CounterAction.IncrementAction)
                    }
                    else -> next(action)
                }
            }
        }
    }

    private fun getStore(): BaseStoreEight<TestState> {
        val storeConfig =
            StoreConfig(
                scope = CoroutineScope(Job()),
                debugMode = false,
                synchronous = false)
        return BaseStoreEight(initialState = TestState(),
            reduce = reduce,
            config = storeConfig,
            middlewares = listOf(plainMiddleware, dispatchingMiddleware))
    }

    @Test
    fun testMiddlewareBehaviour() = runBlocking {
        val store = getStore()
        store.dispatch(CounterAction.IncrementAction)
        store.dispatch(CounterAction.DecrementAction)
        assertEquals(5, store.awaitState().count)
        store.dispatch(CounterAction.ResetAction)
        assertEquals(0, store.awaitState().count)
    }
}