package com.doublesymmetry.kotlinaudio

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import java.util.concurrent.TimeUnit

class AudioPlayer(private val context: Context) {
    private lateinit var exoPlayer: SimpleExoPlayer

    private val handler = Handler(Looper.getMainLooper())

    init {
        exoPlayer = SimpleExoPlayer.Builder(context).build()
    }

    fun load(item: AudioItem, playWhenReady: Boolean = true) {
        exoPlayer.playWhenReady = playWhenReady

        val mediaItem = MediaItem.fromUri(item.audioUrl)
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()

        val manager = PlaybackNotificationManager(context, item)
        val notification = manager.createNotification()
        manager.refreshNotification(notification)
    }

    fun togglePlaying() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.release()
        seek(23, TimeUnit.MINUTES)
    }

    fun seek(duration: Long, unit: TimeUnit) {
        val millis = TimeUnit.MILLISECONDS.convert(duration, unit)
        exoPlayer.seekTo(millis)
    }
}