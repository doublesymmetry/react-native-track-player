package com.guichaguri.trackplayer.module

import android.os.Bundle
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.guichaguri.trackplayer.service.MusicBinder
import com.guichaguri.trackplayer.module.MusicEvents
import android.os.IBinder
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
import android.os.PowerManager
import android.net.wifi.WifiManager
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.*

/**
 * @author Guichaguri
 */
class MusicEvents(private val reactContext: ReactContext) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = intent.getStringExtra("event")
        val data = intent.getBundleExtra("data")
        val map = if (data != null) Arguments.fromBundle(data) else null
        reactContext.getJSModule(RCTDeviceEventEmitter::class.java).emit(
            event!!, map
        )
    }

    companion object {
        // Media Control Events
        const val BUTTON_PLAY = "remote-play"
        const val BUTTON_PLAY_FROM_ID = "remote-play-id"
        const val BUTTON_PLAY_FROM_SEARCH = "remote-play-search"
        const val BUTTON_PAUSE = "remote-pause"
        const val BUTTON_STOP = "remote-stop"
        const val BUTTON_SKIP = "remote-skip"
        const val BUTTON_SKIP_NEXT = "remote-next"
        const val BUTTON_SKIP_PREVIOUS = "remote-previous"
        const val BUTTON_SEEK_TO = "remote-seek"
        const val BUTTON_SET_RATING = "remote-set-rating"
        const val BUTTON_JUMP_FORWARD = "remote-jump-forward"
        const val BUTTON_JUMP_BACKWARD = "remote-jump-backward"
        const val BUTTON_DUCK = "remote-duck"

        // Playback Events
        const val PLAYBACK_STATE = "playback-state"
        const val PLAYBACK_TRACK_CHANGED = "playback-track-changed"
        const val PLAYBACK_QUEUE_ENDED = "playback-queue-ended"
        const val PLAYBACK_METADATA = "playback-metadata-received"
        const val PLAYBACK_ERROR = "playback-error"
    }
}