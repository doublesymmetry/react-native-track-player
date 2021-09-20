package com.doublesymmetry.kotlinaudio.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import com.doublesymmetry.kotlinaudio.R
import com.doublesymmetry.kotlinaudio.utils.isJUnitTest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class NotificationManager(private val context: Context) {
    private val playerNotificationManager: PlayerNotificationManager
    private val descriptionAdapter = DescriptionAdapter(context, null)
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "AudioPlayerSession")
    private val mediaSessionConnector: MediaSessionConnector = MediaSessionConnector(mediaSession)

    init {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        val builder = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)

        playerNotificationManager = builder
            .setMediaDescriptionAdapter(descriptionAdapter)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = CHANNEL_ID
        val channelName = context.getString(R.string.playback_channel_name)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        channel.description = "Used when playing music"
        channel.setSound(null, null)

        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    fun createNotification(exoPlayer: ExoPlayer) {
        mediaSessionConnector.setPlayer(exoPlayer)

        if (!isJUnitTest()) {
            playerNotificationManager.apply {
                setPlayer(exoPlayer)
                setMediaSessionToken(mediaSession.sessionToken)
                setUseNextActionInCompactView(true)
                setUsePreviousActionInCompactView(true)
            }
        }
    }

    fun onPlay() {
        mediaSession.isActive = true
    }

    fun destroy() {
        descriptionAdapter.release()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "kotlin_audio_player"
    }
}