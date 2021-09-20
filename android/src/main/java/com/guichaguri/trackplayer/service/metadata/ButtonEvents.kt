package com.guichaguri.trackplayer.service.metadata

import android.annotation.SuppressLint
import com.facebook.react.bridge.ReactContext
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import android.content.ServiceConnection
import com.guichaguri.trackplayer.service.MusicBinder
import com.guichaguri.trackplayer.module.MusicEvents
import android.content.IntentFilter
import android.content.ComponentName
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
import android.content.ContentResolver
import android.app.NotificationChannel
import com.guichaguri.trackplayer.service.metadata.MetadataManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.PowerManager.WakeLock
import android.net.wifi.WifiManager.WifiLock
import androidx.annotation.RequiresApi
import android.media.AudioFocusRequest
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import android.media.AudioManager
import android.os.PowerManager
import android.net.wifi.WifiManager
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import com.guichaguri.trackplayer.service.Utils

/**
 * @author Guichaguri
 */
class ButtonEvents(private val service: MusicService, private val manager: MusicManager) :
    MediaSessionCompat.Callback() {
    override fun onPlay() {
        service.emit(MusicEvents.Companion.BUTTON_PLAY, null)
    }

    override fun onPause() {
        service.emit(MusicEvents.Companion.BUTTON_PAUSE, null)
    }

    override fun onStop() {
        service.emit(MusicEvents.Companion.BUTTON_STOP, null)
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle) {
        val bundle = Bundle()
        bundle.putString("id", mediaId)
        service.emit(MusicEvents.Companion.BUTTON_PLAY_FROM_ID, bundle)
    }

    @SuppressLint("InlinedApi")
    override fun onPlayFromSearch(query: String, extras: Bundle) {
        val bundle = Bundle()
        bundle.putString("query", query)
        if (extras.containsKey(MediaStore.EXTRA_MEDIA_FOCUS)) {
            var focus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS)
            if (MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE == focus) {
                focus = "artist"
            } else if (MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE == focus) {
                focus = "album"
            } else if (MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE == focus) {
                focus = "playlist"
            } else if (MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE == focus) {
                focus = "genre"
            } else if (MediaStore.Audio.Media.ENTRY_CONTENT_TYPE == focus) {
                focus = "title"
            }
            bundle.putString("focus", focus)
        }
        if (extras.containsKey(MediaStore.EXTRA_MEDIA_TITLE)) bundle.putString(
            "title",
            extras.getString(MediaStore.EXTRA_MEDIA_TITLE)
        )
        if (extras.containsKey(MediaStore.EXTRA_MEDIA_ARTIST)) bundle.putString(
            "artist",
            extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
        )
        if (extras.containsKey(MediaStore.EXTRA_MEDIA_ALBUM)) bundle.putString(
            "album",
            extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
        )
        if (extras.containsKey(MediaStore.EXTRA_MEDIA_GENRE)) bundle.putString(
            "genre",
            extras.getString(MediaStore.EXTRA_MEDIA_GENRE)
        )
        if (extras.containsKey(MediaStore.EXTRA_MEDIA_PLAYLIST)) bundle.putString(
            "playlist",
            extras.getString(MediaStore.EXTRA_MEDIA_PLAYLIST)
        )
        service.emit(MusicEvents.Companion.BUTTON_PLAY_FROM_SEARCH, bundle)
    }

    override fun onSkipToQueueItem(id: Long) {
        val tracks = manager.playback?.queue
        for (i in tracks!!.indices) {
            if (tracks[i]!!.queueId != id) continue
            val bundle = Bundle()
            bundle.putInt("index", i)
            service.emit(MusicEvents.Companion.BUTTON_SKIP, bundle)
            break
        }
    }

    override fun onSkipToPrevious() {
        service.emit(MusicEvents.Companion.BUTTON_SKIP_PREVIOUS, null)
    }

    override fun onSkipToNext() {
        service.emit(MusicEvents.Companion.BUTTON_SKIP_NEXT, null)
    }

    override fun onRewind() {
        val bundle = Bundle()
        bundle.putInt("interval", manager.metadata.backwardJumpInterval)
        service.emit(MusicEvents.Companion.BUTTON_JUMP_BACKWARD, bundle)
    }

    override fun onFastForward() {
        val bundle = Bundle()
        bundle.putInt("interval", manager.metadata.forwardJumpInterval)
        service.emit(MusicEvents.Companion.BUTTON_JUMP_FORWARD, bundle)
    }

    override fun onSeekTo(pos: Long) {
        val bundle = Bundle()
        bundle.putDouble("position", Utils.toSeconds(pos))
        service.emit(MusicEvents.Companion.BUTTON_SEEK_TO, bundle)
    }

    override fun onSetRating(rating: RatingCompat) {
        val bundle = Bundle()
        Utils.setRating(bundle, "rating", rating)
        service.emit(MusicEvents.Companion.BUTTON_SET_RATING, bundle)
    }
}