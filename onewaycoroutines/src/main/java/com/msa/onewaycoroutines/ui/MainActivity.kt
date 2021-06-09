package com.msa.onewaycoroutines.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msa.core.EventAction
import com.msa.onewaycoroutines.base.BaseViewModelFactory
import com.msa.onewaycoroutines.common.ShowToastAction
import com.msa.onewaycoroutines.databinding.ActivityMainBinding
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.ui.viewmodels.CounterReducerFive
import com.msa.onewaycoroutines.ui.viewmodels.CounterViewModelFive
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    /*private val viewModel by viewModels<CounterViewModel> {
        BaseViewModelFactory {
            CounterViewModel.getViewModel()
        }
    }*/

    private val viewModel by viewModels<CounterViewModelFive> {
        BaseViewModelFactory {
            CounterViewModelFive(CounterReducerFive)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonDecrement.setOnClickListener {
            viewModel.dispatch(CounterAction.DecrementAction)
        }

        binding.buttonIncrement.setOnClickListener {
            viewModel.dispatch(CounterAction.IncrementAction)
        }

        binding.buttonReset.setOnClickListener {
            Log.d(TAG, "Current state = ${viewModel.state()}")
            viewModel.dispatch(CounterAction.ResetAction)
        }

        binding.buttonShowToast.setOnClickListener {
            viewModel.dispatch(ShowToastAction("${System.currentTimeMillis()}"))
        }


        lifecycleScope.launchWhenCreated {
            viewModel.states.collect(::setupViews)
        }

        lifecycleScope.launchWhenResumed {
            viewModel.eventActions.collect(::processEvents)
        }
    }

    private fun setupViews(state: com.msa.onewaycoroutines.entities.CounterState) {
        Log.d(TAG, "setupViews = $state | ${viewModel.state()}")
        binding.textCount.text = state.counter.toString()
    }

    private fun processEvents(action: EventAction) {
        when (action) {

            is ShowToastAction -> {
                Toast.makeText(this, action.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}