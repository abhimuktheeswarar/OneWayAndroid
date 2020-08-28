package com.msa.oneway.sample.data

import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.sample.entities.TodoResponse
import io.reactivex.Single

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

interface TodoRepository {

    fun getTodoListRx(): Single<NetworkResponse<TodoResponse, Error>>

    suspend fun getTodoListCoroutine(): TodoResponse
}