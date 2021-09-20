package com.guichaguri.trackplayer.service.player

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.facebook.react.bridge.Promise
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.guichaguri.trackplayer.service.MusicManager
import com.guichaguri.trackplayer.service.Utils
import com.guichaguri.trackplayer.service.models.Track
import java.util.*

/**
 * @author Guichaguri
 */
abstract class ExoPlayback<T : Player?>(
    protected val context: Context,
    protected val manager: MusicManager,
    protected val player: T,
    protected var autoUpdateMetadata: Boolean
) : Player.EventListener, MetadataOutput {
    var queue = Collections.synchronizedList(ArrayList<Track>())

    // https://github.com/google/ExoPlayer/issues/2728
    protected var lastKnownWindow = C.INDEX_UNSET
    protected var lastKnownPosition = C.POSITION_UNSET.toLong()
    protected var previousState = PlaybackStateCompat.STATE_NONE
    var volumeMultiplier = 1.0f
        set(value) {
            playerVolume = volume * value
            field = value
        }
    open fun initialize() {
        player!!.addListener(this)
    }

    abstract fun add(track: Track, index: Int, promise: Promise)
    abstract fun add(tracks: Collection<Track>, index: Int, promise: Promise)
    abstract fun remove(indexes: List<Int>, promise: Promise)
    abstract fun removeUpcomingTracks()
    abstract var repeatMode: Int
    fun updateTrack(index: Int, track: Track) {
        val currentIndex = player!!.currentWindowIndex
        queue[index] = track
        if (currentIndex == index) manager.metadata.updateMetadata(this, track)
    }

    val currentTrackIndex: Int?
        get() {
            val index = player!!.currentWindowIndex
            return if (index < 0 || index >= queue.size) null else index
        }
    val currentTrack: Track?
        get() {
            val index = player!!.currentWindowIndex
            return if (index < 0 || index >= queue.size) null else queue[index]
        }

    fun skip(index: Int, promise: Promise) {
        if (index < 0 || index >= queue.size) {
            promise.reject("index_out_of_bounds", "The index is out of bounds")
            return
        }
        lastKnownWindow = player!!.currentWindowIndex
        lastKnownPosition = player.currentPosition
        player.seekToDefaultPosition(index)
        promise.resolve(null)
    }

    fun skipToPrevious(promise: Promise) {
        val prev = player!!.previousWindowIndex
        if (prev == C.INDEX_UNSET) {
            promise.reject("no_previous_track", "There is no previous track")
            return
        }
        lastKnownWindow = player.currentWindowIndex
        lastKnownPosition = player.currentPosition
        player.seekToDefaultPosition(prev)
        promise.resolve(null)
    }

    fun skipToNext(promise: Promise) {
        val next = player!!.nextWindowIndex
        if (next == C.INDEX_UNSET) {
            promise.reject("queue_exhausted", "There is no tracks left to play")
            return
        }
        lastKnownWindow = player.currentWindowIndex
        lastKnownPosition = player.currentPosition
        player.seekToDefaultPosition(next)
        promise.resolve(null)
    }

    open fun play() {
        player!!.playWhenReady = true
    }

    fun pause() {
        player!!.playWhenReady = false
    }

    open fun stop() {
        lastKnownWindow = player!!.currentWindowIndex
        lastKnownPosition = player.currentPosition
        player.stop(false)
        player.playWhenReady = false
        player.seekTo(lastKnownWindow, 0)
    }

    open fun reset() {
        lastKnownWindow = player!!.currentWindowIndex
        lastKnownPosition = player.currentPosition
        player.stop(true)
        player.playWhenReady = false
    }

    val isRemote: Boolean
        get() = false

    fun shouldAutoUpdateMetadata(): Boolean {
        return autoUpdateMetadata
    }

    val position: Long
        get() = player!!.currentPosition
    val bufferedPosition: Long
        get() = player!!.bufferedPosition
    val duration: Long
        get() {
            val current = currentTrack
            if (current != null && current.duration > 0) {
                return current.duration
            }
            val duration = player!!.duration
            return if (duration == C.TIME_UNSET) 0 else duration
        }

    open fun seekTo(time: Long) {
        lastKnownWindow = player!!.currentWindowIndex
        lastKnownPosition = player.currentPosition
        player.seekTo(time)
    }

    var volume: Float
        get() = playerVolume / volumeMultiplier
        set(volume) {
            playerVolume = volume * volumeMultiplier
        }

//    fun setVolumeMultiplier(multiplier: Float) {
//        playerVolume = volume * multiplier
//        volumeMultiplier = multiplier
//    }

    abstract var playerVolume: Float
    var rate: Float
        get() = player!!.playbackParameters.speed
        set(rate) {
            player!!.setPlaybackParameters(
                PlaybackParameters(
                    rate,
                    player.playbackParameters.pitch
                )
            )
        }
    val state: Int
        get() {
            when (player!!.playbackState) {
                Player.STATE_BUFFERING -> return if (player.playWhenReady) PlaybackStateCompat.STATE_BUFFERING else PlaybackStateCompat.STATE_CONNECTING
                Player.STATE_ENDED -> return PlaybackStateCompat.STATE_STOPPED
                Player.STATE_IDLE -> return PlaybackStateCompat.STATE_NONE
                Player.STATE_READY -> return if (player.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            }
            return PlaybackStateCompat.STATE_NONE
        }

    open fun destroy() {
        player!!.release()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        Log.d(Utils.LOG, "onTimelineChanged: $reason")
        if ((reason == Player.TIMELINE_CHANGE_REASON_PREPARED || reason == Player.TIMELINE_CHANGE_REASON_DYNAMIC) && !timeline.isEmpty) {
            onPositionDiscontinuity(Player.DISCONTINUITY_REASON_INTERNAL)
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        Log.d(Utils.LOG, "onPositionDiscontinuity: $reason")
        if (lastKnownWindow != player!!.currentWindowIndex) {
            val prevIndex = if (lastKnownWindow == C.INDEX_UNSET) null else lastKnownWindow
            val nextIndex = currentTrackIndex
            val next = if (nextIndex == null) null else queue[nextIndex]

            // Track changed because it ended
            // We'll use its duration instead of the last known position
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION && lastKnownWindow != C.INDEX_UNSET) {
                if (lastKnownWindow >= player.currentTimeline.windowCount) return
                val duration =
                    player.currentTimeline.getWindow(lastKnownWindow, Timeline.Window()).durationMs
                if (duration != C.TIME_UNSET) lastKnownPosition = duration
            }
            manager.onTrackUpdate(prevIndex, lastKnownPosition, nextIndex, next)
        } else if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION && lastKnownWindow == player.currentWindowIndex) {
            val nextIndex = currentTrackIndex
            val next = if (nextIndex == null) null else queue[nextIndex]
            val duration =
                player.currentTimeline.getWindow(lastKnownWindow, Timeline.Window()).durationMs
            if (duration != C.TIME_UNSET) lastKnownPosition = duration
            manager.onTrackUpdate(nextIndex, lastKnownPosition, nextIndex, next)
        }
        lastKnownWindow = player.currentWindowIndex
        lastKnownPosition = player.currentPosition
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        for (i in 0 until trackGroups.length) {
            // Loop through all track groups.
            // As for the current implementation, there should be only one
            val group = trackGroups[i]
            for (f in 0 until group.length) {
                // Loop through all formats inside the track group
                val format = group.getFormat(f)

                // Parse the metadata if it is present
                if (format.metadata != null) {
                    onMetadata(format.metadata!!)
                }
            }
        }
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        // Buffering updates
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val state = state
        if (state != previousState) {
            if (Utils.isPlaying(state) && !Utils.isPlaying(previousState)) {
                manager.onPlay()
            } else if (Utils.isPaused(state) && !Utils.isPaused(previousState)) {
                manager.onPause()
            } else if (Utils.isStopped(state) && !Utils.isStopped(previousState)) {
                manager.onStop()
            }
            manager.onStateChange(state)
            previousState = state
            if (state == PlaybackStateCompat.STATE_STOPPED) {
                val previous = currentTrackIndex
                val position = position
                manager.onTrackUpdate(previous, position, null, null)
                manager.onEnd(currentTrackIndex, position)
            }
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        val code: String
        code = if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            "playback-source"
        } else if (error.type == ExoPlaybackException.TYPE_RENDERER) {
            "playback-renderer"
        } else {
            "playback" // Other unexpected errors related to the playback
        }
        manager.onError(code, error.cause!!.message)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        // Speed or pitch changes
    }

    override fun onSeekProcessed() {
        // Finished seeking
    }

    override fun onMetadata(metadata: Metadata) {
        SourceMetadata.handleMetadata(manager, metadata)
    }

    init {
        val component = player!!.metadataComponent
        component?.addMetadataOutput(this)
    }
}