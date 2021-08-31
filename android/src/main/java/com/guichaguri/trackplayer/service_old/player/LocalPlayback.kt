package com.guichaguri.trackplayer.service_old.player

import android.content.Context
import android.util.Log
import com.facebook.react.bridge.Promise
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.guichaguri.trackplayer.service_old.MusicManager
import com.guichaguri.trackplayer.service_old.Utils
import com.guichaguri.trackplayer.service.models.Track
import java.io.File
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
            cache!!,
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
        source!!.addMediaSource(index, trackSource, manager.handler!!) { promise.resolve(index) }
        prepare()
    }

    override fun add(tracks: Collection<Track>, index: Int, promise: Promise) {
        val trackList: MutableList<MediaSource?> = ArrayList()
        for (track in tracks) {
            trackList.add(track.toMediaSource(context, this))
        }
        queue.addAll(index, tracks)
//        source!!.addMediaSources(index, trackList, manager.handler!!) { promise.resolve(index) }
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
                source!!.removeMediaSource(index, manager.handler!!, Utils.toRunnable(promise))
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

    override fun onPlayerError(error: PlaybackException) {
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