package com.msa.oneway.sample.domain.sideeffects

import com.msa.oneway.common.ResourceRepository
import com.msa.oneway.core.*
import com.msa.oneway.sample.data.TodoRepository
import com.msa.oneway.sample.entities.TodoAction
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class GetTodoListCoroutineSideEffect(
    store: Store<*>,
    private val todoRepository: TodoRepository,
    private val resourceRepository: ResourceRepository,
    private val scheduler: Scheduler,
    threadExecutorService: ThreadExecutorService,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    compositeDisposable: CompositeDisposable
) : BaseSideEffect(store, threadExecutorService, coroutineDispatcherProvider, compositeDisposable) {

    override fun handle(action: Action) {
        if (action !is TodoAction.GetTodoListCoroutineAction) {
            return
        }

        launch(coroutineDispatcherProvider.IO + CoroutineExceptionHandler { _, throwable ->
            val exception = if (throwable is Exception) throwable else Exception(throwable)
            dispatch(TodoAction.ErrorLoadingTodoListAction(exception))
        }) {

            val todoResponse = todoRepository.getTodoListCoroutine()
            dispatch(TodoAction.TodoListLoadedAction(todoResponse))
        }
    }
}
