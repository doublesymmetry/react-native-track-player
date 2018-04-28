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

    /**
     * Attributes
     */
    var queue: MutableList<Track> = Collections.synchronizedList(ArrayList())
        protected set

    var currentIndex = -1
        set(value) {
            field = value
            updateCurrentTrack(value)
        }

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
            queue.addAll(tracks)
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
            "play" -> updateCurrentTrack(currentIndex)
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

        updateCurrentTrack(trackIndex)
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
        updateCurrentTrack(0)
    }

    abstract fun seekTo(ms: Long)

    fun copyPlayback(playback: Playback) {
        queue = playback.queue
        currentIndex = playback.currentIndex

        val track = getCurrentTrack() ?: return

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
        if (queue.indices.contains(index)) {
            val previousTrack = getCurrentTrack()
            val oldProgression = position

            val track = queue[index]
            load(track)

            manager.onTrackUpdate(previousTrack, oldProgression, track, true)
        }
    }
}
