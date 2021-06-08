package com.msa.oneway.sample.domain.sideeffects

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.oneway.common.ResourceRepository
import com.msa.oneway.core.BaseCoroutineSideEffect
import com.msa.oneway.core.Store
import com.msa.oneway.core.ThreadExecutorService
import com.msa.oneway.sample.data.TodoRepository
import com.msa.oneway.sample.entities.TodoAction
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class GetTodoListCoroutineSideEffect(
    store: Store<*>,
    private val todoRepository: TodoRepository,
    private val resourceRepository: ResourceRepository,
    threadExecutorService: ThreadExecutorService,
    scope: CoroutineScope,
    coroutineDispatcherProvider: CoroutineDispatcherProvider
) : BaseCoroutineSideEffect(
    store,
    threadExecutorService,
    scope,
    coroutineDispatcherProvider
) {

    override fun handle(action: Action) {
        if (action !is TodoAction.GetTodoListCoroutineAction) {
            return
        }

        scope.launch(coroutineDispatcherProvider.IO + CoroutineExceptionHandler { _, throwable ->
            val exception = if (throwable is Exception) throwable else Exception(throwable)
            dispatch(TodoAction.ErrorLoadingTodoListAction(exception))
        }) {

            val todoResponse = todoRepository.getTodoListCoroutine()
            dispatch(TodoAction.TodoListLoadedAction(todoResponse))
        }
    }
}
