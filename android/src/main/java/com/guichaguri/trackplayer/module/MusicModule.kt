package com.guichaguri.trackplayer.module

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.Capability
import com.doublesymmetry.kotlinaudio.models.RepeatMode
import com.facebook.react.bridge.*
import com.google.android.exoplayer2.Player
import com.guichaguri.trackplayer.model.State
import com.guichaguri.trackplayer.model.Track
import com.guichaguri.trackplayer.model.asLibState
import com.guichaguri.trackplayer.module_old.MusicEvents
import com.guichaguri.trackplayer.module_old.MusicEvents.Companion.EVENT_INTENT
import com.guichaguri.trackplayer.service.MusicService
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.MainScope
import java.util.*
import javax.annotation.Nonnull

/**
 * @author Guichaguri
 */
class MusicModule(private val reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext), ServiceConnection {
    private var binder: MusicService.MusicBinder? = null
    private var eventHandler: MusicEvents? = null
    private var playerOptions: Bundle? = null
    private var isServiceBound = false
    private var playerSetUpPromise: Promise? = null

    private lateinit var musicService: MusicService

    private val mainScope = MainScope()

    @Nonnull
    override fun getName(): String {
        return "TrackPlayerModule"
    }

    override fun initialize() {
        val context: ReactContext = reactApplicationContext
        val manager = LocalBroadcastManager.getInstance(context)

        Logger.addLogAdapter(AndroidLogAdapter())

        eventHandler = MusicEvents(context)
        manager.registerReceiver(eventHandler!!, IntentFilter(EVENT_INTENT))
    }

