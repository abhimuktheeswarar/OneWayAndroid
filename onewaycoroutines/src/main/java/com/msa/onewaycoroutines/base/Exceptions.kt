package com.msa.onewaycoroutines.base

import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * Created by Abhi Muktheeswarar on 11-June-2021.
 */

class ExceededTimeLimitToComputeNewStatException(override val message: String) : Exception()

val coroutineExceptionHandler =
    CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
    }