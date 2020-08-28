package com.msa.oneway.sample.data

import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.sample.entities.TodoResponse
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

interface TodoApiService {

    @GET("todos")
    fun getTodoListRx(): Single<NetworkResponse<TodoResponse, Error>>

    @GET("todos")
    suspend fun getTodoListCoroutine(): TodoResponse
}