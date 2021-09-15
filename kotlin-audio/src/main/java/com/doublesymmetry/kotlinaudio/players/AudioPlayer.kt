package com.doublesymmetry.kotlinaudio.players

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.doublesymmetry.kotlinaudio.DescriptionAdapter
import com.doublesymmetry.kotlinaudio.R
import com.doublesymmetry.kotlinaudio.models.*
import com.doublesymmetry.kotlinaudio.utils.isJUnitTest
import com.doublesymmetry.kotlinaudio.utils.isUriLocal
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultLoadControl.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import java.util.concurrent.TimeUnit

open class AudioPlayer(private val context: Context, bufferOptions: BufferOptions? = null) {
    protected val exoPlayer: SimpleExoPlayer

    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "AudioPlayerSession")
    private val mediaSessionConnector: MediaSessionConnector = MediaSessionConnector(mediaSession)

    private val playerNotificationManager: PlayerNotificationManager
    private val descriptionAdapter = DescriptionAdapter(context, null)

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
            exoPlayer.volume = value
        }

    var rate: Float
        get() = exoPlayer.playbackParameters.speed
        set(value) {
            exoPlayer.setPlaybackSpeed(value)
        }

    val event = EventHolder()

    init {
        val exoPlayerBuilder = SimpleExoPlayer.Builder(context)

        bufferOptions?.let {
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

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }

        if (isJUnitTest()) {
            exoPlayer.setThrowsWhenUsingWrongThread(false)
        }

        mediaSessionConnector.setPlayer(exoPlayer)

        val builder = PlayerNotificationManager.Builder(context, NOTIFICATION_ID, channelId)

        playerNotificationManager = builder
            .setMediaDescriptionAdapter(descriptionAdapter)
            .build()

        if (!isJUnitTest()) {
            playerNotificationManager.apply {
                setPlayer(exoPlayer)
                setMediaSessionToken(mediaSession.sessionToken)
                setUseNextActionInCompactView(true)
                setUsePreviousActionInCompactView(true)
            }
        }

        addPlayerListener()
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

        mediaSession.isActive = true
    }

    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Stops and destroys the player. Only call this when you are finished using the player, otherwise use [pause].
     */
    @CallSuper
    open fun destroy() {
        descriptionAdapter.release()
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

    private fun addPlayerListener() {
        exoPlayer.addListener(object : Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    STATE_BUFFERING -> event.updateAudioPlayerState(AudioPlayerState.BUFFERING)
                    STATE_READY -> event.updateAudioPlayerState(AudioPlayerState.READY)
                    STATE_IDLE -> event.updateAudioPlayerState(AudioPlayerState.IDLE)
                    STATE_ENDED -> event.updateAudioPlayerState(AudioPlayerState.ENDED)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                when (reason) {
                    MEDIA_ITEM_TRANSITION_REASON_AUTO -> event.updateAudioItemTransition(AudioItemTransitionReason.AUTO)
                    MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> event.updateAudioItemTransition(AudioItemTransitionReason.QUEUE_CHANGED)
                    MEDIA_ITEM_TRANSITION_REASON_REPEAT -> event.updateAudioItemTransition(AudioItemTransitionReason.REPEAT)
                    MEDIA_ITEM_TRANSITION_REASON_SEEK -> event.updateAudioItemTransition(AudioItemTransitionReason.SEEK_TO_ANOTHER_AUDIO_ITEM)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) event.updateAudioPlayerState(AudioPlayerState.PLAYING)
                else event.updateAudioPlayerState(AudioPlayerState.PAUSED)
            }
        })
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "kotlin_audio_player"
        const val APPLICATION_NAME = "react-native-track-player"
    }
}