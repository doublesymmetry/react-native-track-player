package com.guichaguri.trackplayer.service

import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import java.util.*

class MusicService : HeadlessJsTaskService() {
    private lateinit var player: QueuedAudioPlayer
    private val handler = Handler(Looper.getMainLooper())

    val queue: List<AudioItem>
        get() = player.items

    val currentIndex
        get() = player.currentIndex

    var repeatMode: QueuedAudioPlayer.RepeatMode
        get() = player.repeatMode
        set(value) {
            player.repeatMode = value
        }

    override fun onCreate() {
        handler.post { player = QueuedAudioPlayer(this) }
        super.onCreate()
    }

    fun setUp() {

    }

    fun add(items: List<AudioItem>, playWhenReady: Boolean = true) {
        handler.post { player.add(items, playWhenReady) }
    }

    fun remove(indexes: List<Int>) {
        handler.post { player.remove(indexes) }
    }

    fun play() {
        handler.post { player.play() }
    }

    fun pause() {
        handler.post { player.pause() }
    }

    fun stop() {
        handler.post { player.stop() }
    }

    fun removeUpcomingTracks() {
        handler.post { player.removeUpcomingItems() }
    }

    fun removePreviousTracks() {
        handler.post { player.removePreviousItems() }
    }

    fun skipToNext() {
        handler.post { player.next() }
    }

    fun skipToPrevious() {
       handler.post { player.previous() }
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

    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }
}