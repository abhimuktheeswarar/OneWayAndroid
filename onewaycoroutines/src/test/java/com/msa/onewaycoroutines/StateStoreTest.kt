package com.msa.onewaycoroutines

import com.msa.core.State
import com.msa.onewaycoroutines.base.nine.BaseStoreNine
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

data class TestState(val count: Int = 1, val list: List<Int> = emptyList()) : State

@Suppress("EXPERIMENTAL_API_USAGE")
class StateStoreTest {

    private val reduce: Reduce<TestState> = { action, state ->
        when (action) {
            is CounterAction.IncrementAction -> state.copy(count = state.count + 1)
            is CounterAction.DecrementAction -> state.copy(count = state.count - 1)
            is CounterAction.ForceUpdateAction -> state.copy(count = action.count)
            else -> state
        }
    }

    private fun getStore(reduce: Reduce<TestState> = this.reduce): BaseStoreNine<TestState> {
        val storeConfig =
            StoreConfig(
                scope = TestCoroutineScope(TestCoroutineDispatcher()),
                debugMode = false,
                synchronous = true)
        return BaseStoreNine(initialState = TestState(),
            reduce = reduce,
            config = storeConfig,
            middlewares = null)
    }

    @Test
    fun testGetRunsSynchronouslyForTests() = runBlocking {
        var callCount = 0
        val reduce: Reduce<TestState> = { action, state ->
            callCount++
            state
        }
        val store = getStore(reduce)
        store.dispatch(CounterAction.IncrementAction)
        assertEquals(1, callCount)
    }

    @Test
    fun testSetState() = runBlocking {
        var called = false
        val reduce: Reduce<TestState> = { action, state ->
            assertEquals(2, (action as CounterAction.ForceUpdateAction).count)
            called = true
            state
        }
        val store = getStore(reduce)
        store.dispatch(CounterAction.ForceUpdateAction(2))
        assertTrue(called)
    }

    @Test
    fun testSubscribeNotCalledForSameValue() = runBlockingTest {
        val store = getStore()
        var callCount = 0
        val job = store.states.onEach {
            callCount++
        }.launchIn(this)
        assertEquals(1, callCount)
        store.dispatch(CounterAction.ForceUpdateAction(1))
        assertEquals(1, callCount)
        job.cancel()
    }

    @Test
    fun testBlockingReceiver() = runBlockingTest {
        val store = getStore()
        val values = mutableListOf<Int>()
        val job = launch {
            store.states.collect {
                values += it.count
                delay(10)
            }
        }

        (2..10).forEach {
            store.dispatch(CounterAction.ForceUpdateAction(it))
        }
        delay(100)
        job.cancel()
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), values)
    }
}
