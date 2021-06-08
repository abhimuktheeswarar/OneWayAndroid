package com.msa.oneway.sample.domain.sideeffects

import com.msa.core.Action
import com.msa.oneway.common.ResourceRepository
import com.msa.oneway.common.network.NetworkResponse
import com.msa.oneway.core.BaseRxSideEffect
import com.msa.oneway.core.SchedulerProvider
import com.msa.oneway.core.Store
import com.msa.oneway.core.ThreadExecutorService
import com.msa.oneway.sample.data.TodoRepository
import com.msa.oneway.sample.entities.TodoAction
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class GetTodoListRxSideEffect(
    store: Store<*>,
    private val todoRepository: TodoRepository,
    private val resourceRepository: ResourceRepository,
    threadExecutorService: ThreadExecutorService,
    schedulerProvider: SchedulerProvider,
    compositeDisposable: CompositeDisposable
) : BaseRxSideEffect(
    store,
    threadExecutorService,
    schedulerProvider,
    compositeDisposable
) {

    override fun handle(action: Action) {
        if (action !is TodoAction.GetTodoListRxAction) {
            return
        }

        addDisposable(todoRepository.getTodoListRx()
            .subscribeOn(schedulerProvider.current)
            .observeOn(schedulerProvider.current)
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
