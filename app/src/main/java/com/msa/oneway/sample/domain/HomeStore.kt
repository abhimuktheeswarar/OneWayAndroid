package com.msa.oneway.sample.domain

import android.util.Log
import com.msa.oneway.core.Action
import com.msa.oneway.core.Store
import com.msa.oneway.core.StoreThreadService
import com.msa.oneway.core.ThreadExecutor
import com.msa.oneway.sample.entities.HomeScreenState
import com.msa.oneway.sample.entities.TodoAction

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class HomeStore(
    initialState: HomeScreenState = HomeScreenState(),
    storeThread: ThreadExecutor? = StoreThreadService()
) : Store<HomeScreenState>(
    initialState = initialState,
    storeThread = storeThread,
    logger = { tag, message -> Log.d(tag, message) }) {

    override fun reduce(action: Action, currentState: HomeScreenState): HomeScreenState {

        return when (action) {

            is TodoAction.GetTodoListRxAction,
            is TodoAction.GetTodoListCoroutineAction -> currentState.copy(
                loading = true,
                todoResponse = null,
                exception = null
            )

            is TodoAction.TodoListLoadedAction -> currentState.copy(
                loading = false,
                todoResponse = action.todoResponse,
                exception = null
            )

            is TodoAction.ErrorLoadingTodoListAction -> currentState.copy(
                loading = false,
                exception = action.exception
            )

            is TodoAction.UpdateCountAction -> {
                currentState.copy(count = currentState.count + 1)
            }

            is TodoAction.SetCountAction -> {
                currentState.copy(count = action.count)
            }

            /*is ShowToastAction -> {
                val mutableLiveData = currentState.countLiveData as MutableLiveData
                mutableLiveData.postValue(currentState.countLiveData.value!! + 1)
                currentState
            }*/

            else -> currentState
        }
    }
}