package com.doublesymmetry.trackplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.os.*
import android.support.v4.media.RatingCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.models.NotificationButton.*
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.doublesymmetry.trackplayer.R
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.model.TrackAudioItem
import com.doublesymmetry.trackplayer.module.MusicEvents
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.utils.Utils
import com.doublesymmetry.trackplayer.utils.Utils.setRating
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class MusicService : HeadlessJsTaskService() {
    private lateinit var player: QueuedAudioPlayer
    private val scope = MainScope()
    private var progressUpdateJob: Job? = null

    var stopWithApp = false
        private set

    val tracks: List<Track>
        get() = player.items.map { (it as TrackAudioItem).track }

    val currentTrack
        get() = (player.currentItem as TrackAudioItem).track

    var ratingType: Int
        get() = player.notificationManager.ratingType
        set(value) {
            player.notificationManager.ratingType = value
        }

    val event get() = player.event

    private var latestOptions: Bundle? = null
    private var capabilities: List<Capability> = emptyList()
    private var notificationCapabilities: List<Capability> = emptyList()
    private var compactCapabilities: List<Capability> = emptyList()

    fun setupPlayer(playerOptions: Bundle?, promise: Promise?) {
        val bufferOptions = BufferConfig(
                playerOptions?.getDouble(MIN_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
                playerOptions?.getDouble(MAX_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
                playerOptions?.getDouble(PLAY_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
                playerOptions?.getDouble(BACK_BUFFER_KEY)?.let { Utils.toMillis(it).toInt() },
        )

        val cacheOptions = CacheConfig(
                playerOptions?.getDouble(MAX_CACHE_SIZE_KEY)?.toLong()
        )

        val automaticallyUpdateNotificationMetadata = playerOptions?.getBoolean(AUTO_UPDATE_METADATA, true) ?: true

        scope.launch {
            player = QueuedAudioPlayer(this@MusicService, bufferOptions, cacheOptions)
            player.automaticallyUpdateNotificationMetadata = automaticallyUpdateNotificationMetadata
            observeEvents()
            promise?.resolve(null)
        }
    }

    fun updateOptions(options: Bundle) {
        scope.launch {
            with (this@MusicService) {
                latestOptions = options
                stopWithApp = options.getBoolean(STOP_WITH_APP_KEY)

                ratingType = Utils.getInt(options, "ratingType", RatingCompat.RATING_NONE)

                player.playerOptions.alwaysPauseOnInterruption =
                    options.getBoolean(PAUSE_ON_INTERRUPTION_KEY)

                capabilities =
                    options.getIntegerArrayList("capabilities")?.map { Capability.values()[it] }
                        ?: emptyList()
                notificationCapabilities = options.getIntegerArrayList("notificationCapabilities")
                    ?.map { Capability.values()[it] } ?: emptyList()
                compactCapabilities = options.getIntegerArrayList("compactCapabilities")
                    ?.map { Capability.values()[it] } ?: emptyList()

                if (notificationCapabilities.isEmpty()) notificationCapabilities = capabilities

                val buttonsList = mutableListOf<NotificationButton>()

                notificationCapabilities.forEach {
                    when (it) {
                        Capability.PLAY -> {
                            val playIcon =
                                Utils.getIconOrNull(this, options, "playIcon")
                            buttonsList.add(PLAY(icon = playIcon))
                        }
                        Capability.PAUSE -> {
                            val pauseIcon =
                                Utils.getIconOrNull(this, options, "pauseIcon")
                            buttonsList.add(PAUSE(icon = pauseIcon))
                        }
                        Capability.STOP -> {
                            val stopIcon =
                                Utils.getIconOrNull(this, options, "stopIcon")
                            buttonsList.add(STOP(icon = stopIcon, isCompact = isCompact(it)))
                        }
                        Capability.SKIP_TO_NEXT -> {
                            val nextIcon =
                                Utils.getIconOrNull(this, options, "nextIcon")
                            buttonsList.add(NEXT(icon = nextIcon, isCompact = isCompact(it)))
                        }
                        Capability.SKIP_TO_PREVIOUS -> {
                            val previousIcon =
                                Utils.getIconOrNull(this, options, "previousIcon")
                            buttonsList.add(
                                PREVIOUS(
                                    icon = previousIcon,
                                    isCompact = isCompact(it)
                                )
                            )
                        }
                        Capability.JUMP_FORWARD -> {
                            val forwardIcon = Utils.getIcon(
                                this,
                                options,
                                "forwardIcon",
                                R.drawable.forward
                            )
                            buttonsList.add(FORWARD(icon = forwardIcon, isCompact = isCompact(it)))
                        }
                        Capability.JUMP_BACKWARD -> {
                            val backwardIcon = Utils.getIcon(
                                this,
                                options,
                                "rewindIcon",
                                R.drawable.rewind
                            )
                            buttonsList.add(
                                BACKWARD(
                                    icon = backwardIcon,
                                    isCompact = isCompact(it)
                                )
                            )
                        }
                        else -> return@forEach
                    }
                }

                val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                val accentColor = Utils.getIntOrNull(options, "color")
                val smallIcon = Utils.getIconOrNull(this, options, "icon")
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    openAppIntent,
                    getPendingIntentFlags()
                )
                val notificationConfig = NotificationConfig(
                    buttonsList,
                    accentColor,
                    smallIcon,
                    pendingIntent
                )

                player.notificationManager.createNotification(notificationConfig)
            }
        }

        // setup progress update events if configured
        progressUpdateJob?.cancel()
        val updateInterval = Utils.getIntOrNull(options, PROGRESS_UPDATE_EVENT_INTERVAL_KEY)
        if (updateInterval != null && updateInterval > 0) {
            progressUpdateJob = scope.launch {
                progressUpdateEventFlow(updateInterval.toLong())
                    .collect { emit(MusicEvents.PLAYBACK_PROGRESS_UPDATED, it) }
            }
        }
    }

    private fun progressUpdateEventFlow(interval: Long) = flow {
        while (true) {
            if (player.isPlaying) {
                val bundle = progressUpdateEvent()
                emit(bundle)
            }

            delay(interval * 1000)
        }
    }

    private suspend fun progressUpdateEvent(): Bundle {
        return withContext(Dispatchers.Main) {
            Bundle().apply {
                putDouble(POSITION_KEY, player.position.toDouble() / 1000)
                putDouble(DURATION_KEY, player.duration.toDouble() / 1000)
                putDouble(BUFFERED_POSITION_KEY, player.bufferedPosition.toDouble() / 1000)
                putInt(TRACK_KEY, player.currentIndex)
            }
        }
    }

    private fun getPendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
    }

    private fun isCompact(capability: Capability): Boolean {
        return compactCapabilities.contains(capability)
    }

    fun add(track: Track) {
        add(listOf(track))
    }

    fun add(tracks: List<Track>) {
        val items = tracks.map { it.toAudioItem() }
        scope.launch { player.add(items, false) }
    }

    fun add(tracks: List<Track>, atIndex: Int) {
        val items = tracks.map { it.toAudioItem() }
        scope.launch { player.add(items, atIndex) }
    }

    fun remove(index: Int) {
        remove(listOf(index))
    }

    fun remove(indexes: List<Int>) = scope.launch {
        player.remove(indexes)
    }

    fun play() = scope.launch {
        player.play()
    }

    fun pause() = scope.launch {
        player.pause()
    }

    fun stop() = scope.launch {
        player.stop()
    }

    fun removeUpcomingTracks() = scope.launch {
        player.removeUpcomingItems()
    }

    fun removePreviousTracks() = scope.launch {
        player.removePreviousItems()
    }

    fun skip(index: Int) = scope.launch {
        player.jumpToItem(index, player.isPlaying)
    }

    fun skipToNext() = scope.launch {
        player.next()
    }

    fun skipToPrevious() = scope.launch {
        player.previous()
    }

    fun seekTo(seconds: Float) = scope.launch {
        player.seek((seconds * 1000).toLong(), TimeUnit.MILLISECONDS)
    }

    fun getCurrentTrackIndex(callback: (Int) -> Unit) = scope.launch {
        callback(player.currentIndex)
    }

    fun getRate(callback: (Float) -> Unit) = scope.launch {
        callback(player.playbackSpeed)
    }

    fun setRate(value: Float) = scope.launch {
        player.playbackSpeed = value
    }

    fun getRepeatMode(callback: (RepeatMode) -> Unit) = scope.launch {
        callback(player.playerOptions.repeatMode)
    }

    fun setRepeatMode(value: RepeatMode) = scope.launch {
        player.playerOptions.repeatMode = value
    }

    fun getVolume(callback: (Float) -> Unit) = scope.launch {
        callback(player.volume)
    }

    fun setVolume(value: Float) = scope.launch {
        player.volume = value
    }

    fun getDurationInSeconds(callback: (Double) -> Unit) = scope.launch {
        callback(player.duration.toDouble() / 1000)
    }

    fun getPositionInSeconds(callback: (Double) -> Unit) = scope.launch {
        callback(player.position.toDouble() / 1000)
    }

    fun getBufferedPositionInSeconds(callback: (Double) -> Unit) = scope.launch {
        callback(player.bufferedPosition.toDouble() / 1000)
    }

    fun updateMetadataForTrack(index: Int, track: Track) = scope.launch {
        player.replaceItem(index, track.toAudioItem())
    }

    fun updateNotificationMetadata(title: String?, artist: String?, artwork: String?) = scope.launch {
        player.notificationManager.notificatioMetadata = NotificationMetadata(title, artist, artwork)
    }

    fun clearNotificationMetadata() = scope.launch {
        player.notificationManager.clearNotification()
    }

    private fun observeEvents() {
        scope.launch {
            event.stateChange.collect {
                val bundle = Bundle()
                bundle.putInt(STATE_KEY, it.asLibState.ordinal)
                emit(MusicEvents.PLAYBACK_STATE, bundle)

                if (it == AudioPlayerState.ENDED && player.nextItem == null) {
                    val endBundle = Bundle()
                    endBundle.putInt(TRACK_KEY, player.currentIndex)
                    endBundle.putDouble(POSITION_KEY, Utils.toSeconds(player.position));

                    emit(MusicEvents.PLAYBACK_QUEUE_ENDED, endBundle)
                    emit(MusicEvents.PLAYBACK_TRACK_CHANGED, endBundle)
                }
            }
        }

        scope.launch {
            event.audioItemTransition.collect {
                Bundle().apply {
                    putDouble(POSITION_KEY, 0.0)
                    putInt(NEXT_TRACK_KEY, player.currentIndex)
                    emit(MusicEvents.PLAYBACK_TRACK_CHANGED, this)
                }
            }
        }

        scope.launch {
            event.onAudioFocusChanged.collect {
                Bundle().apply {
                    putBoolean(IS_FOCUS_LOSS_PERMANENT_KEY, it.isFocusLostPermanently)
                    putBoolean(IS_PAUSED_KEY, it.isPaused)
                    emit(MusicEvents.BUTTON_DUCK, this)
                }
            }
        }

        scope.launch {
            event.onNotificationButtonTapped.collect {
                when (it) {
                    is PLAY -> emit(MusicEvents.BUTTON_PLAY)
                    is PAUSE -> emit(MusicEvents.BUTTON_PAUSE)
                    is NEXT -> emit(MusicEvents.BUTTON_SKIP_NEXT)
                    is PREVIOUS -> emit(MusicEvents.BUTTON_SKIP_PREVIOUS)
                    is STOP -> emit(MusicEvents.BUTTON_STOP)
                    is FORWARD -> {
                        Bundle().apply {
                            val interval = (latestOptions?.getInt(FORWARD_JUMP_INTERVAL_KEY) ?: latestOptions?.getInt(
                                    BACKWARD_JUMP_INTERVAL_KEY)) ?: 15

                            putInt("interval", interval)
                            emit(MusicEvents.BUTTON_JUMP_FORWARD, this)
                        }
                    }
                    is BACKWARD -> {
                        Bundle().apply {
                            val interval = (latestOptions?.getInt(BACKWARD_JUMP_INTERVAL_KEY) ?: latestOptions?.getInt(
                                    FORWARD_JUMP_INTERVAL_KEY)) ?: 15

                            putInt("interval", interval)
                            emit(MusicEvents.BUTTON_JUMP_BACKWARD, this)
                        }
                    }
                }

            }
        }

        scope.launch {
            event.notificationStateChange.collect {
                when (it) {
                    is NotificationState.POSTED -> startForeground(
                            it.notificationId,
                            it.notification
                    )
                    is NotificationState.CANCELLED -> stopForeground(true)
                }
            }
        }

        scope.launch {
            event.onMediaSessionCallbackTriggered.collect {
                when (it) {
                    is MediaSessionCallback.RATING -> {
                        Bundle().apply {
                            setRating(this, "rating", it.rating)
                            emit(MusicEvents.BUTTON_SET_RATING, this)
                        }
                    }
                }
            }
        }

        scope.launch {
            event.onPlaybackMetadata.collect {
                Bundle().apply {
                    putString("source", it.source)
                    putString("title", it.title)
                    putString("url", it.url)
                    putString("artist", it.artist)
                    putString("album", it.album)
                    putString("date", it.date)
                    putString("genre", it.genre)
                    emit(MusicEvents.PLAYBACK_METADATA, this)
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

    // TODO: #AEX-45 forceDestroy is needed when calling destroy() manually. Find an alternative solution that does not require a second flag.
    fun destroyIfAllowed(forceDestroy: Boolean = false) {
        // Player will continue running if this is true, even if the app itself is killed.
        if (!forceDestroy && !stopWithApp) return

        stop()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        scope.launch {
            if (this@MusicService::player.isInitialized) {
                player.destroy()
            }

            scope.cancel()
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
        const val DURATION_KEY = "duration"
        const val BUFFERED_POSITION_KEY = "buffer"

        const val TASK_KEY = "TrackPlayer"

        const val MIN_BUFFER_KEY = "minBuffer"
        const val MAX_BUFFER_KEY = "maxBuffer"
        const val PLAY_BUFFER_KEY = "playBuffer"
        const val BACK_BUFFER_KEY = "backBuffer"

        const val FORWARD_JUMP_INTERVAL_KEY = "forwardJumpInterval"
        const val BACKWARD_JUMP_INTERVAL_KEY = "backwardJumpInterval"
        const val PROGRESS_UPDATE_EVENT_INTERVAL_KEY = "progressUpdateEventInterval"

        const val MAX_CACHE_SIZE_KEY = "maxCacheSize"

        const val STOP_WITH_APP_KEY = "stopWithApp"
        const val PAUSE_ON_INTERRUPTION_KEY = "alwaysPauseOnInterruption"
        const val AUTO_UPDATE_METADATA = "autoUpdateMetadata"

        const val IS_FOCUS_LOSS_PERMANENT_KEY = "permanent"
        const val IS_PAUSED_KEY = "paused"
    }
}
