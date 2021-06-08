package com.msa.core

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

@Suppress("PropertyName")
open class CoroutineDispatcherProvider(val coroutineContext: CoroutineContext) {

    open val Current: CoroutineContext by lazy { coroutineContext }
    open val Main: CoroutineContext by lazy { Dispatchers.Main }
    open val IO: CoroutineContext by lazy { Dispatchers.IO }
    open val Default: CoroutineContext by lazy { Dispatchers.Default }
    open val Unconfined: CoroutineContext by lazy { Dispatchers.Unconfined }
}

object CoroutineScopeProvider {

    fun getOneWayViewModelCoroutineContext(coroutineContext: CoroutineContext = Dispatchers.Default) =
        CoroutineScope(
            SupervisorJob() + coroutineContext + CoroutineExceptionHandler { _, throwable ->
                if (throwable is CancellationException) {
                    throwable.printStackTrace()
                } else {
                    throw throwable
                }

            }).coroutineContext

}