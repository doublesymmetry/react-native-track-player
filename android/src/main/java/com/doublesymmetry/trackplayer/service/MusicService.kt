package com.doublesymmetry.trackplayer.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import androidx.annotation.MainThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.models.NotificationButton.*
import com.doublesymmetry.kotlinaudio.players.AudioPlayer
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.doublesymmetry.trackplayer.R
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toMilliseconds
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toSeconds
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.extensions.find
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.model.TrackAudioItem
import com.doublesymmetry.trackplayer.module.MusicEvents
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.utils.BundleUtils
import com.doublesymmetry.trackplayer.utils.BundleUtils.setRating
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@MainThread
class MusicService : HeadlessJsTaskService() {
    private lateinit var player: QueuedAudioPlayer
    private val binder = MusicBinder()
    private val scope = MainScope()
    private var progressUpdateJob: Job? = null

    /**
     * Use [appKilledPlaybackBehavior] instead.
     */
    @Deprecated("This will be removed soon")
    var stoppingAppPausesPlayback = true
        private set

    private var appKilledPlaybackBehavior = AppKilledPlaybackBehavior.CONTINUE_PLAYBACK

    val tracks: List<Track>
        get() = player.items.map { (it as TrackAudioItem).track }

    val currentTrack
        get() = (player.currentItem as TrackAudioItem).track

    val state
        get() = player.playerState

    var ratingType: Int
        get() = player.ratingType
        set(value) {
            player.ratingType = value
        }

    val playbackError
        get() = player.playbackError

    val event
        get() = player.event

    var playWhenReady: Boolean
        get() = player.playWhenReady
        set(value) {
            player.playWhenReady = value
        }

