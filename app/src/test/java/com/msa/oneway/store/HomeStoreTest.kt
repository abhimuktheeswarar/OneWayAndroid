package com.msa.oneway.store

import com.msa.oneway.core.TestThreadExecutorService
import com.msa.oneway.sample.domain.HomeStore
import com.msa.oneway.sample.entities.HomeScreenState
import com.msa.oneway.sample.entities.TodoAction
import org.junit.Test

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

class HomeStoreTest {

    private val store by lazy { HomeStore(HomeScreenState(), TestThreadExecutorService()) }

    @Test
    fun demoRun() {
        store.dispatch(TodoAction.UpdateCountAction)
        assert(store.state.count == 1)
    }
}


