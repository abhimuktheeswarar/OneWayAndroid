package com.msa.oneway.common.network

import java.io.IOException

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

/**
 * Represents the result of making a network request.
 *
 * @param T success body type for 2xx response.
 * @param U error body type for non-2xx response.
 */
sealed class NetworkResponse<out T : Any, out U : Any> {

    /**
     * A request that resulted in a response with a 2xx status code that has a body.
     */
    data class Success<T : Any>(val body: T) : NetworkResponse<T, Nothing>()

    /**
     * A request that resulted in a response with a non-2xx status code.
     */
    data class ServerError<U : Any>(val body: U?, val code: Int) : NetworkResponse<Nothing, U>()

    /**
     * A request that didn't result in a response.
     */
    data class NetworkError(val error: IOException) : NetworkResponse<Nothing, Nothing>()
}