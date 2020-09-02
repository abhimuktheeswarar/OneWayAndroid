package com.msa.oneway.sample.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.msa.oneway.R
import com.msa.oneway.common.ShowToastAction
import com.msa.oneway.core.Action
import com.msa.oneway.core.EventAction
import com.msa.oneway.core.NavigateAction
import com.msa.oneway.providers.BaseViewModelFactory
import com.msa.oneway.sample.entities.HomeScreenState
import com.msa.oneway.sample.entities.TodoAction
import kotlinx.android.synthetic.main.activity_home.*

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class HomeActivity : AppCompatActivity() {

    private val tag = "HomeActivity"

    private val viewModel by viewModels<HomeViewModel> {
        BaseViewModelFactory {
            HomeViewModel.getHomeViewModel(this)
        }
    }

    private val dispatch: (action: Action) -> Unit = {
        viewModel.dispatch(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel.state.observe(this, Observer { state ->
            setupViews(state)
        })

        viewModel.eventLiveData.observe(this, Observer { action ->
            handleEvent(action)
        })

        viewModel.navigateLiveData.observe(this, Observer { action ->
            handleNavigate(action)
        })

        /*viewModel.state.value?.countLiveData?.observe(this, Observer { count ->
            Log.d(tag, "count = $count")
        })*/

        dispatch(TodoAction.GetTodoListRxAction)
        dispatch(ShowToastAction("Welcome to OneWay"))
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews(state: HomeScreenState) {
        Log.d(tag, "Todo's size = ${state.todoResponse?.size}")
        text_state.text = "count = ${state.todoResponse?.size}"
    }

    private fun handleEvent(action: EventAction) {
        when (action) {

            is ShowToastAction -> {
                Toast.makeText(this, action.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleNavigate(action: NavigateAction) {
        when (action) {

            is TodoAction.OpenTodoDetailScreenAction -> {
                TODO()
            }
        }
    }
}
