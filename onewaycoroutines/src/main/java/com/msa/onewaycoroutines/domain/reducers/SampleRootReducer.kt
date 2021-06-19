package com.msa.onewaycoroutines.domain.reducers

import com.msa.core.Action
import com.msa.core.State

/**
 * Created by Abhi Muktheeswarar on 19-June-2021.
 */

data class Todo(val id: Int, val content: String, val done: Boolean)

data class FilterState(val sortOrder: Int)

data class TodoScreenState(val todos: List<Todo>, val filterState: FilterState) : State

fun todosReducer(action: Action, state: List<Todo>): List<Todo> {
    return when (action) {

        else -> state
    }
}

fun filterReducer(action: Action, state: FilterState): FilterState {
    return when (action) {

        else -> state
    }
}

fun sampleRootReducer(action: Action, state: TodoScreenState): TodoScreenState = with(state) {
    copy(todos = todosReducer(action, todos), filterState = filterReducer(action, filterState))
}