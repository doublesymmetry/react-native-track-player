package com.doublesymmetry.kotlinaudio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import java.util.concurrent.TimeUnit

open class AudioPlayer(private val context: Context) {
    protected val exoPlayer: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()

    private val handler = Handler(Looper.getMainLooper())

    protected val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "AudioPlayerSession")
    private val mediaSessionConnector: MediaSessionConnector = MediaSessionConnector(mediaSession)

    protected val manager = PlaybackNotificationManager(context)

    private val playerNotificationManager: PlayerNotificationManager

    init {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

//        mediaSessionConnector.setPlayer(exoPlayer)
        val builder = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)
        playerNotificationManager = builder.build()
        playerNotificationManager.setPlayer(exoPlayer)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = PlaybackNotificationManager.CHANNEL_ID
        val channelName = context
            .getString(R.string.playback_channel_name)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        channel.description = "Used when playing music"
        channel.setSound(null, null)

        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }


    companion object {
        private const val MEDIA_SESSION_TAG = "one_beat_media_session"
        private const val NO_DURATION = -1L
        private const val CHANNEL_ID = "onebeat_playback"
        const val NOTIFICATION_ID = 1
    }


    open fun load(item: AudioItem, playWhenReady: Boolean = true) {
        exoPlayer.playWhenReady = playWhenReady

        val mediaItem = MediaItem.fromUri(item.audioUrl)
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()


//        val manager = PlaybackNotificationManager(context)
//        val notification = manager.createNotification(mediaSession, item)
//        manager.refreshNotification(notification)
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