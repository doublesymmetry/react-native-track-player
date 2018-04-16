package guichaguri.trackplayer.player

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import com.facebook.react.bridge.Promise
import guichaguri.trackplayer.extensions.*
import guichaguri.trackplayer.logic.MediaManager
import guichaguri.trackplayer.logic.Utils
import guichaguri.trackplayer.logic.track.Track


/**
 * Base player object
 *
 * @author David Chavez
 */
abstract class Playback(protected val context: Context, protected val manager: MediaManager) {

    /**
     * Attributes
     */
    var queue = mutableListOf<Track>()
        private set

    var currentIndex = -1
        set(value) {
            field = value
            updateCurrentTrack(value)
        }

    val currentTrack get() = queue.getOrNull(currentIndex)

    /**
     * State from [android.support.v4.media.session.PlaybackStateCompat]
     */
    abstract val state: Int
    private var prevState = PlaybackStateCompat.STATE_NONE

    abstract var rate: Float
    abstract var volume: Float
    abstract val position: Long
    abstract val duration: Long
    abstract val isRemote: Boolean
    abstract val bufferedPosition: Long

    /**
     * Public API
     */
    fun add(tracks: List<Track>, insertBeforeId: String?, callback: Promise) {
        queue.indexOfFirstOrNull { track -> track.id == insertBeforeId }?.let {
            queue.addAll(it, tracks)
        } ?: queue.addAll(tracks)

        Utils.resolveCallback(callback)
    }

    fun remove(ids: List<String>, callback: Promise) {
        var actionAfterRemovals = "none"
        for (id in ids) {
            queue.indexOfFirstOrNull { track -> track.id == id }?.let {
                when {
                    it < currentIndex -> currentIndex -= 1
                    queue.last().id == id -> actionAfterRemovals = "stop"
                    it == currentIndex -> actionAfterRemovals = "play"
                }

                queue.removeAt(it);
            }
        }

        when (actionAfterRemovals) {
            "play" -> updateCurrentTrack(currentIndex)
            "stop" -> stop()
        }

        Utils.resolveCallback(callback)
        manager.onQueueUpdate()
    }

    fun removeUpcomingTracks() {
        queue = queue.filterIndexed { index, _ -> index <= currentIndex  } as MutableList
        manager.onQueueUpdate()
    }

    fun skip(id: String, callback: Promise) {
        queue.indexOfFirstOrNull { track -> track.id == id }?.let {
            currentIndex = it
            play()
        } ?: Utils.rejectCallback(callback, "track_not_in_queue", "Given track ID was not found in queue")
    }

    fun skipToNext(): Boolean = when (queue.indices.contains(currentIndex + 1)) {
        true -> {
            currentIndex += 1
            play()
            true
        }
        false -> {
            stop()
            false
        }
    }

    fun skipToPrevious(): Boolean = when (queue.indices.contains(currentIndex - 1)) {
        true -> {
            currentIndex -= 1
            play()
            true
        }
        false -> {
            stop()
            false
        }
    }

    abstract fun load(track: Track)

    open fun reset() {
        val prev = currentTrack
        val pos = position

        queue.clear()
        manager.onQueueUpdate()

        currentIndex = -1
        manager.onTrackUpdate(prev, pos, null, true)
    }

    abstract fun play()

    abstract fun pause()

    open fun stop() {
        currentIndex = -1
        updateCurrentTrack(0)
    }

    abstract fun seekTo(ms: Long)

    fun copyPlayback(playback: Playback) {
        queue = playback.queue
        currentIndex = playback.currentIndex

        val track = currentTrack ?: return

        load(track)
        seekTo(playback.position)

        when {
            Utils.isPlaying(playback.state) -> play()
            Utils.isPaused(playback.state) -> pause()
        }
    }

    abstract fun destroy()

    protected fun updateState(state: Int) {
        manager.onPlaybackUpdate()
        if (state == prevState) return

        when {
            Utils.isPlaying(state) && !Utils.isPlaying(prevState) -> manager.onPlay()
            Utils.isPaused(state) && !Utils.isPaused(prevState) -> manager.onPause()
            Utils.isStopped(state) && !Utils.isStopped(prevState) -> manager.onStop()
        }

        manager.onStateChange(state)
        prevState = state
    }

    private fun updateCurrentTrack(index: Int) {
        if (!queue.indices.contains(index)) return

        val previousTrack = currentTrack
        val oldProgression = position

        val track = queue[index]
        load(track)

        manager.onTrackUpdate(previousTrack, oldProgression, track, true)
    }
}
