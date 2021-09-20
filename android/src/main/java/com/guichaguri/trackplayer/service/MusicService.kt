package com.guichaguri.trackplayer.service

import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.guichaguri.trackplayer.service.MusicBinder
import com.guichaguri.trackplayer.module.MusicEvents
import com.guichaguri.trackplayer.service.MusicService
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.RatingCompat
import com.google.android.exoplayer2.Player
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
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.*

/**
 * @author Guichaguri
 */
class MusicService : HeadlessJsTaskService() {
    var manager: MusicManager? = null
    var handler: Handler? = null
    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        return HeadlessJsTaskConfig("TrackPlayer", Arguments.createMap(), 0, true)
    }

    override fun onHeadlessJsTaskFinish(taskId: Int) {
        // Overridden to prevent the service from being terminated
    }

    fun emit(event: String?, data: Bundle?) {
        val intent = Intent(Utils.EVENT_INTENT)
        intent.putExtra("event", event)
        if (data != null) intent.putExtra("data", data)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun destroy() {
        if (handler != null) {
            handler!!.removeMessages(0)
            handler = null
        }
        if (manager != null) {
            manager!!.destroy()
            manager = null
        }
    }

    private fun onStartForeground() {
        var serviceForeground = false
        if (manager != null) {
            // The session is only active when the service is on foreground
            serviceForeground = manager!!.metadata.session.isActive
        }
        if (!serviceForeground) {
            val reactInstanceManager = reactNativeHost.reactInstanceManager
            val reactContext = reactInstanceManager.currentReactContext

            // Checks whether there is a React activity
            if (reactContext == null || !reactContext.hasCurrentActivity()) {
                val channel = Utils.getNotificationChannel(this as Context)

                // Sets the service to foreground with an empty notification
                startForeground(1, NotificationCompat.Builder(this, channel!!).build())
                // Stops the service right after
                stopSelf()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return if (Utils.CONNECT_INTENT == intent.action) {
            MusicBinder(this, manager)
        } else super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null && Intent.ACTION_MEDIA_BUTTON == intent.action) {
            // Check if the app is on background, then starts a foreground service and then ends it right after
            onStartForeground()
            if (manager != null) {
                MediaButtonReceiver.handleIntent(manager!!.metadata.session, intent)
            }
            return START_NOT_STICKY
        }
        manager = MusicManager(this)
        handler = Handler()
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val channel = Utils.getNotificationChannel(this as Context)
        startForeground(1, NotificationCompat.Builder(this, channel!!).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy()
        stopForeground(true)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        if (manager == null || manager!!.shouldStopWithApp()) {
            if (manager != null) {
                manager?.playback?.stop()
            }
            destroy()
            stopSelf()
        }
    }
}