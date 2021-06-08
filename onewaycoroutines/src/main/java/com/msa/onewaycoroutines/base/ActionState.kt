package com.msa.onewaycoroutines.base

import com.msa.core.Action
import com.msa.core.State

/**
 * Created by Abhi Muktheeswarar on 07-June-2021.
 */

data class ActionState<out STATE : State>(
    val action: Action,
    val state: STATE
) {

    /**
     * Returns string representation of the [ActionState] including its [action] and [state] values.
     */
    override fun toString(): String = "($action, $state)"
}