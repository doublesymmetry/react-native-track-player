package com.doublesymmetry.trackplayer.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.react.bridge.UiThreadUtil
import java.util.concurrent.ConcurrentLinkedQueue

object AppForegroundTracker {
    private var counter = 0
    private val foregroundCallbacks = ConcurrentLinkedQueue<() -> Unit>()

    val foregrounded: Boolean
        get() = counter > 0

    val backgrounded: Boolean
        get() = counter <= 0

    fun start() {
        UiThreadUtil.runOnUiThread {
            ProcessLifecycleOwner.get().lifecycle.addObserver(Observer)
        }
    }

    fun onResume(block: () -> Unit) {
        foregroundCallbacks.add(block)
    }

    object Observer : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            counter++
            println("Lifecycle.Event.ON_START --> counter: " + counter)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            counter--
            println("Lifecycle.Event.ON_STOP --> counter: " + counter)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            while (!foregroundCallbacks.isEmpty()) {
                foregroundCallbacks.poll()?.invoke()
            }
        }
    }
}
