package com.guichaguri.trackplayer.service.player

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
import android.util.Log
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.facebook.react.ReactInstanceManager
import com.google.android.exoplayer2.upstream.*
import com.guichaguri.trackplayer.service.Utils
import com.guichaguri.trackplayer.service.models.Track
import java.io.File
import java.lang.Exception
import java.util.*

/**
 * @author Guichaguri
 */
class LocalPlayback(
    context: Context,
    manager: MusicManager,
    player: SimpleExoPlayer,
    private val cacheMaxSize: Long,
    autoUpdateMetadata: Boolean
) : ExoPlayback<SimpleExoPlayer?>(context, manager, player, autoUpdateMetadata) {
    private var cache: SimpleCache? = null
    private var source: ConcatenatingMediaSource? = null
    private var prepared = false
    override fun initialize() {
        cache = if (cacheMaxSize > 0) {
            val cacheDir = File(context.cacheDir, "TrackPlayer")
            val db: DatabaseProvider = ExoDatabaseProvider(context)
            SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(cacheMaxSize), db)
        } else {
            null
        }
        super.initialize()
        resetQueue()
    }

    fun enableCaching(ds: DataSource.Factory): DataSource.Factory {
        return if (cache == null || cacheMaxSize <= 0) ds else CacheDataSourceFactory(
            cache,
            ds,
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
    }

    private fun prepare() {
        if (!prepared) {
            Log.d(Utils.LOG, "Preparing the media source...")
            player!!.prepare(source!!, false, false)
            prepared = true
        }
    }

    override fun add(track: Track, index: Int, promise: Promise) {
        queue.add(index, track)
        val trackSource = track.toMediaSource(context, this)
        source!!.addMediaSource(index, trackSource, manager.handler) { promise.resolve(index) }
        prepare()
    }

    override fun add(tracks: Collection<Track>, index: Int, promise: Promise) {
        val trackList: MutableList<MediaSource?> = ArrayList()
        for (track in tracks) {
            trackList.add(track.toMediaSource(context, this))
        }
        queue.addAll(index, tracks)
        source!!.addMediaSources(index, trackList, manager.handler) { promise.resolve(index) }
        prepare()
    }

    override fun remove(indexes: List<Int>, promise: Promise) {
        val currentIndex = player!!.currentWindowIndex

        // Sort the list so we can loop through sequentially
        Collections.sort(indexes)
        for (i in indexes.indices.reversed()) {
            val index = indexes[i]

            // Skip indexes that are the current track or are out of bounds
            if (index == currentIndex || index < 0 || index >= queue.size) {
                // Resolve the promise when the last index is invalid
                if (i == 0) promise.resolve(null)
                continue
            }
            queue.removeAt(index)
            if (i == 0) {
                source!!.removeMediaSource(index, manager.handler, Utils.toRunnable(promise))
            } else {
                source!!.removeMediaSource(index)
            }

            // Fix the window index
            if (index < lastKnownWindow) {
                lastKnownWindow--
            }
        }
    }

    override fun removeUpcomingTracks() {
        val currentIndex = player!!.currentWindowIndex
        if (currentIndex == C.INDEX_UNSET) return
        for (i in queue.size - 1 downTo currentIndex + 1) {
            queue.removeAt(i)
            source!!.removeMediaSource(i)
        }
    }

    override var repeatMode: Int
        get() = player!!.repeatMode
        set(repeatMode) {
            player!!.repeatMode = repeatMode
        }

    private fun resetQueue() {
        queue.clear()
        source = ConcatenatingMediaSource()
        player!!.prepare(source!!, true, true)
        prepared = false // We set it to false as the queue is now empty
        lastKnownWindow = C.INDEX_UNSET
        lastKnownPosition = C.POSITION_UNSET.toLong()
        manager.onReset()
    }

    override fun play() {
        prepare()
        super.play()
    }

    override fun stop() {
        super.stop()
        prepared = false
    }

    override fun seekTo(time: Long) {
        prepare()
        super.seekTo(time)
    }

    override fun reset() {
        val track = currentTrackIndex
        val position = player!!.currentPosition
        super.reset()
        resetQueue()
        manager.onTrackUpdate(track, position, null, null)
    }

    override var playerVolume: Float
        get() = player!!.volume
        set(volume) {
            player!!.volume = volume
        }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
            prepared = false
        }
        super.onPlayerStateChanged(playWhenReady, playbackState)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        prepared = false
        super.onPlayerError(error)
    }

    override fun destroy() {
        super.destroy()
        if (cache != null) {
            try {
                cache!!.release()
                cache = null
            } catch (ex: Exception) {
                Log.w(Utils.LOG, "Couldn't release the cache properly", ex)
            }
        }
    }
}