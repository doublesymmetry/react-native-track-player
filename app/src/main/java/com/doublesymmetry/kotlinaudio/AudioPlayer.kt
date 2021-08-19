package com.doublesymmetry.kotlinaudio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import java.util.concurrent.TimeUnit

open class AudioPlayer(private val context: Context) {
    protected val exoPlayer: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()

    private val handler = Handler(Looper.getMainLooper())

    protected val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "AudioPlayerSession")
    private val mediaSessionConnector: MediaSessionConnector = MediaSessionConnector(mediaSession)

    protected val manager = PlaybackNotificationManager(context)

    init {
        mediaSessionConnector.setPlayer(exoPlayer)
    }

    open fun load(item: AudioItem, playWhenReady: Boolean = true) {
        exoPlayer.playWhenReady = playWhenReady

        val mediaItem = MediaItem.fromUri(item.audioUrl)
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()

        val manager = PlaybackNotificationManager(context)
        val notification = manager.createNotification(mediaSession, item)
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
        exoPlayer.prepare()
        exoPlayer.play()

        mediaSession.isActive = true
    }

    fun pause() {
        exoPlayer.pause()
    }

    open fun stop() {
        exoPlayer.release()
        seek(23, TimeUnit.MINUTES)
    }

    fun seek(duration: Long, unit: TimeUnit) {
        val millis = TimeUnit.MILLISECONDS.convert(duration, unit)
        exoPlayer.seekTo(millis)
    }
}