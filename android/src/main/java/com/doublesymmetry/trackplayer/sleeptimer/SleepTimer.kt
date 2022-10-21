package com.doublesymmetry.trackplayer.sleeptimer

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

open class SleepTimer {
    public var time: Double? = null
    private var runnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    val isRunning: Boolean
        get() = time != null

    fun sleepAfter(seconds: Double) {
        stopTimer()
        val runnable = Runnable {
            complete()
        }
        this.runnable = runnable
        Handler(Looper.getMainLooper()).postDelayed(runnable, seconds.toLong() * 1000)
        time = System.currentTimeMillis() + (seconds * 1000)
    }

    fun clear(): Boolean {
        val wasRunning = isRunning
        stopTimer()
        time = null
        return wasRunning
    }

    open fun onComplete() {
        // noop
    }

    private fun complete() {
        if (!isRunning) return;
        clear()
        onComplete()
    }

    private fun stopTimer() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = null
    }
}
