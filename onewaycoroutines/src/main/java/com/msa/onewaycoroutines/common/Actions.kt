package com.msa.onewaycoroutines.common

import com.msa.core.ErrorAction
import com.msa.core.EventAction
import com.msa.core.SkipReducer
import com.msa.onewaycoroutines.base.ExceededTimeLimitToComputeNewStatException

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

data class ShowToastAction(val message: String) : EventAction, SkipReducer

data class StoreReducerExceededTimeLimitAction(override val exception: ExceededTimeLimitToComputeNewStatException) :
    ErrorAction