    private var options = Bundle().apply {
        putBundle(
            UpdateOption.ANDROID_OPTIONS.key,
            Bundle().apply {
                putBoolean(
                    UpdateOption.ALWAYS_PAUSE_ON_INTERRUPTION.key,
                    false
                )
                putString(
                    UpdateOption.APP_KILLED_PLAYBACK_BEHAVIOR.key,
                    AppKilledPlaybackBehavior.CONTINUE_PLAYBACK.string
                )
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTask(getTaskConfig(intent))
        return START_STICKY
    }

    @MainThread
    fun setupPlayer(playerOptions: Bundle?) {
        val bufferConfig = BufferConfig(
            playerOptions?.getDouble(PlayerOption.MIN_BUFFER.key)?.toMilliseconds()?.toInt(),
            playerOptions?.getDouble(PlayerOption.MAX_BUFFER.key)?.toMilliseconds()?.toInt(),
            playerOptions?.getDouble(PlayerOption.PLAY_BUFFER.key)?.toMilliseconds()?.toInt(),
            playerOptions?.getDouble(PlayerOption.BACK_BUFFER.key)?.toMilliseconds()?.toInt(),
        )

        val cacheConfig = CacheConfig(
            playerOptions?.getDouble(PlayerOption.MAX_CACHE_SIZE.key)?.toLong()
        )
        val playerConfig = PlayerConfig(
            interceptPlayerActionsTriggeredExternally = true,
            handleAudioBecomingNoisy = true,
            handleAudioFocus = playerOptions?.getBoolean(
                PlayerOption.AUTO_HANDLE_INTERRUPTIONS.key
            ) ?: false,
            audioContentType = when (
                playerOptions?.getString(PlayerOption.ANDROID_AUDIO_CONTENT_TYPE.key)
            ) {
                "music" -> AudioContentType.MUSIC
                "speech" -> AudioContentType.SPEECH
                "sonification" -> AudioContentType.SONIFICATION
                "movie" -> AudioContentType.MOVIE
                "unknown" -> AudioContentType.UNKNOWN
                else -> AudioContentType.MUSIC
            }
        )

        player = QueuedAudioPlayer(this@MusicService, playerConfig, bufferConfig, cacheConfig)
        player.automaticallyUpdateNotificationMetadata = playerOptions?.getBoolean(
            PlayerOption.AUTO_UPDATE_METADATA.key,
            true
        ) ?: true
        observeEvents()
    }

    @MainThread
    fun updateOptions(updateOptions: Bundle) {
        val androidUpdateOptions = updateOptions.getBundle(UpdateOption.ANDROID_OPTIONS.key)

        // Remove android options, so we can copy over everything else:
        updateOptions.remove(UpdateOption.ANDROID_OPTIONS.key)
        options.putAll(updateOptions)

        val androidOptions = options.getBundle(UpdateOption.ANDROID_OPTIONS.key)
        if (androidUpdateOptions != null) {
            androidOptions?.putAll(androidUpdateOptions)
            if (androidUpdateOptions.containsKey(UpdateOption.APP_KILLED_PLAYBACK_BEHAVIOR.key)) {
                appKilledPlaybackBehavior = AppKilledPlaybackBehavior::string.find(
                    androidOptions?.getString(UpdateOption.APP_KILLED_PLAYBACK_BEHAVIOR.key)
                )!!
            }

            if (androidUpdateOptions.containsKey(UpdateOption.ALWAYS_PAUSE_ON_INTERRUPTION.key)) {
                player.playerOptions.alwaysPauseOnInterruption = androidOptions?.getBoolean(
                    UpdateOption.ALWAYS_PAUSE_ON_INTERRUPTION.key
                )!!
            }
        }

        if (updateOptions.containsKey(UpdateOption.STOPPING_APP_PAUSES_PLAYBACK.key)) {
            //TODO: This handles a deprecated flag. Should be removed soon.
            options.getBoolean(UpdateOption.STOPPING_APP_PAUSES_PLAYBACK.key).let {
                stoppingAppPausesPlayback =
                    options.getBoolean(UpdateOption.STOPPING_APP_PAUSES_PLAYBACK.key)
                if (stoppingAppPausesPlayback) {
                    appKilledPlaybackBehavior = AppKilledPlaybackBehavior.PAUSE_PLAYBACK
                }
            }
        }

        if (updateOptions.containsKey(UpdateOption.RATING_TYPE.key)) {
            ratingType = BundleUtils.getInt(
                updateOptions,
                UpdateOption.RATING_TYPE.key,
                RatingCompat.RATING_NONE
            )
        }

        if (NotificationOption.values().any { updateOptions.containsKey(it.key) }) {
            updateNotification()
        }

        if (updateOptions.containsKey(UpdateOption.PROGRESS_UPDATE_EVENT_INTERVAL.key)) {
            updateProgressUpdate()
        }
    }

    @MainThread
    private fun updateProgressUpdate() {
        // setup progress update events if configured
        progressUpdateJob?.cancel()
        val updateInterval = BundleUtils.getIntOrNull(
            options,
            UpdateOption.PROGRESS_UPDATE_EVENT_INTERVAL.key
        )
        if (updateInterval != null && updateInterval > 0) {
            progressUpdateJob = scope.launch {
                progressUpdateEventFlow(updateInterval.toLong()).collect {
                    emit(MusicEvents.PLAYBACK_PROGRESS_UPDATED, it)
                }
            }
        }
    }

    @MainThread
    private fun updateNotification() {
        val capabilities = options.getIntegerArrayList(
            NotificationOption.CAPABILITIES.key
        )?.map { Capability.values()[it] } ?: emptyList()

        var notificationCapabilities = options.getIntegerArrayList(
            NotificationOption.NOTIFICATION_CAPABILITIES.key
        )?.map { Capability.values()[it] } ?: capabilities

        val compactCapabilities = options.getIntegerArrayList(
            NotificationOption.COMPACT_CAPABILITIES.key
        )?.map { Capability.values()[it] } ?: emptyList()

        val buttons = mutableListOf<NotificationButton>()

        fun getIcon(propertyName: String, defaultIcon: Int? = null): Int? {
            if (!options.containsKey(propertyName)) return defaultIcon
            val bundle = options.getBundle(propertyName) ?: return defaultIcon
            val helper = ResourceDrawableIdHelper.getInstance()
            val icon = helper.getResourceDrawableId(this, bundle.getString("uri"))
            return if (icon == 0) defaultIcon else icon
        }

        notificationCapabilities.forEach {
            when (it) {
                Capability.PLAY, Capability.PAUSE -> {
                    buttons.add(PLAY_PAUSE(
                        playIcon = getIcon(NotificationOption.PLAY_ICON.key),
                        pauseIcon = getIcon(NotificationOption.PAUSE_ICON.key)
                    ))
                }
                Capability.STOP -> {
                    buttons.add(STOP(icon = getIcon(NotificationOption.STOP_ICON.key)))
                }
                Capability.SKIP_TO_NEXT -> {
                    buttons.add(NEXT(
                        icon = getIcon(NotificationOption.NEXT_ICON.key),
                        isCompact = compactCapabilities.contains(it)
                    ))
                }
                Capability.SKIP_TO_PREVIOUS -> {
                    buttons.add(PREVIOUS(
                        icon = getIcon(NotificationOption.PREVIOUS_ICON.key),
                        isCompact = compactCapabilities.contains(it)
                    ))
                }
                Capability.JUMP_FORWARD -> {
                    buttons.add(FORWARD(
                        icon = getIcon(NotificationOption.FORWARD_ICON.key, R.drawable.forward),
                        isCompact = compactCapabilities.contains(it)
                    ))
                }
                Capability.JUMP_BACKWARD -> {
                    buttons.add(BACKWARD(
                        icon = getIcon(NotificationOption.REWIND_ICON.key, R.drawable.rewind),
                        isCompact = compactCapabilities.contains(it)
                    ))
                }
                else -> return@forEach
            }
        }

        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add the Uri data so apps can identify that it was a notification click
            data = Uri.parse("trackplayer://notification.click")
            action = Intent.ACTION_VIEW
        }

        val accentColor = BundleUtils.getIntOrNull(options, "color")
        val smallIcon = getIcon(NotificationOption.ICON.key)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, openAppIntent, getPendingIntentFlags())
        val notificationConfig = NotificationConfig(buttons, accentColor, smallIcon, pendingIntent)
        player.notificationManager.createNotification(notificationConfig)
    }

