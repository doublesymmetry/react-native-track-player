package com.guichaguri.trackplayer.service

import com.facebook.react.bridge.ReactContext
import android.os.Bundle
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.guichaguri.trackplayer.service.MusicBinder
import com.guichaguri.trackplayer.module.MusicEvents
import android.os.IBinder
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
import android.os.Build
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
import android.net.Uri
import android.os.PowerManager
import android.net.wifi.WifiManager
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import java.lang.NumberFormatException
import java.lang.RuntimeException

/**
 * @author Guichaguri
 */
object Utils {
    const val EVENT_INTENT = "com.guichaguri.trackplayer.event"
    const val CONNECT_INTENT = "com.guichaguri.trackplayer.connect"
    const val NOTIFICATION_CHANNEL = "com.guichaguri.trackplayer"
    const val LOG = "RNTrackPlayer"
    fun toRunnable(promise: Promise): Runnable {
        return Runnable { promise.resolve(null) }
    }

    fun toMillis(seconds: Double): Long {
        return (seconds * 1000).toLong()
    }

    fun toSeconds(millis: Long): Double {
        return millis / 1000.0
    }

    fun isLocal(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme
        val host = uri.host
        return scheme == null || scheme == ContentResolver.SCHEME_FILE || scheme == ContentResolver.SCHEME_ANDROID_RESOURCE || scheme == ContentResolver.SCHEME_CONTENT || scheme == RawResourceDataSource.RAW_RESOURCE_SCHEME || scheme == "res" || host == null || host == "localhost" || host == "127.0.0.1" || host == "[::1]"
    }

    fun getUri(context: Context, data: Bundle?, key: String?): Uri? {
        if (!data!!.containsKey(key)) return null
        val obj = data[key]
        if (obj is String) {
            // Remote or Local Uri
            if (obj.trim { it <= ' ' }.isEmpty()) throw RuntimeException("The URL cannot be empty")
            return Uri.parse(obj as String?)
        } else if (obj is Bundle) {
            // require/import
            val uri = obj.getString("uri")
            val helper = ResourceDrawableIdHelper.getInstance()
            val id = helper.getResourceDrawableId(context, uri)
            return if (id > 0) {
                // In production, we can obtain the resource uri
                val res = context.resources
                Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(res.getResourcePackageName(id))
                    .appendPath(res.getResourceTypeName(id))
                    .appendPath(res.getResourceEntryName(id))
                    .build()
            } else {
                // During development, the resources might come directly from the metro server
                Uri.parse(uri)
            }
        }
        return null
    }

    fun getRawResourceId(context: Context, data: Bundle, key: String?): Int {
        if (!data.containsKey(key)) return 0
        val obj = data[key] as? Bundle ?: return 0
        var name = obj.getString("uri")
        if (name == null || name.isEmpty()) return 0
        name = name.toLowerCase().replace("-", "_")
        return try {
            name.toInt()
        } catch (ex: NumberFormatException) {
            context.resources.getIdentifier(name, "raw", context.packageName)
        }
    }

    fun isPlaying(state: Int): Boolean {
        return state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING
    }

    fun isPaused(state: Int): Boolean {
        return state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_CONNECTING
    }

    fun isStopped(state: Int): Boolean {
        return state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED
    }

    fun getRating(data: Bundle?, key: String?, ratingType: Int): RatingCompat {
        return if (!data!!.containsKey(key) || ratingType == RatingCompat.RATING_NONE) {
            RatingCompat.newUnratedRating(ratingType)
        } else if (ratingType == RatingCompat.RATING_HEART) {
            RatingCompat.newHeartRating(data.getBoolean(key, true))
        } else if (ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            RatingCompat.newThumbRating(data.getBoolean(key, true))
        } else if (ratingType == RatingCompat.RATING_PERCENTAGE) {
            RatingCompat.newPercentageRating(data.getFloat(key, 0f))
        } else {
            RatingCompat.newStarRating(ratingType, data.getFloat(key, 0f))
        }
    }

    fun setRating(data: Bundle, key: String?, rating: RatingCompat) {
        if (!rating.isRated) return
        val ratingType = rating.ratingStyle
        if (ratingType == RatingCompat.RATING_HEART) {
            data.putBoolean(key, rating.hasHeart())
        } else if (ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            data.putBoolean(key, rating.isThumbUp)
        } else if (ratingType == RatingCompat.RATING_PERCENTAGE) {
            data.putDouble(key, rating.percentRating.toDouble())
        } else {
            data.putDouble(key, rating.starRating.toDouble())
        }
    }

    fun getInt(data: Bundle?, key: String?, defaultValue: Int): Int {
        val value = data!![key]
        return if (value is Number) {
            value.toInt()
        } else defaultValue
    }

    fun getNotificationChannel(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                "MusicService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.setShowBadge(false)
            channel.setSound(null, null)
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
        return NOTIFICATION_CHANNEL
    }
}