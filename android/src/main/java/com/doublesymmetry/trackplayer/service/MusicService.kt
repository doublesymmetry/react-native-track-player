package com.doublesymmetry.trackplayer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import androidx.annotation.MainThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toMilliseconds
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toSeconds
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.extensions.find
import com.doublesymmetry.trackplayer.model.RNTPCapability
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.model.TrackAudioItem
import com.doublesymmetry.trackplayer.module.MusicEvents
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.utils.BundleUtils
import com.doublesymmetry.trackplayer.utils.BundleUtils.setRating
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.google.android.exoplayer2.ui.R as ExoPlayerR
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess
import timber.log.Timber

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

    enum class AppKilledPlaybackBehavior(val string: String) {
        CONTINUE_PLAYBACK("continue-playback"), PAUSE_PLAYBACK("pause-playback"), STOP_PLAYBACK_AND_REMOVE_NOTIFICATION("stop-playback-and-remove-notification")
    }

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

    private var jumpForwardInterval: Int = 15
    private var jumpBackwardInterval: Int = 15

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTask(getTaskConfig(intent))
        startAndStopEmptyNotificationToAvoidANR()
        return START_STICKY
    }

    /**
     * Workaround for the "Context.startForegroundService() did not then call Service.startForeground()"
     * within 5s" ANR and crash by creating an empty notification and stopping it right after. For more
     * information see https://github.com/doublesymmetry/react-native-track-player/issues/1666
     */
    private fun startAndStopEmptyNotificationToAvoidANR() {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var name = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            name = "temporary_channel"
            notificationManager.createNotificationChannel(
                NotificationChannel(name, name, NotificationManager.IMPORTANCE_LOW)
            )
        }

        val notificationBuilder = NotificationCompat.Builder(this, name)
            .setPriority(PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(ExoPlayerR.drawable.exo_notification_small_icon)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
        }
        val notification = notificationBuilder.build()
        startForeground(EMPTY_NOTIFICATION_ID, notification)
        @Suppress("DEPRECATION")
        stopForeground(true)
    }

    @MainThread
    fun setupPlayer(playerOptions: Bundle?) {
        if (this::player.isInitialized) {
            print("Player was initialized. Prevent re-initializing again")
            return
        }

        val bufferConfig = BufferConfig(
            playerOptions?.getDouble(MIN_BUFFER_KEY)?.toMilliseconds()?.toInt(),
            playerOptions?.getDouble(MAX_BUFFER_KEY)?.toMilliseconds()?.toInt(),
            playerOptions?.getDouble(PLAY_BUFFER_KEY)?.toMilliseconds()?.toInt(),
            playerOptions?.getDouble(BACK_BUFFER_KEY)?.toMilliseconds()?.toInt(),
        )

        val cacheConfig = CacheConfig(playerOptions?.getDouble(MAX_CACHE_SIZE_KEY)?.toLong())
        val playerConfig = PlayerConfig(
            interceptPlayerActionsTriggeredExternally = true,
            handleAudioBecomingNoisy = true,
            handleAudioFocus = playerOptions?.getBoolean(AUTO_HANDLE_INTERRUPTIONS) ?: false,
            audioContentType = when(playerOptions?.getString(ANDROID_AUDIO_CONTENT_TYPE)) {
                "music" -> AudioContentType.MUSIC
                "speech" -> AudioContentType.SPEECH
                "sonification" -> AudioContentType.SONIFICATION
                "movie" -> AudioContentType.MOVIE
                "unknown" -> AudioContentType.UNKNOWN
                else -> AudioContentType.MUSIC
            }
        )

        val automaticallyUpdateNotificationMetadata = playerOptions?.getBoolean(AUTO_UPDATE_METADATA, true) ?: true

        player = QueuedAudioPlayer(this@MusicService, playerConfig, bufferConfig, cacheConfig)
        player.automaticallyUpdateNotificationMetadata = automaticallyUpdateNotificationMetadata
        observeEvents()
        setupForegrounding()
    }

    @MainThread
    fun updateOptions(options: Bundle) {
        val androidOptions = options.getBundle(ANDROID_OPTIONS_KEY)

        appKilledPlaybackBehavior = AppKilledPlaybackBehavior::string.find(androidOptions?.getString(APP_KILLED_PLAYBACK_BEHAVIOR_KEY)) ?: AppKilledPlaybackBehavior.CONTINUE_PLAYBACK

        // TODO: This handles a deprecated flag. Should be removed soon.
        options.getBoolean(STOPPING_APP_PAUSES_PLAYBACK_KEY).let {
            stoppingAppPausesPlayback = options.getBoolean(STOPPING_APP_PAUSES_PLAYBACK_KEY)
            if (stoppingAppPausesPlayback) {
                appKilledPlaybackBehavior = AppKilledPlaybackBehavior.PAUSE_PLAYBACK
            }
        }

        player.playerOptions.alwaysPauseOnInterruption = androidOptions?.getBoolean(PAUSE_ON_INTERRUPTION_KEY) ?: false

        // Prepare capabilities
        val capabilities: MutableList<Capability> = mutableListOf()
        val rawCapabilities = options.getParcelableArrayList<Bundle>("capabilities") ?: emptyList()

        rawCapabilities.forEach {
            val constant = it.getDouble(CAPABILITY_CONSTANT_KEY).toInt()
            when (RNTPCapability.values()[constant]) {
                RNTPCapability.PLAY, RNTPCapability.PAUSE -> capabilities.add(
                    Capability.PLAY_PAUSE(
                        showInNotification = it.getBoolean(SHOW_IN_NOTIFICATION_KEY, true),
                        notificationConfig = it.getBundle(NOTIFICATION_OPTIONS_KEY)?.let { notificationConfig ->
                            NofiticationPlayPauseActionConfig(
                                isCompact = notificationConfig.getBoolean(NOTIFICATION_COMPACT_KEY, false),
                                playIcon = BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY),
                            )
                        },
                    )
                )
                RNTPCapability.PLAY_FROM_ID -> capabilities.add(Capability.PLAY_FROM_ID)
                RNTPCapability.PLAY_FROM_SEARCH -> capabilities.add(Capability.PLAY_FROM_SEARCH)
                RNTPCapability.STOP -> capabilities.add(Capability.STOP(
                    showInNotification = it.getBoolean(SHOW_IN_NOTIFICATION_KEY, true),
                    notificationConfig = it.getBundle(NOTIFICATION_OPTIONS_KEY)?.let { notificationConfig ->
                        NofiticationIconActionConfig(
                            icon = BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY),
                        )
                    },
                ))
                RNTPCapability.SEEK_TO -> capabilities.add(Capability.SEEK_TO)
                RNTPCapability.SKIP -> capabilities.add(Capability.SKIP)
                RNTPCapability.SKIP_TO_NEXT -> capabilities.add(Capability.NEXT(
                    showInNotification = it.getBoolean(SHOW_IN_NOTIFICATION_KEY, true),
                    notificationConfig = it.getBundle(NOTIFICATION_OPTIONS_KEY)?.let { notificationConfig ->
                        NofiticationActionConfig(
                            icon = BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY),
                            isCompact = notificationConfig.getBoolean(NOTIFICATION_COMPACT_KEY, false),
                        )
                    },
                ))
                RNTPCapability.SKIP_TO_PREVIOUS -> capabilities.add(Capability.PREVIOUS(
                    showInNotification = it.getBoolean(SHOW_IN_NOTIFICATION_KEY, true),
                    notificationConfig = it.getBundle(NOTIFICATION_OPTIONS_KEY)?.let { notificationConfig ->
                        NofiticationActionConfig(
                            icon = BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY),
                            isCompact = notificationConfig.getBoolean(NOTIFICATION_COMPACT_KEY, false),
                        )
                    },
                ))
                RNTPCapability.JUMP_FORWARD -> {
                    jumpForwardInterval = it.getInt(JUMP_INTERVAL_KEY, 15)
                    capabilities.add(Capability.FORWARD(
                        showInNotification = it.getBoolean(SHOW_IN_NOTIFICATION_KEY, true),
                        notificationConfig = it.getBundle(NOTIFICATION_OPTIONS_KEY)?.let { notificationConfig ->
                            NofiticationActionConfig(
                                icon = BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY),
                                isCompact = notificationConfig.getBoolean(NOTIFICATION_COMPACT_KEY, false),
                            )
                        },
                    ))
                }
                RNTPCapability.JUMP_BACKWARD -> {
                    jumpBackwardInterval = it.getInt(JUMP_INTERVAL_KEY, 15)
                    capabilities.add(Capability.BACKWARD(
                        showInNotification = it.getBoolean(SHOW_IN_NOTIFICATION_KEY, true),
                        notificationConfig = it.getBundle(NOTIFICATION_OPTIONS_KEY)?.let { notificationConfig ->
                            NofiticationActionConfig(
                                icon = BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY),
                                isCompact = notificationConfig.getBoolean(NOTIFICATION_COMPACT_KEY, false),
                            )
                        },
                    ))
                }
                RNTPCapability.SET_RATING -> {
                    ratingType = it.getInt(RATING_TYPE_KEY, RatingCompat.RATING_NONE)
                }
                else -> {}
            }
        }

        // Check if a pause was present in rawCapabilities so we can extract an icon for it.
        // We then update the icon for the PLAY_PAUSE capability.
        val pauseCapability = rawCapabilities.find { RNTPCapability.values()[it.getInt(CAPABILITY_CONSTANT_KEY)] == RNTPCapability.PAUSE }
        if (pauseCapability != null) {
            capabilities.find { it is Capability.PLAY_PAUSE }?.let {
                val notificationConfig = pauseCapability.getBundle(NOTIFICATION_OPTIONS_KEY)
                if (notificationConfig != null) {
                    (it as Capability.PLAY_PAUSE).notificationConfig?.pauseIcon =
                        BundleUtils.getIconOrNull(this, notificationConfig, NOTIFICATION_ICON_KEY)
                }
            }
        }


        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Add the Uri data so apps can identify that it was a notification click
            data = Uri.parse("trackplayer://notification.click")
            action = Intent.ACTION_VIEW
        }

        val accentColor = BundleUtils.getIntOrNull(options, "color")
        val smallIcon = BundleUtils.getIconOrNull(this, options, "icon")
        val pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, getPendingIntentFlags())
        val notificationConfig = NotificationConfig(accentColor, smallIcon, pendingIntent)
        val capabilitiesConfig = CapabilitiesConfig(capabilities, notificationConfig)

        player.notificationManager.createNotification(capabilitiesConfig)

        // setup progress update events if configured
        progressUpdateJob?.cancel()
        val updateInterval = BundleUtils.getIntOrNull(options, PROGRESS_UPDATE_EVENT_INTERVAL_KEY)
        if (updateInterval != null && updateInterval > 0) {
            progressUpdateJob = scope.launch {
                progressUpdateEventFlow(updateInterval.toLong()).collect { emit(MusicEvents.PLAYBACK_PROGRESS_UPDATED, it) }
            }
        }
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
                putDouble(POSITION_KEY, player.position.toSeconds())
                putDouble(DURATION_KEY, player.duration.toSeconds())
                putDouble(BUFFERED_POSITION_KEY, player.bufferedPosition.toSeconds())
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
        player.seek((seconds * 1000).toLong(), TimeUnit.MILLISECONDS)
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
    fun getPlayerStateBundle(state: AudioPlayerState): Bundle {
        val bundle = Bundle()
        bundle.putString(STATE_KEY, state.asLibState.state)
        if (state == AudioPlayerState.ERROR) {
            bundle.putBundle(ERROR_KEY, getPlaybackErrorBundle())
        }
        return bundle
    }

    @MainThread
    fun updateMetadataForTrack(index: Int, track: Track) {
        player.replaceItem(index, track.toAudioItem())
    }

    @MainThread
    fun updateNotificationMetadata(title: String?, artist: String?, artwork: String?) {
        player.notificationManager.notificationMetadata = NotificationMetadata(title, artist, artwork)
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
        val a = Bundle()
        a.putDouble(POSITION_KEY, oldPosition)
        if (index != null) {
            a.putInt(NEXT_TRACK_KEY, index)
        }

        if (previousIndex != null) {
            a.putInt(TRACK_KEY, previousIndex)
        }

        emit(MusicEvents.PLAYBACK_TRACK_CHANGED, a)

        val b = Bundle()
        b.putDouble("lastPosition", oldPosition)
        if (tracks.isNotEmpty()) {
            b.putInt("index", player.currentIndex)
            b.putBundle("track", tracks[player.currentIndex].originalItem)
            if (previousIndex != null) {
                b.putInt("lastIndex", previousIndex)
                b.putBundle("lastTrack", tracks[previousIndex].originalItem)
            }
        }
        emit(MusicEvents.PLAYBACK_ACTIVE_TRACK_CHANGED, b)
    }

    private fun emitQueueEndedEvent() {
        val bundle = Bundle()
        bundle.putInt(TRACK_KEY, player.currentIndex)
        bundle.putDouble(POSITION_KEY, player.position.toSeconds())
        emit(MusicEvents.PLAYBACK_QUEUE_ENDED, bundle)
    }

    @Suppress("DEPRECATION")
    fun isForegroundService(): Boolean {
        val manager = baseContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (MusicService::class.java.name == service.service.className) {
                return service.foreground
            }
        }
        Timber.e("isForegroundService found no matching service")
        return false
    }

    @MainThread
    private fun setupForegrounding() {
        // Implementation based on https://github.com/Automattic/pocket-casts-android/blob/ee8da0c095560ef64a82d3a31464491b8d713104/modules/services/repositories/src/main/java/au/com/shiftyjelly/pocketcasts/repositories/playback/PlaybackService.kt#L218
        var notificationId: Int? = null
        var notification: Notification? = null
        var stopForegroundWhenNotOngoing = false
        var removeNotificationWhenNotOngoing = false

        fun startForegroundIfNecessary() {
            if (isForegroundService()) {
                Timber.d("skipping foregrounding as the service is already foregrounded")
                return
            }
            if (notification == null) {
                Timber.d("can't startForeground as the notification is null")
                return
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        notificationId!!,
                        notification!!,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(notificationId!!, notification)
                }
                Timber.d("notification has been foregrounded")
            } catch (error: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    error is ForegroundServiceStartNotAllowedException
                ) {
                    Timber.e(
                        "ForegroundServiceStartNotAllowedException: App tried to start a foreground Service when it was not allowed to do so.",
                        error
                    )
                    emit(MusicEvents.PLAYER_ERROR, Bundle().apply {
                        putString("message", error.message)
                        putString("code", "android-foreground-service-start-not-allowed")
                    });
                }
            }
        }

        scope.launch {
            val BACKGROUNDABLE_STATES = listOf(
                AudioPlayerState.IDLE,
                AudioPlayerState.ENDED,
                AudioPlayerState.STOPPED,
                AudioPlayerState.ERROR,
                AudioPlayerState.PAUSED
            )
            val REMOVABLE_STATES = listOf(
                AudioPlayerState.IDLE,
                AudioPlayerState.STOPPED,
                AudioPlayerState.ERROR
            )
            val LOADING_STATES = listOf(
                AudioPlayerState.LOADING,
                AudioPlayerState.READY,
                AudioPlayerState.BUFFERING
            )
            var stateCount = 0
            event.stateChange.collect {
                stateCount++
                if (it in LOADING_STATES) return@collect;
                // Skip initial idle state, since we are only interested when
                // state becomes idle after not being idle
                stopForegroundWhenNotOngoing = stateCount > 1 && it in BACKGROUNDABLE_STATES
                removeNotificationWhenNotOngoing = stopForegroundWhenNotOngoing && it in REMOVABLE_STATES
            }
        }

        scope.launch {
            event.notificationStateChange.collect {
                when (it) {
                    is NotificationState.POSTED -> {
                        Timber.d("notification posted with id=%s, ongoing=%s", it.notificationId, it.ongoing)
                        notificationId = it.notificationId;
                        notification = it.notification;
                        if (it.ongoing) {
                            if (player.playWhenReady) {
                                startForegroundIfNecessary()
                            }
                        } else if (stopForegroundWhenNotOngoing) {
                            if (removeNotificationWhenNotOngoing || isForegroundService()) {
                                @Suppress("DEPRECATION")
                                stopForeground(removeNotificationWhenNotOngoing)
                                Timber.d("stopped foregrounding%s", if (removeNotificationWhenNotOngoing) " and removed notification" else "")
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    @MainThread
    private fun observeEvents() {
        scope.launch {
            event.stateChange.collect {
                emit(MusicEvents.PLAYBACK_STATE, getPlayerStateBundle(it))

                if (it == AudioPlayerState.ENDED && player.nextItem == null) {
                    emitQueueEndedEvent()
                    emitPlaybackTrackChangedEvents(null, player.currentIndex, player.position.toSeconds())
                }
            }
        }

        scope.launch {
            event.audioItemTransition.collect {
                if (it !is AudioItemTransitionReason.REPEAT) {
                    emitPlaybackTrackChangedEvents(
                        player.currentIndex,
                        player.previousIndex,
                        (it?.oldPosition ?: 0).toSeconds()
                    )
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
                            putInt("interval", jumpForwardInterval)
                            emit(MusicEvents.BUTTON_JUMP_FORWARD, this)
                        }
                    }
                    MediaSessionCallback.REWIND -> {
                        Bundle().apply {
                            putInt("interval", jumpBackwardInterval)
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

    private fun getPlaybackErrorBundle(): Bundle {
        val bundle = Bundle()
        if (playbackError?.message != null) {
            bundle.putString("message", playbackError!!.message)
        }
        if (playbackError?.code != null) {
            bundle.putString("code", "android-" + playbackError!!.code)
        }
        return bundle
    }

    @MainThread
    private fun emit(event: String?, data: Bundle? = null) {
        val intent = Intent(EVENT_INTENT)
        intent.putExtra(EVENT_KEY, event)
        if (data != null) intent.putExtra(DATA_KEY, data)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun getTaskConfig(intent: Intent?): HeadlessJsTaskConfig {
        return HeadlessJsTaskConfig(TASK_KEY, Arguments.createMap(), 0, true)
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

    companion object {
        const val EMPTY_NOTIFICATION_ID = 1
        const val STATE_KEY = "state"
        const val ERROR_KEY  = "error"
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

        const val RATING_TYPE_KEY = "ratingType"
        const val CAPABILITY_CONSTANT_KEY = "constant"
        const val SHOW_IN_NOTIFICATION_KEY = "showInNotification"
        const val NOTIFICATION_OPTIONS_KEY = "notificationOptions"
        const val NOTIFICATION_COMPACT_KEY = "compact"
        const val NOTIFICATION_ICON_KEY = "icon"
        const val JUMP_INTERVAL_KEY = "jumpInterval"
        const val PROGRESS_UPDATE_EVENT_INTERVAL_KEY = "progressUpdateEventInterval"

        const val MAX_CACHE_SIZE_KEY = "maxCacheSize"

        const val ANDROID_OPTIONS_KEY = "android"

        const val STOPPING_APP_PAUSES_PLAYBACK_KEY = "stoppingAppPausesPlayback"
        const val APP_KILLED_PLAYBACK_BEHAVIOR_KEY = "appKilledPlaybackBehavior"
        const val PAUSE_ON_INTERRUPTION_KEY = "alwaysPauseOnInterruption"
        const val AUTO_UPDATE_METADATA = "autoUpdateMetadata"
        const val AUTO_HANDLE_INTERRUPTIONS = "autoHandleInterruptions"
        const val ANDROID_AUDIO_CONTENT_TYPE = "androidAudioContentType"
        const val IS_FOCUS_LOSS_PERMANENT_KEY = "permanent"
        const val IS_PAUSED_KEY = "paused"

        const val DEFAULT_JUMP_INTERVAL = 15.0
    }
}
