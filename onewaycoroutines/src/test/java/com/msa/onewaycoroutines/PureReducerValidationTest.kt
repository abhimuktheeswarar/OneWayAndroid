package com.msa.onewaycoroutines

import com.msa.core.State
import com.msa.onewaycoroutines.base.eight.BaseStoreEight
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import com.msa.onewaycoroutines.entities.CounterAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

data class PureReducerValidationState(val count: Int = 0, val updatedOn: Long = 0) : State

class PureReducerValidationTest {

    @get:Rule
    @Suppress("DEPRECATION")
    var thrown = ExpectedException.none()!!

    @ExperimentalCoroutinesApi
    private fun <S : State> getStore(initialState: S, reduce: Reduce<S>): BaseStoreEight<S> {
        val storeConfig =
            StoreConfig(
                scope = TestCoroutineScope(TestCoroutineDispatcher()),
                debugMode = true,
                synchronous = true)
        return BaseStoreEight(initialState,
            reduce = reduce,
            config = storeConfig,
            middlewares = null)
    }

    @Test
    fun impureReducerShouldFail() {
        val reduce: Reduce<PureReducerValidationState> = { action, state ->
            when (action) {
                is CounterAction.IncrementAction -> state.copy(count = state.count + 1,
                    updatedOn = System.nanoTime())
                else -> state
            }
        }
        val store = getStore(PureReducerValidationState(), reduce)
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("Impure reducer used!")
        store.dispatch(CounterAction.IncrementAction)
    }

    @Test
    fun pureReducerShouldNotFail() {
        val reduce: Reduce<PureReducerValidationState> = { action, state ->
            when (action) {
                is CounterAction.IncrementAction -> state.copy(count = state.count + 1)
                else -> state
            }
        }
        val store = getStore(PureReducerValidationState(), reduce)
        store.dispatch(CounterAction.IncrementAction)
    }
}
