@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.msa.onewaycoroutines

import com.msa.core.State
import com.msa.onewaycoroutines.base.nine.BaseStoreNine
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue

data class OrderingState(val count: Int = 0) : State

class SetStateWithStateOrderingTest {

    private fun getStore(
        scope: CoroutineScope,
        reduce: Reduce<OrderingState>,
    ): BaseStoreNine<OrderingState> {
        val storeConfig =
            StoreConfig(
                scope = scope,
                debugMode = false,
                synchronous = false)
        return BaseStoreNine(initialState = OrderingState(),
            reduce = reduce,
            config = storeConfig,
            middlewares = null)
    }

    @Test
    fun test1() = runBlocking {
        val calls = mutableListOf<String>()
        val reduce: Reduce<OrderingState> = { action, state ->
            when (action) {
                is CounterAction.IncrementAction -> {
                    println("state updating for IncrementAction")
                    calls += "s1"
                    state.copy(count = state.count + 1)
                }
                else -> state
            }
        }
        val store = getStore(TestCoroutineScope(), reduce)
        store.dispatch(CounterAction.IncrementAction)
        launch {
            store.awaitState()
            println("state received")
            calls += "w1"
        }
        assertMatches(calls, "s1", "w1")
    }

    @Test
    fun test2() = runBlocking {
        val calls = mutableListOf<String>()
        val reduce: Reduce<OrderingState> = { action, state ->
            when (action) {
                is CounterAction.IncrementAction -> {
                    println("state updating for IncrementAction")
                    calls += "s1"
                    state.copy(count = state.count + 1)
                }
                else -> state
            }
        }
        val store = getStore(TestCoroutineScope(), reduce)
        launch {
            store.awaitState()
            println("state received 0")
            calls += "w1"
            store.dispatch(CounterAction.IncrementAction)
            launch {
                store.awaitState()
                println("state received 1")
                calls += "w2"
            }
        }
        assertMatches(calls, "w1", "s1", "w2")
    }

    @Test
    fun test3() = runBlocking {
        val calls = mutableListOf<String>()
        val reduce: Reduce<OrderingState> = { action, state ->
            when (action) {
                is CounterAction.IncrementAction -> {
                    println("state updating for IncrementAction")
                    calls += "s1"
                    state.copy(count = state.count + 1)
                }
                else -> state
            }
        }
        val store = getStore(TestCoroutineScope(), reduce)
        launch {
            store.awaitState()
            println("state received 0")
            calls += "w1"
            launch {
                store.awaitState()
                println("state received 1")
                calls += "w2"
            }
            store.dispatch(CounterAction.IncrementAction)
        }
        assertMatches(calls, "w1", "s1", "w2")
    }

    @Test
    fun test4() = runBlocking {
        val calls = ConcurrentLinkedQueue<String>()
        val reduce: Reduce<OrderingState> = { action, state ->
            when (action) {
                is CounterAction.IncrementAction -> {
                    println("state updating for IncrementAction")
                    calls += "s1"
                    state.copy(count = state.count + 1)
                }
                is CounterAction.DecrementAction -> {
                    println("state updating for DecrementAction")
                    calls += "s2"
                    state.copy(count = state.count - 1)
                }
                is CounterAction.ForceUpdateAction -> {
                    println("state updating for ForceUpdateAction")
                    calls += "s3"
                    state.copy(count = action.count)
                }
                else -> state
            }
        }
        val store = getStore(TestCoroutineScope(), reduce)
        launch {
            store.awaitState()
            calls += "w1"
            launch {
                calls += "w2"
            }
            store.dispatch(CounterAction.IncrementAction)
            store.dispatch(CounterAction.DecrementAction)
            val count = store.awaitState().count
            store.dispatch(CounterAction.ForceUpdateAction(count))
        }
        println(store.state.count)
        assertMatches(calls, "w1", "s1", "s2", "s3", "w2")
    }

    private suspend fun assertMatches(calls: Collection<String>, vararg expectedCalls: String) {
        while (calls.size != expectedCalls.size) {
            delay(1)
        }
        assertEquals(expectedCalls.toList(), calls.toList())
    }
}