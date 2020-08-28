package com.msa.oneway.data.repository

import com.msa.oneway.Utils
import com.msa.oneway.common.network.KotlinRxJava2CallAdapterFactory
import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.sample.data.TodoApiService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

class TodoRepositoryTest {

    private val mockWebServer by lazy { MockWebServer() }
    private lateinit var todoApiService: TodoApiService

    @Before
    fun setup() {
        mockWebServer.start()
        todoApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(KotlinRxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApiService::class.java)
    }

    @Test
    fun testTicketDetailsApi() {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(Utils.getStringFromFile("response/todos.json"))
        mockWebServer.enqueue(response)

        val output = todoApiService.getTodoListRx().blockingGet()

        assert(output is NetworkResponse.Success)

        val todoResponse = (output as NetworkResponse.Success).body
        println("todo's count = ${todoResponse.size}")

    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
}