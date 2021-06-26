package com.msa.onewaycoroutines

import com.msa.core.State
import com.msa.onewaycoroutines.base.eight.BaseStoreEight
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SetStateWithStateAfterScopeCancellationTest {

    data class TestState(val count: Int = 1) : State

    @ExperimentalCoroutinesApi
    @Test
    fun setStateAfterScopeCancellation() = runBlockingTest {
        val scope = TestCoroutineScope(Job())
        scope.cancel()
        val reduce: Reduce<TestState> = { action, state ->
            when (action) {
                is CounterAction.ForceUpdateAction -> state.copy(count = action.count)
                else -> state
            }
        }
        val storeConfig =
            StoreConfig(
                scope = scope,
                debugMode = false)
        val store =
            BaseStoreEight(initialState = TestState(),
                reduce = reduce,
                config = storeConfig,
                middlewares = null)
        store.dispatch(CounterAction.ForceUpdateAction(4))
        // ensure set operation above is ignored
        val count = store.state().count
        assertEquals(1, count)
    }
}