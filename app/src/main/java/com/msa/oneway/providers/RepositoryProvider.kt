package com.msa.oneway.providers

import android.content.Context
import com.msa.oneway.common.AndroidResourceRepository
import com.msa.oneway.common.ResourceRepository
import com.msa.oneway.sample.data.TodoRepositoryImpl

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

object RepositoryProvider {

    val todoRepository by lazy { TodoRepositoryImpl(NetworkProvider.todoApiService) }

    fun getResourceRepository(context: Context): ResourceRepository =
        AndroidResourceRepository(context)
}