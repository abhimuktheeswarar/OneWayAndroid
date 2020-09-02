package com.msa.oneway.core

import io.reactivex.Scheduler

/**
 * Created by Abhi Muktheeswarar on 31-August-2020
 */

class TestSchedulerProvider(scheduler: Scheduler) : SchedulerProvider(scheduler) {

    override val current: Scheduler by lazy { scheduler }
    override val io: Scheduler by lazy { scheduler }
    override val computation: Scheduler by lazy { scheduler }
    override val main: Scheduler by lazy { scheduler }
}