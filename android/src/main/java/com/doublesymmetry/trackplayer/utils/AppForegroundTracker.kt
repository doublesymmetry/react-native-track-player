package com.doublesymmetry.trackplayer.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.react.bridge.UiThreadUtil

object AppForegroundTracker {
    private var activityCount = 0

    // With the New Architecture the module is created lazily, on first use from JS, after the
    // activity has resumed. The observer registered in start() is added on the UI thread later,
    // so activityCount can still be 0 when setupPlayer() runs. Read the process state as well.
    val foregrounded: Boolean
        get() = activityCount > 0 ||
            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

    val backgrounded: Boolean
        get() = !foregrounded

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
