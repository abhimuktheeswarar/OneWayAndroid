package com.msa.oneway.sample.counter

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msa.core.EventAction
import com.msa.core.name
import com.msa.oneway.R
import com.msa.oneway.common.ShowToastAction
import com.msa.oneway.providers.BaseViewModelFactory
import kotlinx.android.synthetic.main.activity_counter.*
import kotlinx.coroutines.launch

class CounterActivity : AppCompatActivity() {

    @Suppress("PrivatePropertyName")
    private val TAG: String = javaClass.simpleName

    private val viewModel by viewModels<CounterViewModel> {
        BaseViewModelFactory {
            CounterViewModel.get(this)
        }
    }

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counter)

        button_decrement.setOnClickListener {
            //viewModel.dispatch(CounterAction.DecrementAction)
            lifecycleScope.launch {
                repeat(25) {
                    viewModel.dispatch(CounterAction.DecrementAction)
                }
            }
        }

        button_increment.setOnClickListener {
            //viewModel.dispatch(CounterAction.IncrementAction)
            lifecycleScope.launch {
                repeat(25) {
                    viewModel.dispatch(CounterAction.IncrementAction)
                }
            }
        }

        button_reset.setOnClickListener {
            viewModel.dispatch(CounterAction.ResetAction)
            count = 0
        }

        button_show_toast.setOnClickListener {
            viewModel.dispatch(ShowToastAction("${System.currentTimeMillis()}"))
        }

        viewModel.state.observe(this, ::setupViews)
        viewModel.eventLiveData.observe(this, ::processEvents)
    }

    private fun setupViews(state: CounterState) {
        Log.d(TAG, "$count setupViews = ${state.counter}}")
        count++
        text_count.text = state.counter.toString()
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