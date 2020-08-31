package com.msa.oneway.core

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

interface ThreadExecutor {
    fun execute(block: () -> Unit)
}

abstract class ThreadExecutorService(private val executorService: Executor) : ThreadExecutor {
    override fun execute(block: () -> Unit) {
        executorService.execute { block() }
    }
}

class StoreThreadService : ThreadExecutorService(ExecutorServices.store)

class SideEffectThreadService : ThreadExecutorService(ExecutorServices.sideEffect)

object ExecutorServices {

    val store: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    val sideEffect: ExecutorService by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Executors.newWorkStealingPool()
        } else {
            Executors.newCachedThreadPool()
        }
    }
}

class MainThread(private val context: WeakReference<Context>) : ThreadExecutor {
    override fun execute(block: () -> Unit) {
        context.get()?.runOnUiThread { block() }
    }
}

fun Context.runOnUiThread(f: Context.() -> Unit) {
    if (Looper.getMainLooper() === Looper.myLooper()) {
        f()
    } else ContextHelper.handler.post { f() }
}

private object ContextHelper {
    val handler = Handler(Looper.getMainLooper())
}

class TestThreadExecutorService(executorService: Executor = CurrentThreadExecutor()) :
    ThreadExecutorService(executorService)

class CurrentThreadExecutor : Executor {

    override fun execute(r: Runnable) {
        r.run()
    }
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

@Suppress("PropertyName")
open class CoroutineDispatcherProvider(val coroutineContext: CoroutineContext) {

    open val Main: CoroutineContext by lazy { Dispatchers.Default }
    open val IO: CoroutineContext by lazy { Dispatchers.IO }
    open val Default: CoroutineContext by lazy { Dispatchers.Default }
    open val Unconfined: CoroutineContext by lazy { Dispatchers.Unconfined }
}


open class SchedulerProvider(scheduler: Scheduler) {

    open val main: Scheduler by lazy { scheduler }
    open val io: Scheduler by lazy { Schedulers.io() }
    open val computation: Scheduler by lazy { Schedulers.computation() }
}

