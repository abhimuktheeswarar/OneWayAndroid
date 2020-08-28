package com.msa.oneway.core


/**
 * Created by Abhi Muktheeswarar on 19-August-2020
 */

interface Reducer<S : State> {

    fun reduce(action: Action, currentState: S): S
}