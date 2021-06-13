package com.msa.onewaycoroutines.base

import com.msa.core.Action
import com.msa.core.State
import kotlinx.coroutines.flow.Flow

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

interface Store<S : State> {

    val actions: Flow<Action>
    val states: Flow<S>

    fun dispatch(action: Action)

    fun state(): S

    suspend fun awaitState(): S

    fun terminate()
}

const val TAG_STORE = "Store"
const val TAG_REDUCER = "Reducer"
const val TAG_MIDDLEWARE = "Middleware"