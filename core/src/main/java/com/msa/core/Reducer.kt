package com.msa.core

/**
 * Created by Abhi Muktheeswarar on 09-June-2021.
 */

interface Reducer<S : State> {

    fun reduce(action: Action, state: S): S
}