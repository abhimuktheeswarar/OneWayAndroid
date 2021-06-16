package com.msa.onewaycoroutines

import com.msa.core.State
import com.msa.onewaycoroutines.base.eight.BaseStoreEight
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import org.junit.Assert
import org.junit.Test

/**
 * Created by Abhi Muktheeswarar on 16-June-2021.
 */

class StoreTest {

    data class TestState(val count: Int = 0) : State

    private var count = 0

    @Test
    fun replayTest() = runBlocking {
        repeat(1) {
            singleReplayTestIteration(N = 200, subscribers = 1)
        }
        Unit
    }

    /**
     * Tests consistency of produced flow. E.g. for just increment reducer output must be
     * 1,2,3,4,5
     * not 1,3,4,5 (value missing)
     * or 4,3,4,5 (incorrect order)
     * or 3,3,4,5 (duplicate value)
     */
    private suspend fun singleReplayTestIteration(N: Int, subscribers: Int) =
        withContext(Dispatchers.Default) {
            val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            val reduce: Reduce<TestState> = { action, state ->
                when (action) {
                    is CounterAction.IncrementAction -> state.copy(count = state.count + 1)
                    else -> state
                }
            }
            val storeConfig =
                StoreConfig(
                    scope = scope,
                    debugMode = false,
                    reducerTimeLimitInMilliSeconds = 6000)
            val store =
                BaseStoreEight(initialState = TestState(),
                    reduce = reduce,
                    config = storeConfig,
                    middlewares = null)

            launch {
                repeat(N) {
                    store.dispatch(CounterAction.IncrementAction)
                }
            }

            // One more scope for subscribers, to ensure subscribers are finished before cancelling store scope
            coroutineScope {
                repeat(subscribers) {
                    launch {
                        // Since only increase by 1 reducers are applied
                        // it's expected to see monotonously increasing sequence with no missing values
                        store.states.takeWhile { it.count < N }.toList().zipWithNext { a, b ->
                            Assert.assertEquals(a.count + 1, b.count)
                        }
                    }
                }
            }
            scope.cancel()
        }
}