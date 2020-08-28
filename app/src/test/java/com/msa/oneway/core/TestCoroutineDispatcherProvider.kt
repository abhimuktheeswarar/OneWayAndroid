package com.msa.oneway.core


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

@ExperimentalCoroutinesApi
class TestCoroutineDispatcherProvider(coroutineContext: CoroutineContext) :
    CoroutineDispatcherProvider(coroutineContext) {

    override val Main: CoroutineContext = TestCoroutineDispatcher()
    override val IO: CoroutineContext = TestCoroutineDispatcher()
    override val Default: CoroutineContext = TestCoroutineDispatcher()
    override val Unconfined: CoroutineContext = TestCoroutineDispatcher()
}