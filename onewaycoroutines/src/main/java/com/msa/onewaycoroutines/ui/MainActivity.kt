package com.msa.onewaycoroutines.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msa.core.EventAction
import com.msa.core.name
import com.msa.onewaycoroutines.base.BaseViewModelFactory
import com.msa.onewaycoroutines.common.ShowToastAction
import com.msa.onewaycoroutines.databinding.ActivityMainBinding
import com.msa.onewaycoroutines.entities.CounterAction
import com.msa.onewaycoroutines.entities.CounterState
import com.msa.onewaycoroutines.ui.viewmodels.CounterViewModelSeven
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    /*private val viewModel by viewModels<CounterViewModel> {
        BaseViewModelFactory {
            CounterViewModel.getViewModel()
        }
    }*/

    var counter = 0

    /*private val viewModel by viewModels<CounterViewModelFive> {
        BaseViewModelFactory {
            CounterViewModelFive()
        }
    }*/

    private val viewModel by viewModels<CounterViewModelSeven> {
        BaseViewModelFactory {
            CounterViewModelSeven()
        }
    }
    private val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonDecrement.setOnClickListener {
            viewModel.dispatch(CounterAction.DecrementAction)
            /*lifecycleScope.launch {
                repeat(25) {
                    viewModel.dispatch(CounterAction.DecrementAction)
                }
            }*/
        }

        binding.buttonIncrement.setOnClickListener {
            //viewModel.dispatch(CounterAction.IncrementAction)
            scope.launch {
                repeat(25) {
                    viewModel.dispatch(CounterAction.IncrementAction)
                    viewModel.dispatch(CounterAction.ForceUpdateAction(viewModel.getState().counter - 1))
                }
            }
        }

        binding.buttonReset.setOnClickListener {
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

    private fun setupViews(state: CounterState) {
        //Log.d(TAG, "$counter setupViews = ${state.counter}")
        counter++
        binding.textCount.text = state.counter.toString()
    }

    private fun processEvents(action: EventAction) {
        Log.d(TAG, "processEvents = ${action.name()}")
        when (action) {

            is ShowToastAction -> {
                Toast.makeText(this, action.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}