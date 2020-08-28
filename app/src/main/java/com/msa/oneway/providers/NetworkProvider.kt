package com.msa.oneway.providers

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.msa.oneway.common.network.KotlinRxJava2CallAdapterFactory
import com.msa.oneway.sample.data.TodoApiService
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

object NetworkProvider {

    val todoApiService by lazy { createApiService<TodoApiService>("https://jsonplaceholder.typicode.com/") }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .callTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(
                LoggingInterceptor.Builder()
                    .setLevel(Level.BASIC)
                    .log(Platform.INFO)
                    .request("Request")
                    .response("Response")
                    .build()
            ).build()
    }

    private inline fun <reified A> createApiService(baseUrl: String): A {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(KotlinRxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(A::class.java)
    }
}