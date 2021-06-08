package com.msa.oneway.sample.entities

import com.msa.core.Action
import com.msa.core.NavigateAction


/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */
sealed class TodoAction : Action {

    object GetTodoListRxAction : TodoAction()

    object GetTodoListCoroutineAction : TodoAction()

    data class TodoListLoadedAction(val todoResponse: TodoResponse) : TodoAction()

    data class ErrorLoadingTodoListAction(val exception: Exception) : TodoAction()

    data class OpenTodoDetailScreenAction(val id: String) : TodoAction(), NavigateAction

    object UpdateCountAction : TodoAction()

    data class SetCountAction(val count: Int) : TodoAction()
}