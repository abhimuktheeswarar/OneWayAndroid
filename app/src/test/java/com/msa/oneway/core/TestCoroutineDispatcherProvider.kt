package com.msa.oneway.core


import com.msa.core.CoroutineDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

@ExperimentalCoroutinesApi
class TestCoroutineDispatcherProvider(coroutineContext: CoroutineContext) :
    CoroutineDispatcherProvider(coroutineContext) {

    override val Current: CoroutineContext = coroutineContext
    override val Main: CoroutineContext = coroutineContext
    override val IO: CoroutineContext = coroutineContext
    override val Default: CoroutineContext = coroutineContext
    override val Unconfined: CoroutineContext = coroutineContext
}