package com.msa.oneway.sample.ui

import android.content.Context
import com.msa.oneway.core.*
import com.msa.oneway.providers.RepositoryProvider
import com.msa.oneway.sample.domain.HomeStore
import com.msa.oneway.sample.domain.sideeffects.GetTodoListCoroutineSideEffect
import com.msa.oneway.sample.domain.sideeffects.GetTodoListRxSideEffect
import com.msa.oneway.sample.entities.HomeScreenState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class HomeViewModel(
    store: HomeStore,
    mainThread: ThreadExecutor,
    coroutineContext: CoroutineContext,
    compositeDisposable: CompositeDisposable
) : BaseOneWayViewModel<HomeScreenState>(store, mainThread, coroutineContext, compositeDisposable) {

    companion object {

        fun getHomeViewModel(context: Context): HomeViewModel {
            val todoRepository = RepositoryProvider.todoRepository
            val resourceRepository =
                RepositoryProvider.getResourceRepository(context.applicationContext)
            val store = HomeStore()

            val threadExecutorService = SideEffectThreadService()
            val mainThread = MainThread(WeakReference(context))
            val coroutineContext = CoroutineScopeProvider.getOneWayViewModelCoroutineContext()
            val coroutineDispatcherProvider = CoroutineDispatcherProvider(coroutineContext)
            val schedulerProvider = SchedulerProvider(Schedulers.from(ExecutorServices.sideEffect))
            val compositeDisposable = CompositeDisposable()

            GetTodoListRxSideEffect(
                store,
                todoRepository,
                resourceRepository,
                threadExecutorService,
                schedulerProvider,
                compositeDisposable
            )
            GetTodoListCoroutineSideEffect(
                store,
                todoRepository,
                resourceRepository,
                threadExecutorService,
                coroutineDispatcherProvider
            )

            return HomeViewModel(
                store,
                mainThread,
                coroutineDispatcherProvider.coroutineContext,
                compositeDisposable
            )
        }
    }
}