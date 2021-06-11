package com.msa.onewaycoroutines.common

import com.msa.core.EventAction
import com.msa.core.SkipReducer

/**
 * Created by Abhi Muktheeswarar on 10-June-2021.
 */

data class ShowToastAction(val message: String) : EventAction, SkipReducer