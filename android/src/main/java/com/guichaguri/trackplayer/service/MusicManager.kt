package com.guichaguri.trackplayer.service

import android.annotation.SuppressLint
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.guichaguri.trackplayer.service.MusicBinder
import com.guichaguri.trackplayer.module.MusicEvents
import com.guichaguri.trackplayer.service.MusicService
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.RatingCompat
import com.google.android.exoplayer2.Player
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableArray
import com.guichaguri.trackplayer.service.player.ExoPlayback
import com.guichaguri.trackplayer.service.models.NowPlayingMetadata
import com.google.android.exoplayer2.C
import com.guichaguri.trackplayer.service.models.TrackMetadata
import com.guichaguri.trackplayer.service.models.TrackType
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaDescriptionCompat
import com.guichaguri.trackplayer.service.player.LocalPlayback
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.guichaguri.trackplayer.service.MusicManager
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.ExoPlaybackException
import com.guichaguri.trackplayer.service.player.SourceMetadata
import com.google.android.exoplayer2.Player.MetadataComponent
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame
import com.google.android.exoplayer2.metadata.icy.IcyHeaders
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.metadata.flac.VorbisComment
import com.google.android.exoplayer2.extractor.mp4.MdtaMetadataEntry
import android.provider.MediaStore
import com.bumptech.glide.request.target.SimpleTarget
import android.graphics.Bitmap
import com.guichaguri.trackplayer.R
import android.app.NotificationManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.Glide
import androidx.media.session.MediaButtonReceiver
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper
import com.guichaguri.trackplayer.service.metadata.ButtonEvents
import android.app.PendingIntent
import android.app.NotificationChannel
import android.content.*
import com.guichaguri.trackplayer.service.metadata.MetadataManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.PowerManager.WakeLock
import android.net.wifi.WifiManager.WifiLock
import androidx.annotation.RequiresApi
import android.media.AudioFocusRequest
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import com.google.android.exoplayer2.audio.AudioAttributes
import com.guichaguri.trackplayer.service.models.Track

/**
 * @author Guichaguri
 */
