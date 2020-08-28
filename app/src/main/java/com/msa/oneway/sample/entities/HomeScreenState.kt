package com.msa.oneway.sample.entities

import com.msa.oneway.core.State

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

data class HomeScreenState(
    val loading: Boolean = true,
    val todoResponse: TodoResponse? = null,
    val exception: Exception? = null,
    val count: Int = 0
    //val countLiveData: LiveData<Int> = MutableLiveData<Int>().default(0)
) : State