    @MainThread
    private fun progressUpdateEventFlow(interval: Long) = flow {
        while (true) {
            if (player.isPlaying) {
                val bundle = progressUpdateEvent()
                emit(bundle)
            }

            delay(interval * 1000)
        }
    }

    @MainThread
    private suspend fun progressUpdateEvent(): Bundle {
        return withContext(Dispatchers.Main) {
            Bundle().apply {
                putDouble("position", player.position.toSeconds())
                putDouble("duration", player.duration.toSeconds())
                putDouble("buffer", player.bufferedPosition.toSeconds())
                putInt("track", player.currentIndex)
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

    @MainThread
    fun add(track: Track) {
        add(listOf(track))
    }

    @MainThread
    fun add(tracks: List<Track>) {
        val items = tracks.map { it.toAudioItem() }
        player.add(items)
    }

    @MainThread
    fun add(tracks: List<Track>, atIndex: Int) {
        val items = tracks.map { it.toAudioItem() }
        player.add(items, atIndex)
    }

    @MainThread
    fun load(track: Track) {
        player.load(track.toAudioItem())
    }

    @MainThread
    fun move(fromIndex: Int, toIndex: Int) {
        player.move(fromIndex, toIndex);
    }

    @MainThread
    fun remove(index: Int) {
        remove(listOf(index))
    }

    @MainThread
    fun remove(indexes: List<Int>) {
        player.remove(indexes)
    }

    @MainThread
    fun clear() {
        player.clear()
    }

    @MainThread
    fun play() {
        player.play()
    }

    @MainThread
    fun pause() {
        player.pause()
    }

    @MainThread
    fun stop() {
        player.stop()
    }

    @MainThread
    fun removeUpcomingTracks() {
        player.removeUpcomingItems()
    }

    @MainThread
    fun removePreviousTracks() {
        player.removePreviousItems()
    }

    @MainThread
    fun skip(index: Int) {
        player.jumpToItem(index)
    }

    @MainThread
    fun skipToNext() {
        player.next()
    }

    @MainThread
    fun skipToPrevious() {
        player.previous()
    }

    @MainThread
    fun seekTo(seconds: Float) {
        player.seek((seconds.toLong()), TimeUnit.SECONDS)
    }

    @MainThread
    fun seekBy(offset: Float) {
        player.seekBy((offset.toLong()), TimeUnit.SECONDS)
    }

    @MainThread
    fun retry() {
        player.prepare()
    }

    @MainThread
    fun getCurrentTrackIndex(): Int = player.currentIndex

    @MainThread
    fun getRate(): Float = player.playbackSpeed

    @MainThread
    fun setRate(value: Float) {
        player.playbackSpeed = value
    }

    @MainThread
    fun getRepeatMode(): RepeatMode = player.playerOptions.repeatMode

    @MainThread
    fun setRepeatMode(value: RepeatMode) {
        player.playerOptions.repeatMode = value
    }

    @MainThread
    fun getVolume(): Float = player.volume

    @MainThread
    fun setVolume(value: Float) {
        player.volume = value
    }

    @MainThread
    fun getDurationInSeconds(): Double = player.duration.toSeconds()

    @MainThread
    fun getPositionInSeconds(): Double = player.position.toSeconds()

    @MainThread
    fun getBufferedPositionInSeconds(): Double = player.bufferedPosition.toSeconds()

    @MainThread
    fun updateMetadataForTrack(index: Int, track: Track) {
        player.replaceItem(index, track.toAudioItem())
    }

    @MainThread
    fun updateNotificationMetadata(title: String?, artist: String?, artwork: String?) {
        player.notificationManager.notificationMetadata =
            NotificationMetadata(title, artist, artwork)
    }

    @MainThread
    fun clearNotificationMetadata() {
        player.notificationManager.hideNotification()
    }

    private fun emitPlaybackTrackChangedEvents(
        index: Int?,
        previousIndex: Int?,
        oldPosition: Double
    ) {
        emit(MusicEvents.PLAYBACK_TRACK_CHANGED, Bundle().apply {
            putDouble("position", oldPosition)
            if (index != null) {
                putInt("nextTrack", index)
            }
            if (previousIndex != null) {
                putInt("track", previousIndex)
            }
        })
        emit(MusicEvents.PLAYBACK_ACTIVE_TRACK_CHANGED, Bundle().apply {
            putDouble("lastPosition", oldPosition)
            if (tracks.size > 0) {
                putInt("index", player.currentIndex)
                putBundle("track", tracks[player.currentIndex].originalItem)
                if (previousIndex != null) {
                    putInt("lastIndex", previousIndex)
                    putBundle("lastTrack", tracks[previousIndex].originalItem)
                }
            }
        })
    }

    private fun emitQueueEndedEvent() {
        val bundle = Bundle()
        bundle.putInt("track", player.currentIndex)
        bundle.putDouble("position", player.position.toSeconds())
        emit(MusicEvents.PLAYBACK_QUEUE_ENDED, bundle)
    }

    @MainThread
    private fun observeEvents() {
        scope.launch {
            event.stateChange.collect {
                emit(MusicEvents.PLAYBACK_STATE, getPlaybackStateBundle(it))

                if (it == AudioPlayerState.ENDED && player.nextItem == null) {
                    emitQueueEndedEvent()
                    emitPlaybackTrackChangedEvents(
                        index = null,
                        previousIndex = player.currentIndex,
                        oldPosition = player.position.toSeconds()
                    )
                }
            }
        }

        scope.launch {
            event.audioItemTransition.collect {
                var lastIndex: Int? = null
                if (it is AudioItemTransitionReason.REPEAT) {
                    lastIndex = player.currentIndex
                } else if (player.previousItem != null) {
                    lastIndex = player.previousIndex
                }
                var lastPosition = (it?.oldPosition ?: 0).toSeconds();
                emitPlaybackTrackChangedEvents(player.currentIndex, lastIndex, lastPosition)
            }
        }

        scope.launch {
            event.onAudioFocusChanged.collect {
                Bundle().apply {
                    putBoolean("permanent", it.isFocusLostPermanently)
                    putBoolean("paused", it.isPaused)
                    emit(MusicEvents.BUTTON_DUCK, this)
                }
            }
        }

        scope.launch {
            event.notificationStateChange.collect {
                when (it) {
                    is NotificationState.POSTED -> {
                        startForeground(it.notificationId, it.notification)
                    }
                    is NotificationState.CANCELLED -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        } else {
                            @Suppress("DEPRECATION")
                            stopForeground(true)
                        }
                    }
                }
            }
        }

        scope.launch {
            val DEFAULT_JUMP_INTERVAL = 15.0
            event.onPlayerActionTriggeredExternally.collect {
                when (it) {
                    is MediaSessionCallback.RATING -> {
                        Bundle().apply {
                            setRating(this, "rating", it.rating)
                            emit(MusicEvents.BUTTON_SET_RATING, this)
                        }
                    }
                    is MediaSessionCallback.SEEK -> {
                        Bundle().apply {
                            putDouble("position", it.positionMs.toSeconds())
                            emit(MusicEvents.BUTTON_SEEK_TO, this)
                        }
                    }
                    MediaSessionCallback.PLAY -> emit(MusicEvents.BUTTON_PLAY)
                    MediaSessionCallback.PAUSE -> emit(MusicEvents.BUTTON_PAUSE)
                    MediaSessionCallback.NEXT -> emit(MusicEvents.BUTTON_SKIP_NEXT)
                    MediaSessionCallback.PREVIOUS -> emit(MusicEvents.BUTTON_SKIP_PREVIOUS)
                    MediaSessionCallback.STOP -> emit(MusicEvents.BUTTON_STOP)
                    MediaSessionCallback.FORWARD -> {
                        Bundle().apply {
                            val interval = options?.getDouble(
                                UpdateOption.FORWARD_JUMP_INTERVAL.key,
                                DEFAULT_JUMP_INTERVAL
                            ) ?: DEFAULT_JUMP_INTERVAL
                            putInt("interval", interval.toInt())
                            emit(MusicEvents.BUTTON_JUMP_FORWARD, this)
                        }
                    }
                    MediaSessionCallback.REWIND -> {
                        Bundle().apply {
                            val interval = options?.getDouble(
                                UpdateOption.BACKWARD_JUMP_INTERVAL.key,
                                DEFAULT_JUMP_INTERVAL
                            ) ?: DEFAULT_JUMP_INTERVAL
                            putInt("interval", interval.toInt())
                            emit(MusicEvents.BUTTON_JUMP_BACKWARD, this)
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

        scope.launch {
            event.playWhenReadyChange.collect {
                Bundle().apply {
                    putBoolean("playWhenReady", it.playWhenReady)
                    emit(MusicEvents.PLAYBACK_PLAY_WHEN_READY_CHANGED, this)
                }
            }
        }

        scope.launch {
            event.playbackError.collect {
                emit(MusicEvents.PLAYBACK_ERROR, getPlaybackErrorBundle())
            }
        }
    }

    fun getPlaybackStateBundle(state: AudioPlayerState): Bundle {
        return Bundle().apply {
            putString("state", state.asLibState.state)
            if (state == AudioPlayerState.ERROR) {
                putBundle("error", getPlaybackErrorBundle())
            }
        }
    }

    private fun getPlaybackErrorBundle(): Bundle {
        val error = playbackError!!
        return Bundle().apply {
            if (error.message != null) {
                putString("message", error.message)
            }
            if (error.code != null) {
                putString("code", "android-" + error.code)
            }
        }
    }

    @MainThread
    private fun emit(event: String?, data: Bundle? = null) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(
            Intent(EVENT_INTENT).apply {
                putExtra("event", event)
                if (data != null) putExtra("data", data)
            }
        )
    }

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig {
        return HeadlessJsTaskConfig(
            "TrackPlayer",
            Arguments.createMap(),
            0,
            true
        )
    }

    @MainThread
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    @MainThread
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        if (!::player.isInitialized) return

        when (appKilledPlaybackBehavior) {
            AppKilledPlaybackBehavior.PAUSE_PLAYBACK -> player.pause()
            AppKilledPlaybackBehavior.STOP_PLAYBACK_AND_REMOVE_NOTIFICATION -> {
                player.clear()
                player.stop()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }

                stopSelf()
                exitProcess(0)
            }
            else -> {}
        }
    }

    @MainThread
    override fun onHeadlessJsTaskFinish(taskId: Int) {
        // This is empty so ReactNative doesn't kill this service
    }

    @MainThread
    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.destroy()
        }

        progressUpdateJob?.cancel()
    }

    @MainThread
    inner class MusicBinder : Binder() {
        val service = this@MusicService
    }

    enum class PlayerOption(val key: String) {
        MIN_BUFFER("minBuffer"),
        MAX_BUFFER("maxBuffer"),
        PLAY_BUFFER("playBuffer"),
        BACK_BUFFER("backBuffer"),
        MAX_CACHE_SIZE("maxCacheSize"),
        AUTO_UPDATE_METADATA("autoUpdateMetadata"),
        AUTO_HANDLE_INTERRUPTIONS("autoHandleInterruptions"),
        ANDROID_AUDIO_CONTENT_TYPE("androidAudioContentType")
    }

    enum class UpdateOption(val key: String) {
        RATING_TYPE("ratingType"),
        FORWARD_JUMP_INTERVAL("forwardJumpInterval"),
        BACKWARD_JUMP_INTERVAL("backwardJumpInterval"),
        PROGRESS_UPDATE_EVENT_INTERVAL("progressUpdateEventInterval"),
        ANDROID_OPTIONS("android"),
        STOPPING_APP_PAUSES_PLAYBACK("stoppingAppPausesPlayback"),
        APP_KILLED_PLAYBACK_BEHAVIOR("appKilledPlaybackBehavior"),
        ALWAYS_PAUSE_ON_INTERRUPTION("alwaysPauseOnInterruption")
    }

    enum class NotificationOption(val key: String) {
        CAPABILITIES("capabilities"),
        NOTIFICATION_CAPABILITIES("notificationCapabilities"),
        COMPACT_CAPABILITIES("compactCapabilities"),
        ANDROID_OPTIONS("android"),
        PLAY_ICON("playIcon"),
        PAUSE_ICON("pauseIcon"),
        STOP_ICON("stopIcon"),
        NEXT_ICON("nextIcon"),
        PREVIOUS_ICON("previousIcon"),
        FORWARD_ICON("forwardIcon"),
        REWIND_ICON("rewindIcon"),
        ICON("icon")
    }

    enum class AppKilledPlaybackBehavior(val string: String) {
        CONTINUE_PLAYBACK("continue-playback"),
        PAUSE_PLAYBACK("pause-playback"),
        STOP_PLAYBACK_AND_REMOVE_NOTIFICATION("stop-playback-and-remove-notification")
    }
}
