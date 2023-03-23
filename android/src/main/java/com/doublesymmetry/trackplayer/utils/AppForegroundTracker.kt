package com.doublesymmetry.trackplayer.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

object AppForegroundTracker : LifecycleObserver {
    private var counter = 0

    val foregrounded: Boolean
        get() = counter > 0

    val backgrounded: Boolean
        get() = counter <= 0

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        counter++
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        counter--
    }
}
