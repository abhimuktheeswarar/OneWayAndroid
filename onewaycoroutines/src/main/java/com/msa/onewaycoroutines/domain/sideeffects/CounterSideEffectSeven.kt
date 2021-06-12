package com.msa.onewaycoroutines.domain.sideeffects

import com.msa.core.Action
import com.msa.core.CoroutineDispatcherProvider
import com.msa.onewaycoroutines.base.seven.BaseSideEffectSeven
import com.msa.onewaycoroutines.base.seven.BaseStoreSeven
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Abhi Muktheeswarar on 12-June-2021.
 */

class CounterSideEffectSeven(
    store: BaseStoreSeven<*>,
    scope: CoroutineScope,
    dispatchers: CoroutineDispatcherProvider
) : BaseSideEffectSeven(store, scope, dispatchers) {

    override fun handle(action: Action) {

    }
}