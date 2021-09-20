package com.guichaguri.trackplayer.service

import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.MainThread
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import java.util.*

class MusicService: HeadlessJsTaskService() {
    private lateinit var player: QueuedAudioPlayer

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        handler.post {
            player = QueuedAudioPlayer(this)
        }
        super.onCreate()
    }

    fun setUp() {

    }

    fun add(items: List<AudioItem>, playWhenReady: Boolean = true) {
        handler.post {
            player.add(items, playWhenReady)
        }
    }

    @MainThread
    fun play() {
        handler.post {
            player.play()
        }
    }
//
    fun pause() {

    }

    fun destroy() {

    }

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig {
        return HeadlessJsTaskConfig("TrackPlayer", Arguments.createMap(), 0, true)
    }

    override fun onHeadlessJsTaskFinish(taskId: Int) {
        // Overridden to prevent the service from being terminated
    }

    override fun onBind(intent: Intent?): IBinder {
        return MusicBinder()
    }

    inner class MusicBinder: Binder() {
        val service = this@MusicService
    }
}