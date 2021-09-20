package com.guichaguri.trackplayer.service.models

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
import com.guichaguri.trackplayer.service.Utils

abstract class TrackMetadata {
    var artwork: Uri? = null
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var date: String? = null
    var genre: String? = null
    var duration: Long = 0
    var rating: RatingCompat? = null
    open fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        artwork = Utils.getUri(context, bundle, "artwork")
        title = bundle!!.getString("title")
        artist = bundle.getString("artist")
        album = bundle.getString("album")
        date = bundle.getString("date")
        genre = bundle.getString("genre")
        duration = Utils.toMillis(bundle.getDouble("duration", 0.0))
        rating = Utils.getRating(bundle, "rating", ratingType)
    }

    open fun toMediaMetadata(): MediaMetadataCompat.Builder? {
        val builder = MediaMetadataCompat.Builder()
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
        builder.putString(MediaMetadataCompat.METADATA_KEY_DATE, date)
        builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
        if (duration > 0) {
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }
        if (artwork != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artwork.toString())
        }
        if (rating != null) {
            builder.putRating(MediaMetadataCompat.METADATA_KEY_RATING, rating)
        }
        return builder
    }
}