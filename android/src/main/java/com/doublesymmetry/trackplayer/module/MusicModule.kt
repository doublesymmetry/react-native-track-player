package com.doublesymmetry.trackplayer.module

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.net.Uri
import android.support.v4.media.RatingCompat
import androidx.media3.common.MediaItem
import androidx.media.utils.MediaConstants
import androidx.media3.common.MediaMetadata
import com.doublesymmetry.kotlinaudio.models.Capability
import com.doublesymmetry.kotlinaudio.models.RepeatMode
import com.doublesymmetry.trackplayer.model.State
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.service.MusicService
import com.doublesymmetry.trackplayer.utils.AppForegroundTracker
import com.doublesymmetry.trackplayer.utils.RejectionException
import com.facebook.react.bridge.*
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.annotation.Nonnull

import com.doublesymmetry.trackplayer.NativeTrackPlayerSpec


/**
 * @author Milen Pivchev @mpivchev
 */
@ReactModule(name = MusicModule.NAME)
class MusicModule(reactContext: ReactApplicationContext) : NativeTrackPlayerSpec(reactContext),
    ServiceConnection {
    private lateinit var browser: MediaBrowser
    private var playerOptions: Bundle? = null
    private var isServiceBound = false
    private var playerSetUpPromise: Promise? = null
    private val scope = MainScope()
    private lateinit var musicService: MusicService
    private val context = reactContext

    @Nonnull
    override fun getName(): String {
      return NAME
    }

    companion object {
      const val NAME = "TrackPlayer"
    }

    override fun addListener(eventType: String) {
        // No implementation needed for TurboModule
        // This implements the abstract method required by NativeTrackPlayerSpec
    }

    override fun removeListeners(count: Double) {
        // No implementation needed for TurboModule
        // This implements the abstract method required by NativeTrackPlayerSpec
    }

    override fun initialize() {
        AppForegroundTracker.start()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        launchInScope {
            // If a binder already exists, don't get a new one
            if (!::musicService.isInitialized) {
                val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
                musicService = binder.service
                musicService.setupPlayer(playerOptions)
                playerSetUpPromise?.resolve(null)
            }

            isServiceBound = true
        }
    }

    /**
     * Called when a connection to the Service has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName) {
        launchInScope {
            isServiceBound = false
        }
    }

    /**
     * Checks wither service is bound, or rejects. Returns whether promise was rejected.
     */
    private fun verifyServiceBoundOrReject(promise: Promise): Boolean {
        if (!isServiceBound) {
            promise.reject(
                "player_not_initialized",
                "The player is not initialized. Call setupPlayer first."
            )
            return true
        }

        return false
    }

    private fun bundleToTrack(bundle: Bundle): Track {
        return Track(context, bundle, musicService.ratingType)
    }

    private fun rejectWithException(callback: Promise, exception: Exception) {
        when (exception) {
            is RejectionException -> {
                callback.reject(exception.code, exception)
            }
            else -> {
                callback.reject("runtime_exception", exception)
            }
        }
    }

    private fun readableArrayToTrackList(data: ReadableArray?): MutableList<Track> {
        val bundleList = Arguments.toList(data)
        if (bundleList !is ArrayList) {
            throw RejectionException("invalid_parameter", "Was not given an array of tracks")
        }
        return bundleList.map {
            if (it is Bundle) {
                bundleToTrack(it)
            } else {
                throw RejectionException(
                    "invalid_track_object",
                    "Track was not a dictionary type"
                )
            }
        }.toMutableList()
    }

    /* ****************************** API ****************************** */
    override fun getTypedExportedConstants(): Map<String, Any> {
        return HashMap<String, Any>().apply {
            // Capabilities
            this["CAPABILITY_PLAY"] = Capability.PLAY.ordinal
            this["CAPABILITY_PLAY_FROM_ID"] = Capability.PLAY_FROM_ID.ordinal
            this["CAPABILITY_PLAY_FROM_SEARCH"] = Capability.PLAY_FROM_SEARCH.ordinal
            this["CAPABILITY_PAUSE"] = Capability.PAUSE.ordinal
            this["CAPABILITY_STOP"] = Capability.STOP.ordinal
            this["CAPABILITY_SEEK_TO"] = Capability.SEEK_TO.ordinal
            this["CAPABILITY_SKIP"] = OnErrorAction.SKIP.ordinal
            this["CAPABILITY_SKIP_TO_NEXT"] = Capability.SKIP_TO_NEXT.ordinal
            this["CAPABILITY_SKIP_TO_PREVIOUS"] = Capability.SKIP_TO_PREVIOUS.ordinal
            this["CAPABILITY_SET_RATING"] = Capability.SET_RATING.ordinal
            this["CAPABILITY_JUMP_FORWARD"] = Capability.JUMP_FORWARD.ordinal
            this["CAPABILITY_JUMP_BACKWARD"] = Capability.JUMP_BACKWARD.ordinal

            // States
            this["STATE_NONE"] = State.None.state
            this["STATE_READY"] = State.Ready.state
            this["STATE_PLAYING"] = State.Playing.state
            this["STATE_PAUSED"] = State.Paused.state
            this["STATE_STOPPED"] = State.Stopped.state
            this["STATE_BUFFERING"] = State.Buffering.state
            this["STATE_LOADING"] = State.Loading.state

            // Rating Types
            this["RATING_HEART"] = RatingCompat.RATING_HEART
            this["RATING_THUMBS_UP_DOWN"] = RatingCompat.RATING_THUMB_UP_DOWN
            this["RATING_3_STARS"] = RatingCompat.RATING_3_STARS
            this["RATING_4_STARS"] = RatingCompat.RATING_4_STARS
            this["RATING_5_STARS"] = RatingCompat.RATING_5_STARS
            this["RATING_PERCENTAGE"] = RatingCompat.RATING_PERCENTAGE

            // Repeat Modes
            this["REPEAT_OFF"] = Player.REPEAT_MODE_OFF
            this["REPEAT_TRACK"] = Player.REPEAT_MODE_ONE
            this["REPEAT_QUEUE"] = Player.REPEAT_MODE_ALL

            // Pitch Algorithm: No-op on android
            this["PITCH_ALGORITHM_LINEAR"] = -1
            this["PITCH_ALGORITHM_MUSIC"] = -2
            this["PITCH_ALGORITHM_VOICE"] = -3
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun setupPlayer(data: ReadableMap?, promise: Promise) {
        if (isServiceBound) {
            promise.reject(
                "player_already_initialized",
                "The player has already been initialized via setupPlayer."
            )
            return
        }

        val bundledData = Arguments.toBundle(data)

        playerSetUpPromise = promise
        playerOptions = bundledData

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                MusicEvents(context),
                IntentFilter(EVENT_INTENT), Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(
                MusicEvents(context),
                IntentFilter(EVENT_INTENT)
            )
        }

        val musicModule = this
        try {
            Intent(context, MusicService::class.java).also { intent ->
                context.bindService(intent, musicModule, Context.BIND_AUTO_CREATE)
                val sessionToken =
                    SessionToken(context, ComponentName(context, MusicService::class.java))
                val browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()
                // browser = browserFuture.get()
            }
        } catch (exception: Exception) {
            Timber.w(exception, "Could not initialize service")
            throw exception
        }
    }

    override fun updateOptions(data: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        val options = Arguments.toBundle(data)

        options?.let {
            musicService.updateOptions(it)
        }

        callback.resolve(null)
    }

    // override fun add(data: Double, y: Double): Double {
    //   return 1.0
    // }
    override fun add(data: ReadableArray, insertBeforeIndex: Double?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        val insertBeforeIndexInt = insertBeforeIndex?.toInt() ?: 0;
        try {
            val tracks = readableArrayToTrackList(data);
            if (insertBeforeIndexInt < -1 || insertBeforeIndexInt > musicService.tracks.size) {
                callback.reject("index_out_of_bounds", "The track index is out of bounds")
                return@launchInScope
            }
            val index = if (insertBeforeIndexInt == -1) musicService.tracks.size else insertBeforeIndexInt
            musicService.add(
                tracks,
                index
            )
            callback.resolve(index)
        } catch (exception: Exception) {
            rejectWithException(callback, exception)
        }
    }

    override fun load(data: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        if (data == null) {
            callback.resolve(null)
            return@launchInScope
        }
        val bundle = Arguments.toBundle(data);
        if (bundle is Bundle) {
            musicService.load(bundleToTrack(bundle))
            callback.resolve(null)
        } else {
            callback.reject("invalid_track_object", "Track was not a dictionary type")
        }
    }

    override fun move(fromIndex: Double, toIndex: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.move(fromIndex.toInt(), toIndex.toInt())
        callback.resolve(null)
    }

    override fun remove(data: ReadableArray?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        val inputIndexes = Arguments.toList(data)
        if (inputIndexes != null) {
            val size = musicService.tracks.size
            val indexes: ArrayList<Int> = ArrayList();
            for (inputIndex in inputIndexes) {
                val index = if (inputIndex is Int) inputIndex else inputIndex.toString().toInt()
                if (index < 0 || index >= size) {
                    callback.reject(
                        "index_out_of_bounds",
                        "One or more indexes was out of bounds"
                    )
                    return@launchInScope
                }
                indexes.add(index)
            }
            musicService.remove(indexes)
        }
        callback.resolve(null)
    }

    override fun updateMetadataForTrack(index: Double, map: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        if (index < 0 || index >= musicService.tracks.size) {
            callback.reject("index_out_of_bounds", "The index is out of bounds")
            return@launchInScope
        }

        Arguments.toBundle(map)?.let {
            musicService.updateMetadataForTrack(index.toInt(), it)
        }

        callback.resolve(null)
    }

    override fun updateNowPlayingMetadata(map: ReadableMap?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        if (musicService.tracks.isEmpty()) {
            callback.reject("no_current_item", "There is no current item in the player")
            return@launchInScope
        }

        Arguments.toBundle(map)?.let {
            musicService.updateNowPlayingMetadata(it)
        }

        callback.resolve(null)
    }

    override fun removeUpcomingTracks(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.removeUpcomingTracks()
        callback.resolve(null)
    }

    override fun skip(index: Double, initialTime: Double?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.skip(index.toInt())

        if (initialTime != null && initialTime >= 0) {
            musicService.seekTo(initialTime.toFloat())
        }

        callback.resolve(null)
    }

    override fun skipToNext(initialTime: Double?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.skipToNext()

        if (initialTime != null && initialTime >= 0) {
            musicService.seekTo(initialTime.toFloat())
        }

        callback.resolve(null)
    }

    override fun skipToPrevious(initialTime: Double?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.skipToPrevious()

        if (initialTime != null && initialTime >= 0) {
            musicService.seekTo(initialTime.toFloat())
        }

        callback.resolve(null)
    }

    override fun reset(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.stop()
        delay(300) // Allow playback to stop
        musicService.clear()

        callback.resolve(null)
    }

    override fun play(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.play()
        callback.resolve(null)
    }

    override fun pause(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.pause()
        callback.resolve(null)
    }

    override fun stop(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.stop()
        callback.resolve(null)
    }

    override fun seekTo(seconds: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.seekTo(seconds.toFloat())
        callback.resolve(null)
    }

    override fun seekBy(offset: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.seekBy(offset.toFloat())
        callback.resolve(null)
    }

    override fun retry(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.retry()
        callback.resolve(null)
    }

    override fun setVolume(volume: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.setVolume(volume.toFloat())
        callback.resolve(null)
    }

    override fun getVolume(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getVolume())
    }

    override fun setRate(rate: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.setRate(rate.toFloat())
        callback.resolve(null)
    }

    override fun getRate(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getRate())
    }

    override fun setRepeatMode(mode: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.setRepeatMode(RepeatMode.fromOrdinal(mode.toInt()))
        callback.resolve(null)
    }

    override fun getRepeatMode(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.getRepeatMode().ordinal)
    }

    override fun setPlayWhenReady(playWhenReady: Boolean, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        musicService.playWhenReady = playWhenReady
        callback.resolve(null)
    }

    override fun getPlayWhenReady(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(musicService.playWhenReady)
    }

    override fun getTrack(index: Double, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        val indexInt = index.toInt()
        if (indexInt >= 0 && indexInt < musicService.tracks.size) {
            callback.resolve(Arguments.fromBundle(musicService.tracks[indexInt].originalItem))
        } else {
            callback.resolve(null)
        }
    }

    override fun getQueue(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        callback.resolve(Arguments.fromList(musicService.tracks.map { it.originalItem }))
    }

    override fun setQueue(data: ReadableArray?, callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope

        try {
            musicService.clear()
            musicService.add(readableArrayToTrackList(data))
            callback.resolve(null)
        } catch (exception: Exception) {
            rejectWithException(callback, exception)
        }
    }

    override fun getActiveTrackIndex(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(
            if (musicService.tracks.isEmpty()) null else musicService.getCurrentTrackIndex()
        )
    }

    override fun getActiveTrack(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(
            musicService.currentTrack?.let {
                Arguments.fromBundle(it.originalItem)
            }
        )
    }

    override fun getProgress(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        val bundle = Bundle()
        bundle.putDouble("duration", musicService.getDurationInSeconds());
        bundle.putDouble("position", musicService.getPositionInSeconds());
        bundle.putDouble("buffered", musicService.getBufferedPositionInSeconds());
        callback.resolve(Arguments.fromBundle(bundle))
    }

    override fun getPlaybackState(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(Arguments.fromBundle(musicService.getPlayerStateBundle(musicService.state)))
    }

    override fun acquireWakeLock(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.acquireWakeLock()
        callback.resolve(null)
    }

    override fun abandonWakeLock(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        musicService.abandonWakeLock()
        callback.resolve(null)
    }

    override fun validateOnStartCommandIntent(callback: Promise) = launchInScope {
        if (verifyServiceBoundOrReject(callback)) return@launchInScope
        callback.resolve(musicService.onStartCommandIntentValid)
    }

    // Bridgeless interop layer tries to pass the `Job` from `scope.launch` to the JS side
    // which causes an exception. We can work around this using a wrapper.
    private fun launchInScope(block: suspend () -> Unit) {
        scope.launch {
            block()
        }
    }
}
