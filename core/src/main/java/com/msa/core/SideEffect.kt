package com.msa.core

/**
 * Created by Abhi Muktheeswarar on 09-June-2021.
 */

interface SideEffect {

    fun handle(action: Action)
}