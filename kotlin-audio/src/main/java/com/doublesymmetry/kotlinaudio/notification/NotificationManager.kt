package com.doublesymmetry.kotlinaudio.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import com.doublesymmetry.kotlinaudio.R
import com.doublesymmetry.kotlinaudio.models.NotificationAction
import com.doublesymmetry.kotlinaudio.models.NotificationActionType
import com.doublesymmetry.kotlinaudio.utils.isJUnitTest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow

internal class NotificationManager(private val context: Context, private val exoPlayer: ExoPlayer) : PlayerNotificationManager.PrimaryActionReceiver {
    //    private var playerNotificationManager: PlayerNotificationManager
    private val descriptionAdapter = DescriptionAdapter(context, null)
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "AudioPlayerSession")
    private val mediaSessionConnector: MediaSessionConnector = MediaSessionConnector(mediaSession)

    val onCustomAction = MutableSharedFlow<NotificationActionType>()

    private val scope = CoroutineScope(Dispatchers.Main)

    val actions = mutableListOf<NotificationAction?>()

    private val channelId: String

    lateinit var playerNotificationManager: PlayerNotificationManager

//    private val builder by lazy {
//
//
//        PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)
//            .setMediaDescriptionAdapter(descriptionAdapter)
//            .setCustomActionReceiver(this)
//    }

    init {
        channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        mediaSessionConnector.setPlayer(exoPlayer)

        playerNotificationManager = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setPrimaryActionReceiver(this)
            .build()

        if (!isJUnitTest()) {
            playerNotificationManager.apply {
                setPlayer(exoPlayer)
                setMediaSessionToken(mediaSession.sessionToken)
//                setUsePlayPauseActions(false)
                setUseFastForwardAction(false)
                setUseNextAction(false)
                setUsePreviousAction(false)
                setUseStopAction(false)
                setUseRewindAction(false)
            }
        }
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

    fun createNotification() {


//        playerNotificationManager.setUseNextAction()
    }

    override fun onAction(player: Player, action: String, intent: Intent) {
        Log.d("test", action)
    }

    fun onPlay() {
        mediaSession.isActive = true
    }

    fun refresh() {
        createNotification()
    }

    fun destroy() {
        descriptionAdapter.release()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "kotlin_audio_player"
    }
}