package com.msa.onewaycoroutines

import com.msa.core.Action
import com.msa.core.State
import com.msa.onewaycoroutines.base.eight.BaseStoreEight
import com.msa.onewaycoroutines.base.eight.BaseViewModelEight
import com.msa.onewaycoroutines.common.Reduce
import com.msa.onewaycoroutines.common.StoreConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

data class StateWithMutableMap(val map: MutableMap<String, String> = mutableMapOf()) : State
data class StateWithImmutableMap(val map: Map<String, String> = mapOf()) : State

data class UpdateDataAction(val item: Pair<String, String>) : Action

class MutableStateValidationTest {

    @ExperimentalCoroutinesApi
    @Test(expected = IllegalArgumentException::class)
    fun mutableStateShouldFail() = runBlockingTest {
        val storeConfig =
            StoreConfig(scope = TestCoroutineScope(TestCoroutineDispatcher()), debugMode = true)
        val initialState = StateWithMutableMap(map = mutableMapOf("1" to "one"))
        val reduce: Reduce<StateWithMutableMap> =
            { action, state ->
                if (action is UpdateDataAction) {
                    val map = state.map.apply { put(action.item.first, action.item.second) }
                    state.copy(map = map)
                } else state
            }
        val store = BaseStoreEight(initialState = initialState,
            config = storeConfig,
            reduce = reduce,
            middlewares = null)
        val viewModel = BaseViewModelEight(initialState = initialState, store = store)
        viewModel.dispatch(UpdateDataAction(Pair("1", "two")))
    }

    @Test
    fun immutableStateShouldNotFail() {
        val initialState = StateWithImmutableMap(map = mapOf("2" to "two"))
        BaseViewModelEight(initialState = initialState, reduce = { action, state -> state })
    }
}
