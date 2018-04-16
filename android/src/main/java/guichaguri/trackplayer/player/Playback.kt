package guichaguri.trackplayer.player

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.facebook.react.bridge.Promise
import guichaguri.trackplayer.logic.MediaManager
import guichaguri.trackplayer.logic.Utils
import guichaguri.trackplayer.logic.track.Track
import java.util.ArrayList
import java.util.Collections

/**
 * Base player object
 *
 * @author David Chavez
 */
abstract class Playback(protected val context: Context, protected val manager: MediaManager) {
    var queue: MutableList<Track> = Collections.synchronizedList(ArrayList())
        protected set

    protected var currentIndex = -1
    private var prevState = PlaybackStateCompat.STATE_NONE

    /**
     * State from [android.support.v4.media.session.PlaybackStateCompat]
     */
    abstract val state: Int

    abstract val position: Long

    abstract val bufferedPosition: Long

    abstract val duration: Long

    abstract var rate: Float

    abstract var volume: Float

    abstract val isRemote: Boolean

    fun getCurrentTrack(): Track? {
        return if (currentIndex < queue.size && currentIndex >= 0) queue[currentIndex] else null
    }

    fun add(tracks: List<Track>, insertBeforeId: String?, callback: Promise) {
        val trackIndex = queue.indexOfFirst { track -> track.id == insertBeforeId }
        if (trackIndex != -1) {
            queue.addAll(trackIndex, tracks)
            if (currentIndex >= trackIndex) { currentIndex += tracks.size }
        } else {
            val wasEmpty = queue.isEmpty()
            queue.addAll(tracks)
            if (wasEmpty) { updateCurrentTrack(0, null) }
        }

        Utils.resolveCallback(callback)
    }

    fun remove(ids: List<String>, callback: Promise) {
        var actionAfterRemovals = "none"
        for (id in ids) {
            val trackIndex = queue.indexOfFirst { track -> track.id == id }
            if (trackIndex == -1) { return }

            when {
                trackIndex < currentIndex   -> currentIndex -= 1
                queue.last().id == id       -> actionAfterRemovals = "stop"
                trackIndex == currentIndex  -> actionAfterRemovals = "play"
            }

            queue.removeAt(trackIndex);
        }

        when (actionAfterRemovals) {
            "play" -> updateCurrentTrack(currentIndex, null)
            "stop" -> stop()
        }

        Utils.resolveCallback(callback)
        manager.onQueueUpdate()
    }

    fun clearQueue() {
        queue.clear()
        manager.onQueueUpdate()
        currentIndex = -1
    }

    fun skip(id: String, callback: Promise) {
        val trackIndex = queue.indexOfFirst { track -> track.id == id }
        if (trackIndex == -1) {
            Utils.rejectCallback(callback, "track_not_in_queue", "Given track ID was not found in queue")
            return
        }

        updateCurrentTrack(trackIndex, callback)
    }

    fun hasNext(): Boolean {
        return queue.indices.contains(currentIndex + 1)
    }

    fun skipToNext(callback: Promise) {
        if (queue.indices.contains(currentIndex + 1)) {
            updateCurrentTrack(currentIndex + 1, callback)
        } else {
            stop()
            Utils.rejectCallback(callback, "queue_exhausted", "There is no tracks left to play")
        }
    }

    fun skipToPrevious(callback: Promise) {
        if (queue.indices.contains(currentIndex - 1)) {
            updateCurrentTrack(currentIndex - 1, callback)
        } else {
            stop()
            Utils.rejectCallback(callback, "no_previous_track", "There is no previous track")
        }
    }

    abstract fun load(track: Track, callback: Promise?)

    open fun reset() {
        val prev = getCurrentTrack()
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
        updateCurrentTrack(0, null)
    }

    abstract fun seekTo(ms: Long)

    fun copyPlayback(playback: Playback) {
        queue = playback.queue
        currentIndex = playback.currentIndex

        val track = getCurrentTrack() ?: return

        load(track, null)
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

    protected fun updateCurrentTrack(track: Int, callback: Promise?) {
        var updatedTrackIndex = track
        when {
            queue.isEmpty() -> {
                reset()
                Utils.rejectCallback(callback, "queue", "The queue is empty")
            }
            updatedTrackIndex >= queue.size -> { updatedTrackIndex = queue.size - 1 }
            updatedTrackIndex < 0 -> updatedTrackIndex = 0
        }

        val previous = getCurrentTrack()
        val position = position
        val oldState = state

        Log.d(Utils.TAG, "Updating current track...")

        val next = queue[updatedTrackIndex]
        currentIndex = updatedTrackIndex

        load(next, callback)

        when {
            Utils.isPlaying(oldState) -> play()
            Utils.isPaused(oldState) -> pause()
        }

        manager.onTrackUpdate(previous, position, next, true)
    }
}
