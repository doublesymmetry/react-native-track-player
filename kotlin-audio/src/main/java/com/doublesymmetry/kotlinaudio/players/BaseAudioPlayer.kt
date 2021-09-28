package com.doublesymmetry.kotlinaudio.players

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.net.Uri
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioAttributesCompat.CONTENT_TYPE_MUSIC
import androidx.media.AudioAttributesCompat.USAGE_MEDIA
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import androidx.media.AudioManagerCompat.AUDIOFOCUS_GAIN
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.notification.NotificationManager
import com.doublesymmetry.kotlinaudio.utils.isJUnitTest
import com.doublesymmetry.kotlinaudio.utils.isUriLocal
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.DefaultLoadControl.*
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class BaseAudioPlayer internal constructor(private val context: Context, bufferConfig: BufferConfig? = null) : AudioManager.OnAudioFocusChangeListener {
    protected val exoPlayer: SimpleExoPlayer
    val notificationManager: NotificationManager

    open val playerOptions: PlayerOptions = PlayerOptionsImpl()

    protected val scope = CoroutineScope(Dispatchers.Main)

    val duration: Long
        get() {
            return if (exoPlayer.duration == C.TIME_UNSET) 0
            else exoPlayer.duration
        }

    val position: Long
        get() {
            return if (exoPlayer.currentPosition == C.POSITION_UNSET.toLong()) 0
            else exoPlayer.currentPosition
        }

    val bufferedPosition: Long
        get() {
            return if (exoPlayer.bufferedPosition == C.POSITION_UNSET.toLong()) 0
            else exoPlayer.bufferedPosition
        }

    var volume: Float
        get() = exoPlayer.volume
        set(value) {
            exoPlayer.volume = value * volumeMultiplier
        }

    var playbackSpeed: Float
        get() = exoPlayer.playbackParameters.speed
        set(value) {
            exoPlayer.setPlaybackSpeed(value)
        }

    private var volumeMultiplier = 1f
        private set(value) {
            field = value
            volume = volume
        }

    val isPlaying
        get() = exoPlayer.isPlaying

    val event = EventHolder()

    private var focus: AudioFocusRequestCompat? = null
    private var hasAudioFocus = false
    private var wasDucking = false

    init {
        val exoPlayerBuilder = SimpleExoPlayer.Builder(context)

        bufferConfig?.let {
            val multiplier = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / DEFAULT_BUFFER_FOR_PLAYBACK_MS
            val minBuffer = if (it.minBuffer != null && it.minBuffer != 0) it.minBuffer else DEFAULT_MIN_BUFFER_MS
            val maxBuffer = if (it.maxBuffer != null && it.maxBuffer != 0) it.maxBuffer else DEFAULT_MAX_BUFFER_MS
            val playBuffer = if (it.playBuffer != null && it.playBuffer != 0) it.playBuffer else DEFAULT_BUFFER_FOR_PLAYBACK_MS
            val backBuffer = if (it.backBuffer != null && it.playBuffer != 0) it.backBuffer else DEFAULT_BACK_BUFFER_DURATION_MS

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(minBuffer, maxBuffer, playBuffer, playBuffer * multiplier)
                .setBackBuffer(backBuffer, false)
                .build()

            exoPlayerBuilder.setLoadControl(loadControl)
        }

        exoPlayer = exoPlayerBuilder.build()
        notificationManager = NotificationManager(context, exoPlayer)

        if (isJUnitTest()) {
            exoPlayer.setThrowsWhenUsingWrongThread(false)
        }

        exoPlayer.addListener(PlayerListener())
        observeEvents()
    }

    /**
     * Will replace the current item with a new one and load it into the player.
     * @param item The [AudioItem] to replace the current one.
     * @param playWhenReady If this is `true` it will automatically start playback. Default is `true`.
     */
    open fun load(item: AudioItem, playWhenReady: Boolean = true) {
        val mediaSource = getMediaSourceFromAudioItem(item)

        exoPlayer.addMediaSource(mediaSource)
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()
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
        notificationManager.onPlay()
    }

    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Stops and destroys the player. Only call this when you are finished using the player, otherwise use [pause].
     */
    @CallSuper
    open fun destroy() {
        abandonAudioFocus()
        notificationManager.destroy()
        exoPlayer.release()
    }

    fun seek(duration: Long, unit: TimeUnit) {
        val millis = TimeUnit.MILLISECONDS.convert(duration, unit)

        exoPlayer.seekTo(millis)
    }

    private fun getMediaItemFromAudioItem(audioItem: AudioItem): MediaItem {
        return MediaItem.Builder().setUri(audioItem.audioUrl).setTag(audioItem).build()
    }

    protected fun getMediaSourceFromAudioItem(audioItem: AudioItem): MediaSource {
        val factory: DataSource.Factory
        val uri = Uri.parse(audioItem.audioUrl)
        val mediaItem = getMediaItemFromAudioItem(audioItem)

        val userAgent = if (audioItem.options == null || audioItem.options!!.userAgent.isNullOrBlank()) {
            Util.getUserAgent(context, APPLICATION_NAME)
        } else {
            audioItem.options!!.userAgent
        }

        factory = when {
            audioItem.options?.resourceId != null -> {
                val raw = RawResourceDataSource(context)
                raw.open(DataSpec(uri))
                DataSource.Factory { raw }
            }
            isUriLocal(uri) -> {
                DefaultDataSourceFactory(context, userAgent)
            }
            else -> {
                val tempFactory = DefaultHttpDataSource.Factory().apply {
                    setUserAgent(userAgent)
                    setAllowCrossProtocolRedirects(true)

                    audioItem.options?.headers?.let {
                        setDefaultRequestProperties(it.toMap())
                    }
                }

                tempFactory
            }

            //TODO: Do we need this?
//        enableCaching()
        }

        return when (audioItem.type) {
            MediaType.DASH -> createDashSource(mediaItem, factory)
            MediaType.HLS -> createHlsSource(mediaItem, factory)
            MediaType.SMOOTH_STREAMING -> createSsSource(mediaItem, factory)
            else -> createProgressiveSource(mediaItem, factory)
        }
    }

    private fun createDashSource(mediaItem: MediaItem, factory: DataSource.Factory?): MediaSource {
        return DashMediaSource.Factory(DefaultDashChunkSource.Factory(factory!!), factory)
            .createMediaSource(mediaItem)
    }

    private fun createHlsSource(mediaItem: MediaItem, factory: DataSource.Factory?): MediaSource {
        return HlsMediaSource.Factory(factory!!)
            .createMediaSource(mediaItem)
    }

    private fun createSsSource(mediaItem: MediaItem, factory: DataSource.Factory?): MediaSource {
        return SsMediaSource.Factory(DefaultSsChunkSource.Factory(factory!!), factory)
            .createMediaSource(mediaItem)
    }

    private fun createProgressiveSource(mediaItem: MediaItem, factory: DataSource.Factory): ProgressiveMediaSource {
        return ProgressiveMediaSource.Factory(
            factory, DefaultExtractorsFactory()
                .setConstantBitrateSeekingEnabled(true)
        )
            .createMediaSource(mediaItem)
    }

//
//    fun enableCaching(factory: DataSource.Factory): DataSource.Factory {
//        return if (cache == null || cacheMaxSize <= 0) factory else CacheDataSourceFactory(
//            cache!!,
//            factory,
//            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
//        )
//    }

    private fun requestAudioFocus() {
        if (hasAudioFocus) return
        Timber.d("Requesting audio focus...")

        val manager = ContextCompat.getSystemService(context, AudioManager::class.java)

        focus = AudioFocusRequestCompat.Builder(AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(this)
            .setAudioAttributes(AudioAttributesCompat.Builder()
                .setUsage(USAGE_MEDIA)
                .setContentType(CONTENT_TYPE_MUSIC)
                .build())
            .setWillPauseWhenDucked(playerOptions.alwaysPauseOnInterruption)
            .build()

        val result: Int = if (manager != null && focus != null) {
            AudioManagerCompat.requestAudioFocus(manager, focus!!)
        } else {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        Timber.d("Abandoning audio focus...")

        val manager = ContextCompat.getSystemService(context, AudioManager::class.java)

        val result: Int = if (manager != null && focus != null) {
            AudioManagerCompat.abandonAudioFocusRequest(manager, focus!!)
        } else {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        hasAudioFocus = (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Timber.d("Audio focus changed")

        var isPermanent = false
        var isPaused = false
        var isDucking = false

        when (focusChange) {
            AUDIOFOCUS_LOSS -> {
                isPermanent = true
                abandonAudioFocus()
                isPaused = true
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> isPaused = true
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (playerOptions.alwaysPauseOnInterruption) isPaused = true else isDucking = true
        }

        if (isDucking) {
            volumeMultiplier = 0.5f
            wasDucking = true
        } else if (wasDucking) {
            volumeMultiplier = 1f
            wasDucking = false
        }

        event.updateOnAudioFocusChanged(isPaused, isPermanent)
    }

    private fun observeEvents() {
        scope.launch {
            notificationManager.onNotificationAction.collect {
                event.updateOnNotificationAction(it)
            }
        }
    }

    companion object {
        const val APPLICATION_NAME = "react-native-track-player"
    }

    inner class PlayerListener: Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> event.updateAudioPlayerState(AudioPlayerState.BUFFERING)
                Player.STATE_READY -> event.updateAudioPlayerState(AudioPlayerState.READY)
                Player.STATE_IDLE -> event.updateAudioPlayerState(AudioPlayerState.IDLE)
                Player.STATE_ENDED -> event.updateAudioPlayerState(AudioPlayerState.ENDED)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> event.updateAudioItemTransition(AudioItemTransitionReason.AUTO)
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> event.updateAudioItemTransition(AudioItemTransitionReason.QUEUE_CHANGED)
                Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> event.updateAudioItemTransition(AudioItemTransitionReason.REPEAT)
                Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> event.updateAudioItemTransition(AudioItemTransitionReason.SEEK_TO_ANOTHER_AUDIO_ITEM)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                requestAudioFocus()
                event.updateAudioPlayerState(AudioPlayerState.PLAYING)
            } else {
                abandonAudioFocus()
                event.updateAudioPlayerState(AudioPlayerState.PAUSED)
            }
        }
    }
}