    override fun onCatalystInstanceDestroy() {
        val context: ReactContext = reactApplicationContext
        if (eventHandler != null) {
            val manager = LocalBroadcastManager.getInstance(context)
            manager.unregisterReceiver(eventHandler!!)
            eventHandler = null
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
        musicService = binder.service
        musicService.setupPlayer(playerOptions, playerSetUpPromise)

        isServiceBound = true

//
//        // Reapply options that user set before with updateOptions
//        if (options != null) {
//            binder!!.updateOptions(options)
//        }
//
//        // Triggers all callbacks
//        while (!initCallbacks.isEmpty()) {
//            binder!!.post(initCallbacks.remove())
//        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        musicService.destroy()
        isServiceBound = false
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

    /* ****************************** API ****************************** */
    override fun getConstants(): Map<String, Any> {
        val constants: MutableMap<String, Any> = HashMap()

        // Capabilities
        constants["CAPABILITY_PLAY"] = Capability.PLAY.ordinal
        constants["CAPABILITY_PLAY_FROM_ID"] = Capability.PLAY_FROM_ID.ordinal
        constants["CAPABILITY_PLAY_FROM_SEARCH"] = Capability.PLAY_FROM_SEARCH.ordinal
        constants["CAPABILITY_PAUSE"] = Capability.PAUSE.ordinal
        constants["CAPABILITY_STOP"] = Capability.STOP.ordinal
        constants["CAPABILITY_SEEK_TO"] = Capability.SEEK_TO.ordinal
        constants["CAPABILITY_SKIP"] = Capability.SKIP.ordinal
        constants["CAPABILITY_SKIP_TO_NEXT"] = Capability.SKIP_TO_NEXT.ordinal
        constants["CAPABILITY_SKIP_TO_PREVIOUS"] = Capability.SKIP_TO_PREVIOUS.ordinal
        constants["CAPABILITY_SET_RATING"] = Capability.SET_RATING.ordinal
        constants["CAPABILITY_JUMP_FORWARD"] = Capability.JUMP_FORWARD.ordinal
        constants["CAPABILITY_JUMP_BACKWARD"] = Capability.JUMP_BACKWARD.ordinal

        // States
        constants["STATE_NONE"] = State.None.ordinal
        constants["STATE_READY"] = State.Paused.ordinal
        constants["STATE_PLAYING"] = State.Playing.ordinal
        constants["STATE_PAUSED"] = State.Paused.ordinal
        constants["STATE_STOPPED"] = State.Stopped.ordinal
        constants["STATE_BUFFERING"] = State.Buffering.ordinal
        constants["STATE_CONNECTING"] = State.Connecting.ordinal

        // Rating Types
        constants["RATING_HEART"] = RatingCompat.RATING_HEART
        constants["RATING_THUMBS_UP_DOWN"] = RatingCompat.RATING_THUMB_UP_DOWN
        constants["RATING_3_STARS"] = RatingCompat.RATING_3_STARS
        constants["RATING_4_STARS"] = RatingCompat.RATING_4_STARS
        constants["RATING_5_STARS"] = RatingCompat.RATING_5_STARS
        constants["RATING_PERCENTAGE"] = RatingCompat.RATING_PERCENTAGE

        // Repeat Modes
        constants["REPEAT_OFF"] = Player.REPEAT_MODE_OFF
        constants["REPEAT_TRACK"] = Player.REPEAT_MODE_ONE
        constants["REPEAT_QUEUE"] = Player.REPEAT_MODE_ALL
        return constants
    }

    @ReactMethod
    fun setupPlayer(data: ReadableMap?, promise: Promise) {
        if (isServiceBound) {
            promise.reject("player_already_initialized", "The player has already been initialized via setupPlayer.")
            return
        }

        playerSetUpPromise = promise
        playerOptions = Arguments.toBundle(data)

        val intent = Intent(reactContext, MusicService::class.java)
        reactContext?.startService(intent)
        reactContext?.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    @ReactMethod
    fun destroy() {
        if (isServiceBound) {
            reactApplicationContext.unbindService(this)
            isServiceBound = false
        }

        musicService.destroy()
        binder = null
    }

    @ReactMethod
    fun updateOptions(data: ReadableMap?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        val options = Arguments.toBundle(data)

        options?.let {
            musicService.updateOptions(it)
        }

        callback.resolve(null)

    }

    @ReactMethod
    fun add(data: ReadableArray?, insertBeforeIndex: Int, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        val bundleList = Arguments.toList(data)
        val tracks: List<Track> = try {
            Track.createTracks(
                reactApplicationContext, bundleList, RatingCompat.RATING_HEART
            )!!


        } catch (ex: Exception) {
            callback.reject("invalid_track_object", ex)
            return
        }

        musicService.apply {
            add(tracks)
            callback.resolve(null)
        }
    }

    @ReactMethod
    fun remove(data: ReadableArray?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        val trackList = Arguments.toList(data)
        val queue = musicService.tracks
        val indexes: MutableList<Int> = ArrayList()
        for (o in trackList!!) {
            val index = if (o is Int) o else o.toString().toInt()

            // we do not allow removal of the current item
            musicService.getCurrentTrackIndex {
                val currentIndex = it
                if (index == currentIndex) return@getCurrentTrackIndex
                if (index >= 0 && index < queue.size) {
                    indexes.add(index)
                }
            }
        }

        if (indexes.isNotEmpty())
            musicService.remove(indexes)

        callback.resolve(null)
    }

    @ReactMethod
    fun updateMetadataForTrack(index: Int, map: ReadableMap?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

//        waitForConnection {
//            val playback = binder?.playback
//            val tracks = playback!!.tracks
//            if (index < 0 || index >= tracks!!.size) {
//                callback.reject("index_out_of_bounds", "The index is out of bounds")
//            } else {
//                val track = tracks[index]
//                track!!.setMetadata(
//                    reactApplicationContext,
//                    Arguments.toBundle(map),
//                    binder?.ratingType!!
//                )
//                playback.updateTrack(index, track)
//                callback.resolve(null)
//            }
//        }
    }

    @ReactMethod
    fun updateNowPlayingMetadata(map: ReadableMap?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

//        val data = Arguments.toBundle(map)
//        waitForConnection {
//            val metadata = NowPlayingMetadata(reactApplicationContext, data, binder?.ratingType!!)
//            binder!!.updateNowPlayingMetadata(metadata)
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun clearNowPlayingMetadata(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

//        waitForConnection {
//            binder!!.clearNowPlayingMetadata()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun removeUpcomingTracks(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.removeUpcomingTracks()
        callback.resolve(null)
    }

    @ReactMethod
    fun skip(index: Int, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.skip(index)
        callback.resolve(null)
    }

    @ReactMethod
    fun skipToNext(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return
        musicService.skipToNext()
        callback.resolve(null)
    }

    @ReactMethod
    fun skipToPrevious(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.skipToPrevious()
        callback.resolve(null)
    }

    @ReactMethod
    fun reset(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.destroy()
        callback.resolve(null)
    }

    @ReactMethod
    fun play(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.play()
        callback.resolve(null)
//        waitForConnection {
//            binder?.playback?.play()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun pause(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.pause()
        callback.resolve(null)
//        waitForConnection {
//            binder?.playback?.pause()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun stop(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.pause()
        callback.resolve(null)

//        waitForConnection {
//            binder?.playback?.stop()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun seekTo(seconds: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.seekTo(seconds)
        callback.resolve(null)
//        waitForConnection {
//            val secondsToSkip = Utils.toMillis(seconds.toDouble())
//            binder?.playback?.seekTo(secondsToSkip)
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun setVolume(volume: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.volume = volume
        callback.resolve(null)
    }

    @ReactMethod
    fun getVolume(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        callback.resolve(musicService.volume)
    }

    @ReactMethod
    fun setRate(rate: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.rate = rate
        callback.resolve(null)
    }

    @ReactMethod
    fun getRate(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        callback.resolve(musicService.rate)
    }

    @ReactMethod
    fun setRepeatMode(mode: Int, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.repeatMode = RepeatMode.fromOrdinal(mode)
        callback.resolve(null)
    }

    @ReactMethod
    fun getRepeatMode(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        callback.resolve(musicService.repeatMode.ordinal)
    }

    @ReactMethod
    fun getTrack(index: Int, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        if (index >= 0 && index < musicService.tracks.size) {
            callback.resolve(Arguments.fromBundle(musicService.tracks[index].originalItem))
        } else {
            callback.resolve(null)
        }
    }

    @ReactMethod
    fun getQueue(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        callback.resolve(Arguments.fromList(musicService.tracks.map { it.originalItem }))
    }

    @ReactMethod
    fun getCurrentTrack(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getCurrentTrackIndex {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getDuration(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getDurationInSeconds {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getBufferedPosition(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getBufferedPositionInSeconds {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getPosition(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getPositionInSeconds {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getState(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        if (!::musicService.isInitialized) {
            callback.resolve(State.None.ordinal)
        } else {
            callback.resolve(musicService.event.stateChange.value.asLibState.ordinal)
        }
    }
}