class MusicManager @SuppressLint("InvalidWakeLockTag") constructor(private val service: MusicService) :
    OnAudioFocusChangeListener {
    private val wakeLock: WakeLock
    private val wifiLock: WifiLock
    val metadata: MetadataManager
    var playback: ExoPlayback<*>? = null
        private set

    @RequiresApi(26)
    private var focus: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var wasDucking = false
    private val noisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            service.emit(MusicEvents.Companion.BUTTON_PAUSE, null)
        }
    }
    private var receivingNoisyEvents = false
    private var stopWithApp = false
    private var alwaysPauseOnInterruption = false
    fun shouldStopWithApp(): Boolean {
        return stopWithApp
    }

    fun setStopWithApp(stopWithApp: Boolean) {
        this.stopWithApp = stopWithApp
    }

    fun setAlwaysPauseOnInterruption(alwaysPauseOnInterruption: Boolean) {
        this.alwaysPauseOnInterruption = alwaysPauseOnInterruption
    }

    val handler: Handler?
        get() = service.handler

    fun switchPlayback(playback: ExoPlayback<*>?) {
        if (this.playback != null) {
            this.playback!!.stop()
            this.playback!!.destroy()
        }
        this.playback = playback
        if (this.playback != null) {
            this.playback!!.initialize()
        }
    }

    fun createLocalPlayback(options: Bundle?): LocalPlayback {
        val autoUpdateMetadata = options!!.getBoolean("autoUpdateMetadata", true)
        val minBuffer = Utils.toMillis(
            options.getDouble(
                "minBuffer",
                Utils.toSeconds(DefaultLoadControl.DEFAULT_MIN_BUFFER_MS.toLong())
            )
        ).toInt()
        val maxBuffer = Utils.toMillis(
            options.getDouble(
                "maxBuffer",
                Utils.toSeconds(DefaultLoadControl.DEFAULT_MAX_BUFFER_MS.toLong())
            )
        ).toInt()
        val playBuffer = Utils.toMillis(
            options.getDouble(
                "playBuffer",
                Utils.toSeconds(DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS.toLong())
            )
        ).toInt()
        val backBuffer = Utils.toMillis(
            options.getDouble(
                "backBuffer",
                Utils.toSeconds(DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS.toLong())
            )
        ).toInt()
        val cacheMaxSize = (options.getDouble("maxCacheSize", 0.0) * 1024).toLong()
        val multiplier =
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS
        val control: LoadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(minBuffer, maxBuffer, playBuffer, playBuffer * multiplier)
            .setBackBuffer(backBuffer, false)
            .createDefaultLoadControl()
        val player = SimpleExoPlayer.Builder(service)
            .setLoadControl(control)
            .build()
        player.audioAttributes = AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build()
        return LocalPlayback(service, this, player, cacheMaxSize, autoUpdateMetadata)
    }

    @SuppressLint("WakelockTimeout")
    fun onPlay() {
        Log.d(Utils.LOG, "onPlay")
        if (playback == null) return
        val track = playback!!.currentTrack ?: return
        if (!playback!!.isRemote) {
            requestFocus()
            if (!receivingNoisyEvents) {
                receivingNoisyEvents = true
                service.registerReceiver(
                    noisyReceiver,
                    IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                )
            }
            if (!wakeLock.isHeld) wakeLock.acquire()
            if (!Utils.isLocal(track.uri)) {
                if (!wifiLock.isHeld) wifiLock.acquire()
            }
        }
        if (playback!!.shouldAutoUpdateMetadata()) metadata.setActive(true)
    }

    fun onPause() {
        Log.d(Utils.LOG, "onPause")

        // Unregisters the noisy receiver
        if (receivingNoisyEvents) {
            service.unregisterReceiver(noisyReceiver)
            receivingNoisyEvents = false
        }

        // Release the wake and the wifi locks
        if (wakeLock.isHeld) wakeLock.release()
        if (wifiLock.isHeld) wifiLock.release()
        if (playback!!.shouldAutoUpdateMetadata()) metadata.setActive(true)
    }

    fun onStop() {
        Log.d(Utils.LOG, "onStop")

        // Unregisters the noisy receiver
        if (receivingNoisyEvents) {
            service.unregisterReceiver(noisyReceiver)
            receivingNoisyEvents = false
        }

        // Release the wake and the wifi locks
        if (wakeLock.isHeld) wakeLock.release()
        if (wifiLock.isHeld) wifiLock.release()
        abandonFocus()
        if (playback!!.shouldAutoUpdateMetadata()) metadata.setActive(false)
    }

    fun onStateChange(state: Int) {
        Log.d(Utils.LOG, "onStateChange")
        val bundle = Bundle()
        bundle.putInt("state", state)
        service.emit(MusicEvents.Companion.PLAYBACK_STATE, bundle)
        if (playback!!.shouldAutoUpdateMetadata()) metadata.updatePlayback(playback)
    }

    fun onTrackUpdate(prevIndex: Int?, prevPos: Long, nextIndex: Int?, next: Track?) {
        Log.d(Utils.LOG, "onTrackUpdate")
        if (playback!!.shouldAutoUpdateMetadata() && next != null) metadata.updateMetadata(
            playback,
            next
        )
        val bundle = Bundle()
        if (prevIndex != null) bundle.putInt("track", prevIndex)
        bundle.putDouble("position", Utils.toSeconds(prevPos))
        if (nextIndex != null) bundle.putInt("nextTrack", nextIndex)
        service.emit(MusicEvents.Companion.PLAYBACK_TRACK_CHANGED, bundle)
    }

    fun onReset() {
        metadata.removeNotifications()
    }

    fun onEnd(previousIndex: Int?, prevPos: Long) {
        Log.d(Utils.LOG, "onEnd")
        val bundle = Bundle()
        if (previousIndex != null) bundle.putInt("track", previousIndex)
        bundle.putDouble("position", Utils.toSeconds(prevPos))
        service.emit(MusicEvents.Companion.PLAYBACK_QUEUE_ENDED, bundle)
    }

    fun onMetadataReceived(
        source: String,
        title: String?,
        url: String?,
        artist: String?,
        album: String?,
        date: String?,
        genre: String?
    ) {
        Log.d(Utils.LOG, "onMetadataReceived: $source")
        val bundle = Bundle()
        bundle.putString("source", source)
        bundle.putString("title", title)
        bundle.putString("url", url)
        bundle.putString("artist", artist)
        bundle.putString("album", album)
        bundle.putString("date", date)
        bundle.putString("genre", genre)
        service.emit(MusicEvents.Companion.PLAYBACK_METADATA, bundle)
    }

    fun onError(code: String, error: String?) {
        Log.d(Utils.LOG, "onError")
        Log.e(Utils.LOG, "Playback error: $code - $error")
        val bundle = Bundle()
        bundle.putString("code", code)
        bundle.putString("message", error)
        service.emit(MusicEvents.Companion.PLAYBACK_ERROR, bundle)
    }

    override fun onAudioFocusChange(focus: Int) {
        Log.d(Utils.LOG, "onDuck")
        var permanent = false
        var paused = false
        var ducking = false
        when (focus) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                permanent = true
                abandonFocus()
                paused = true
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> paused = true
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (alwaysPauseOnInterruption) paused =
                true else ducking = true
            else -> {
            }
        }
        if (ducking) {
            playback!!.volumeMultiplier = 0.5f
            wasDucking = true
        } else if (wasDucking) {
            playback!!.volumeMultiplier = 1.0f
            wasDucking = false
        }
        val bundle = Bundle()
        bundle.putBoolean("permanent", permanent)
        bundle.putBoolean("paused", paused)
        service.emit(MusicEvents.Companion.BUTTON_DUCK, bundle)
    }

    private fun requestFocus() {
        if (hasAudioFocus) return
        Log.d(Utils.LOG, "Requesting audio focus...")
        val manager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val r: Int

        if (manager == null) {
            r = AudioManager.AUDIOFOCUS_REQUEST_FAILED
        } else if (Build.VERSION.SDK_INT >= 26) {
            focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(this)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setWillPauseWhenDucked(alwaysPauseOnInterruption)
                .build()
            r = manager.requestAudioFocus(focus!!)
        } else {
            r = manager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        hasAudioFocus = r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (!hasAudioFocus) return
        Log.d(Utils.LOG, "Abandoning audio focus...")
        val manager = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val r: Int
        r = if (manager == null) {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED
        } else if (Build.VERSION.SDK_INT >= 26) {
            manager.abandonAudioFocusRequest(focus!!)
        } else {
            manager.abandonAudioFocus(this)
        }
        hasAudioFocus = r != AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun destroy() {
        Log.d(Utils.LOG, "Releasing service resources...")

        // Disable audio focus
        abandonFocus()

        // Stop receiving audio becoming noisy events
        if (receivingNoisyEvents) {
            service.unregisterReceiver(noisyReceiver)
            receivingNoisyEvents = false
        }

        // Release the playback resources
        if (playback != null) playback!!.destroy()

        // Release the metadata resources
        metadata.destroy()

        // Release the locks
        if (wifiLock.isHeld) wifiLock.release()
        if (wakeLock.isHeld) wakeLock.release()
    }

    init {
        metadata = MetadataManager(service, this)
        val powerManager = service.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track-player-wake-lock")
        wakeLock.setReferenceCounted(false)

        // Android 7: Use the application context here to prevent any memory leaks
        val wifiManager =
            service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "track-player-wifi-lock")
        wifiLock.setReferenceCounted(false)
    }
}