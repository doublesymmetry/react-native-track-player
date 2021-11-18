package com.doublesymmetry.trackplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.os.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.models.NotificationButton.*
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.model.TrackAudioItem
import com.doublesymmetry.trackplayer.module.MusicEvents
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.utils.Utils
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class MusicService : HeadlessJsTaskService() {
    private lateinit var player: QueuedAudioPlayer
    private val handler = Handler(Looper.getMainLooper())
    var stopWithApp = false

    private val serviceScope = MainScope()

    val tracks: List<Track>
        get() = player.items.map { (it as TrackAudioItem).track }

    val currentTrack
        get() = (player.currentItem as TrackAudioItem).track

    var repeatMode: RepeatMode
        get() = player.playerOptions.repeatMode
        set(value) {
            handler.post {
                player.playerOptions.repeatMode = value
            }
        }

    val event get() = player.event

    private var capabilities: List<Capability> = emptyList()
    private var notificationCapabilities: List<Capability> = emptyList()
    private var compactCapabilities: List<Capability> = emptyList()

    fun setupPlayer(playerOptions: Bundle?, promise: Promise?) {
        val bufferOptions = BufferConfig(
            playerOptions?.getDouble(MIN_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
            playerOptions?.getDouble(MAX_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
            playerOptions?.getDouble(PLAY_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
            playerOptions?.getDouble(BACK_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
            // TODO: Ignored autoUpdateMetadata. Do we need them?
        )

        val cacheOptions = CacheConfig(
            playerOptions?.getDouble(MAX_CACHE_SIZE_KEY)?.toLong()
        )

        handler.post {
            player = QueuedAudioPlayer(this, bufferOptions, cacheOptions)
            observeEvents()
            promise?.resolve(null)
        }
    }

    fun updateOptions(options: Bundle) {
        handler.post {
            stopWithApp = options.getBoolean(STOP_WITH_APP_KEY)

            player.playerOptions.alwaysPauseOnInterruption = options.getBoolean(PAUSE_ON_INTERRUPTION_KEY)

            capabilities = options.getIntegerArrayList("capabilities")?.map { Capability.values()[it] } ?: emptyList()
            notificationCapabilities = options.getIntegerArrayList("notificationCapabilities")?.map { Capability.values()[it] } ?: emptyList()
            compactCapabilities = options.getIntegerArrayList("compactCapabilities")?.map { Capability.values()[it] } ?: emptyList()

            if (notificationCapabilities.isEmpty()) notificationCapabilities = capabilities

            val buttonsList = mutableListOf<NotificationButton>()

            notificationCapabilities.forEach {
                when (it) {
                    Capability.PLAY -> buttonsList.add(PLAY())
                    Capability.PAUSE -> buttonsList.add(PAUSE())
                    Capability.STOP -> buttonsList.add(STOP())
                    Capability.SKIP_TO_NEXT -> buttonsList.add(NEXT(isCompact = isCompact(it)))
                    Capability.SKIP_TO_PREVIOUS -> buttonsList.add(PREVIOUS(isCompact = isCompact(it)))
                    Capability.JUMP_FORWARD -> buttonsList.add(FORWARD(isCompact = isCompact(it)))
                    Capability.JUMP_BACKWARD -> buttonsList.add(BACKWARD(isCompact = isCompact(it)))
                    else -> return@forEach
                }
            }

            val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, getPendingIntentFlags())

            val notificationConfig = NotificationConfig(buttonsList, pendingIntent)

            player.notificationManager.createNotification(notificationConfig)
        }
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT } else { PendingIntent.FLAG_CANCEL_CURRENT }
    }

    private fun isCompact(capability: Capability): Boolean {
        return compactCapabilities.contains(capability)
    }

    fun add(tracks: List<Track>) {
        val items = tracks.map { it.toAudioItem() }
        handler.post { player.add(items, false) }
    }

    fun add(tracks: List<Track>, atIndex: Int) {
        val items = tracks.map { it.toAudioItem() }
        handler.post { player.add(items, atIndex) }
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

    fun skip(index: Int) {
        handler.post { player.jumpToItem(index) }
    }

    fun skipToNext() {
        handler.post { player.next() }
    }

    fun skipToPrevious() {
        handler.post { player.previous() }
    }

    fun seekTo(seconds: Float) {
        handler.post { player.seek(seconds.toLong(), TimeUnit.SECONDS) }
    }

    fun getCurrentTrackIndex(callback: (Int) -> Unit) {
        handler.post {
            callback(player.currentIndex)
        }
    }

    fun getRate(callback: (Float) -> Unit) {
        handler.post {
            callback(player.playbackSpeed)
        }
    }

    fun setRate(value: Float) {
        handler.post {
            player.playbackSpeed = value
        }
    }

    fun getVolume(callback: (Float) -> Unit) {
        handler.post {
            callback(player.volume)
        }
    }

    fun setVolume(value: Float) {
        handler.post {
            player.volume = value
        }
    }

    fun getDurationInSeconds(callback: (Double) -> Unit) {
        handler.post {
            callback(TimeUnit.MILLISECONDS.toSeconds(player.duration).toDouble())
        }
    }

    fun getPositionInSeconds(callback: (Double) -> Unit) {
        handler.post {
            callback(TimeUnit.MILLISECONDS.toSeconds(player.position).toDouble())
        }
    }

    fun getBufferedPositionInSeconds(callback: (Double) -> Unit) {
        handler.post {
            callback(TimeUnit.MILLISECONDS.toSeconds(player.bufferedPosition).toDouble())
        }
    }

    private fun observeEvents() {
        serviceScope.launch {
            event.stateChange.collect {
                val bundle = Bundle()

                bundle.putInt(STATE_KEY, it.asLibState.ordinal)

                if (it == AudioPlayerState.ENDED && player.nextItem == null) {
                    if (player.previousIndex != null) bundle.putInt(TRACK_KEY, player.previousIndex!!)
                    emit(MusicEvents.PLAYBACK_QUEUE_ENDED, null)
                }

                emit(MusicEvents.PLAYBACK_STATE, bundle)
            }
        }

        serviceScope.launch {
            event.audioItemTransition.collect {
                handler.post {
                    Bundle().apply {
                        putDouble(POSITION_KEY, 0.0)
                        putInt(NEXT_TRACK_KEY, player.currentIndex)
                        emit(MusicEvents.PLAYBACK_TRACK_CHANGED, this)
                    }
                }
            }
        }

        serviceScope.launch {
            event.onAudioFocusChanged.collect {
                Bundle().apply {
                    putBoolean(IS_FOCUS_LOSS_PERMANENT_KEY, it.isFocusLostPermanently)
                    putBoolean(IS_PAUSED_KEY, it.isPaused)
                    emit(MusicEvents.BUTTON_DUCK, this)
                }
            }
        }

        serviceScope.launch {
            event.onNotificationButtonTapped.collect {
                when (it) {
                    is PLAY -> emit(MusicEvents.BUTTON_PLAY)
                    is PAUSE -> emit(MusicEvents.BUTTON_PAUSE)
                    is NEXT -> emit(MusicEvents.BUTTON_SKIP_NEXT)
                    is PREVIOUS -> emit(MusicEvents.BUTTON_SKIP_PREVIOUS)
                    is STOP -> emit(MusicEvents.BUTTON_STOP)
                    is FORWARD -> emit(MusicEvents.BUTTON_JUMP_FORWARD)
                    is BACKWARD -> emit(MusicEvents.BUTTON_JUMP_BACKWARD)
                }

            }
        }

        serviceScope.launch {
            event.notificationStateChange.collect {
                when (it) {
                    is NotificationState.POSTED -> startForeground(it.notificationId, it.notification)
                    is NotificationState.CANCELLED -> stopForeground(true)
                }
            }
        }
    }

    private fun emit(event: String?, data: Bundle? = null) {
        val intent = Intent(EVENT_INTENT)
        intent.putExtra(EVENT_KEY, event)
        if (data != null) intent.putExtra(DATA_KEY, data)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig {
        return HeadlessJsTaskConfig(TASK_KEY, Arguments.createMap(), 0, true)
    }

    override fun onHeadlessJsTaskFinish(taskId: Int) {
        // Overridden to prevent the service from being terminated
    }

    override fun onBind(intent: Intent?): IBinder {
        return MusicBinder()
    }

    fun destroyIfAllowed(ignoreStopWithAppSetting: Boolean = false) {
        // Player will continue running if this is true, even if the app itself is killed.
        if (ignoreStopWithAppSetting) {
            stop()
            stopForeground(true)
            stopSelf()
        } else {
            if (!stopWithApp) return

            stop()
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onDestroy() {
        handler.post {
            player.destroy()
            handler.removeMessages(0)
        }

        super.onDestroy()
    }

    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }

    companion object {
        const val STATE_KEY = "state"
        const val EVENT_KEY = "event"
        const val DATA_KEY = "data"
        const val TRACK_KEY = "track"
        const val NEXT_TRACK_KEY = "nextTrack"
        const val POSITION_KEY = "position"

        const val TASK_KEY = "TrackPlayer"

        const val MIN_BUFFER_KEY = "minBuffer"
        const val MAX_BUFFER_KEY = "maxBuffer"
        const val PLAY_BUFFER_KEY = "playBuffer"
        const val BACK_BUFFER_KEY = "backBuffer"

        const val MAX_CACHE_SIZE_KEY = "maxCacheSize"

        const val STOP_WITH_APP_KEY = "stopWithApp"
        const val PAUSE_ON_INTERRUPTION_KEY = "alwaysPauseOnInterruption"

        const val IS_FOCUS_LOSS_PERMANENT_KEY = "permanent"
        const val IS_PAUSED_KEY = "paused"
    }
}