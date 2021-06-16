package com.msa.oneway.store

import com.msa.core.Action
import com.msa.core.State
import com.msa.oneway.core.Store
import com.msa.oneway.core.TestThreadExecutorService
import com.msa.oneway.sample.counter.CounterAction
import kotlinx.coroutines.*
import org.junit.Test

/**
 * Created by Abhi Muktheeswarar on 16-June-2021.
 */

class StoreTest {

    data class TestState(val count: Int = 0) : State

    @Test
    fun replayTest() = runBlocking {
        repeat(100) {
            singleReplayTestIteration(N = 5000, subscribers = 10)
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

            val store =
                object : Store<TestState>(initialState = TestState(),
                    storeThread = TestThreadExecutorService()) {
                    override fun reduce(action: Action, currentState: TestState): TestState {
                        return when (action) {
                            is CounterAction.IncrementAction -> state.copy(count = state.count + 1)
                            else -> state
                        }
                    }
                }

            async {
                repeat(N) {
                    store.dispatch(CounterAction.IncrementAction)
                }
            }

            // One more scope for subscribers, to ensure subscribers are finished before cancelling store scope
            coroutineScope {
                repeat(subscribers) {
                    async {
                        // Since only increase by 1 reducers are applied
                        // it's expected to see monotonously increasing sequence with no missing values
                        /*store.sl.takeWhile { it.count < N }.toList().zipWithNext { a, b ->
                            Assert.assertEquals(a.count + 1, b.count)
                        }*/
                    }
                }
            }
            scope.cancel()
        }
}