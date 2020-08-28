package com.msa.oneway.sample.domain.sideeffects

import com.msa.oneway.common.ResourceRepository
import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.core.*
import com.msa.oneway.sample.data.TodoRepository
import com.msa.oneway.sample.entities.TodoAction
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class GetTodoListRxSideEffect(
    store: Store<*>,
    private val todoRepository: TodoRepository,
    private val resourceRepository: ResourceRepository,
    private val scheduler: Scheduler,
    private val threadExecutorService: ThreadExecutorService,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    compositeDisposable: CompositeDisposable
) : BaseSideEffect(store, threadExecutorService, coroutineDispatcherProvider, compositeDisposable) {

    override fun handle(action: Action) {
        if (action !is TodoAction.GetTodoListRxAction) {
            return
        }

        addDisposable(todoRepository.getTodoListRx()
            .subscribeOn(scheduler)
            .map { response ->

                when (response) {
                    is NetworkResponse.Success -> {
                        Result.success(response.body)
                    }
                    is NetworkResponse.ServerError -> {
                        val exception =
                            if (response.body is Exception) response.body else Exception(
                                response.body
                            )
                        Result.failure(exception)
                    }
                    is NetworkResponse.NetworkError -> {
                        Result.failure(response.error)
                    }
                }
            }
            .onErrorReturn { Result.failure(it) }
            .observeOn(Schedulers.from(threadExecutorService.executorService))
            .subscribe { result ->

                result.fold({ response ->

                    dispatch(TodoAction.TodoListLoadedAction(response))
                }, { error ->

                    val exception = if (error is Exception) error else Exception(error)
                    dispatch(TodoAction.ErrorLoadingTodoListAction(exception))

                    error.printStackTrace()
                })
            })
    }
}
