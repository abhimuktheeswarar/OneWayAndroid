package com.msa.onewaycoroutines.entities

import com.msa.core.Action

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

sealed interface CounterAction : Action {

    object IncrementAction : CounterAction

    object DecrementAction : CounterAction

    object ResetAction : CounterAction

    data class ForceUpdateAction(val count: Int) : CounterAction
}