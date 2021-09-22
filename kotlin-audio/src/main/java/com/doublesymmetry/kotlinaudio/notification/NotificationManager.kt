package com.doublesymmetry.kotlinaudio.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
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
import kotlinx.coroutines.launch

internal class NotificationManager(private val context: Context, private val exoPlayer: ExoPlayer) : PlayerNotificationManager.CustomActionReceiver {
    private val playerNotificationManager: PlayerNotificationManager
    private val descriptionAdapter = DescriptionAdapter(context, null)
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "AudioPlayerSession")
    private val mediaSessionConnector: MediaSessionConnector = MediaSessionConnector(mediaSession)

    val onCustomAction = MutableSharedFlow<NotificationActionType>()

    private val scope = CoroutineScope(Dispatchers.Main)

    var actions = listOf<NotificationAction>()

    init {


//        val builder = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)

//        playerNotificationManager = builder
//            .setMediaDescriptionAdapter(descriptionAdapter)
//            .setCustomActionReceiver(this)
//            .build()

//        playerNotificationManager = builder.build()
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

    private val builder by lazy {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setCustomActionReceiver(this)
    }

    fun createNotification() {
        mediaSessionConnector.setPlayer(exoPlayer)

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

//        refresh()
    }

    //    private fun updateOptions(options: Bundle) {
//        val capabilities: List<Int>? = options.getIntegerArrayList("capabilities")
//        val notification: List<Int>? = options.getIntegerArrayList("notificationCapabilities")
//        val compact: List<Int>? = options.getIntegerArrayList("compactCapabilities")
//    }

//    override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
//        val intentShuffleOn: Intent = Intent(name).setPackage(context.packageName)
//        val action = NotificationCompat.Action(android.R.drawable.btn_radio, "closeBar", PendingIntent.getBroadcast(context, 123, intentShuffleOn, PendingIntent.FLAG_CANCEL_CURRENT))
//        val actionMap: MutableMap<String, NotificationCompat.Action> = HashMap()
//        actionMap[name] = action
//        return actionMap
//    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
//        return mutableMapOf(PlayerNotificationManager.ACTION_PAUSE to NotificationCompat.Action(R.drawable.exo_notification_small_icon, "Test", null))
        return actions.associate {
            val intent = Intent(it.type.value).setPackage(context.packageName)
            val action = NotificationCompat.Action(it.icon, it.title, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
            it.type.value to action
        }.toMutableMap()
//        return actions.filter { it.type == NotificationActionType.CUSTOM }.associate { it.type.value to it.action }.toMutableMap()
    }

    override fun getCustomActions(player: Player): MutableList<String> {
        return actions.map { it.type.value }.toMutableList()
//        return mutableListOf(PlayerNotificationManager.ACTION_PAUSE)
    }

    override fun onCustomAction(player: Player, action: String, intent: Intent) {
        scope.launch {
            onCustomAction.emit(NotificationActionType.valueOf(action))
        }
    }

//    override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
//        val action = NotificationCompat.Action(android.R.drawable.btn_radio, "closeBar", PendingIntent.getBroadcast(context, 123, Intent().setPackage(context.packageName), PendingIntent.FLAG_CANCEL_CURRENT))
//        val actionMap: MutableMap<String, NotificationCompat.Action> = HashMap()
//        actionMap["test"] = action
//        return actionMap
//    }
//
//    override fun getCustomActions(player: Player): List<String> {
//        val customActions: MutableList<String> = ArrayList()
//        customActions.add("test")
//        return customActions
//    }
//
//    override fun onCustomAction(player: Player, action: String, intent: Intent) {
//        scope.launch {
//            onCustomAction.emit(NotificationActionType.valueOf(action))
//        }
//        Timber.d("action: $action")
//    }

//    override fun createCustomActions(context: Context, instanceId: Int): MutableMap<String, NotificationCompat.Action> {
//        val intentShuffleOn: Intent = Intent(name).setPackage(context.packageName)
//        val action = NotificationCompat.Action(android.R.drawable.btn_radio, "closeBar", PendingIntent.getBroadcast(context, 123, intentShuffleOn, PendingIntent.FLAG_CANCEL_CURRENT))
//        val actionMap: MutableMap<String, NotificationCompat.Action> = HashMap()
//        actionMap[name] = action
//        return actionMap
//    }
//
//    override fun getCustomActions(player: Player): List<String> {
//        val customActions: MutableList<String> = ArrayList()
//        customActions.add(name)
//        return customActions
//    }
//
//    override fun onCustomAction(player: Player, action: String, intent: Intent) {
//        scope.launch {
//            onCustomAction.emit(NotificationActionType.valueOf(action))
//        }
//        Timber.d("action: $action")
//    }

    fun onPlay() {
        mediaSession.isActive = true
    }

    fun refresh() {
        playerNotificationManager.invalidate()
        createNotification()
        playerNotificationManager.invalidate()
    }

    fun destroy() {
        descriptionAdapter.release()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "kotlin_audio_player"
    }
}