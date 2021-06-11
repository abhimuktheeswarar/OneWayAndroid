package com.msa.onewaycoroutines.entities

import com.msa.core.State

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

data class CounterState(val counter: Int = 0) : State
