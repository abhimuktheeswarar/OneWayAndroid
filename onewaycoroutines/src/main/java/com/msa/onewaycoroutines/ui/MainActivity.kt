package com.msa.onewaycoroutines.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.msa.onewaycoroutines.base.BaseViewModelFactory
import com.msa.onewaycoroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<CounterViewModel> {
        BaseViewModelFactory {
            CounterViewModel.getViewModel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonDecrement.setOnClickListener {
            viewModel.dispatch(com.msa.onewaycoroutines.entities.CounterAction.DecrementAction)
        }

        binding.buttonIncrement.setOnClickListener {
            viewModel.dispatch(com.msa.onewaycoroutines.entities.CounterAction.IncrementAction)
        }

        binding.buttonReset.setOnClickListener {
            viewModel.dispatch(com.msa.onewaycoroutines.entities.CounterAction.ResetAction)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.state.collect { setupViews(it) }
        }
    }

    private fun setupViews(state: com.msa.onewaycoroutines.entities.CounterState) {
        //Log.d(tag, "setupViews = $state")
        binding.textCount.text = state.counter.toString()
    }
}