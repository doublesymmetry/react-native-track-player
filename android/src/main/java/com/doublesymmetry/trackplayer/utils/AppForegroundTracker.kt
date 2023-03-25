package com.doublesymmetry.trackplayer.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.react.bridge.UiThreadUtil

object AppForegroundTracker {
    private var activityCount = 0

    val foregrounded: Boolean
        get() = activityCount > 0

    val backgrounded: Boolean
        get() = activityCount <= 0

    fun start() {
        UiThreadUtil.runOnUiThread {
            ProcessLifecycleOwner.get().lifecycle.addObserver(Observer)
        }
    }

    object Observer : DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            activityCount++
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            activityCount--
        }
    }
}
