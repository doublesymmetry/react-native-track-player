package com.doublesymmetry.trackplayer.module

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.Capability
import com.doublesymmetry.kotlinaudio.models.RepeatMode
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.interfaces.LifecycleEventsListener
import com.doublesymmetry.trackplayer.model.State
import com.doublesymmetry.trackplayer.model.Track
import com.doublesymmetry.trackplayer.module.MusicEvents.Companion.EVENT_INTENT
import com.doublesymmetry.trackplayer.service.MusicService
import com.doublesymmetry.trackplayer.utils.Utils
import com.facebook.react.bridge.*
import com.google.android.exoplayer2.DefaultLoadControl.*
import com.google.android.exoplayer2.Player
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import timber.log.Timber
import javax.annotation.Nonnull

/**
 * @author Milen Pivchev @mpivchev
 *
 */
class MusicModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ServiceConnection, LifecycleEventsListener {
    private var binder: MusicService.MusicBinder? = null
    private var eventHandler: MusicEvents? = null
    private var playerOptions: Bundle? = null
    private var isServiceBound = false
    private var playerSetUpPromise: Promise? = null

    private lateinit var musicService: MusicService

    @Nonnull
    override fun getName(): String {
        return "TrackPlayerModule"
    }

    override fun initialize() {
        val context: ReactContext = reactApplicationContext
        context.addLifecycleEventListener(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }

    /**
     * Called when the React context is destroyed or reloaded.
     */
    override fun onCatalystInstanceDestroy() {
        if (!isServiceBound) return

        musicService.destroyIfAllowed(true)
        unbindFromService()
    }

    /**
     * Called when host activity receives destroy event (e.g. {@link Activity#onDestroy}. Only called
     * for the last React activity to be destroyed.
     */
    override fun onHostDestroy() {
        destroyIfAllowed()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
        musicService = binder.service
        musicService.setupPlayer(playerOptions, playerSetUpPromise)

        isServiceBound = true
    }

    /**
     * Called when a connection to the Service has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName) {
        musicService.destroyIfAllowed()
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

    private fun unbindFromService() {
        val context: ReactContext = reactApplicationContext

        // The music service will not stop unless we unbind it first.
        if (isServiceBound) {
            context.unbindService(this)
            isServiceBound = false
            binder = null
        }

        if (eventHandler != null) {
            val manager = LocalBroadcastManager.getInstance(context)
            manager.unregisterReceiver(eventHandler!!)
            eventHandler = null
        }
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
        constants["STATE_READY"] = State.Ready.ordinal
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
            promise.reject(
                    "player_already_initialized",
                    "The player has already been initialized via setupPlayer."
            )
            return
        }

        // validate buffer keys.
        val bundledData = Arguments.toBundle(data)
        val minBuffer = bundledData?.getDouble(MusicService.MIN_BUFFER_KEY)
                ?.let { Utils.toMillis(it).toInt() } ?: DEFAULT_MIN_BUFFER_MS
        val maxBuffer = bundledData?.getDouble(MusicService.MAX_BUFFER_KEY)
                ?.let { Utils.toMillis(it).toInt() } ?: DEFAULT_MAX_BUFFER_MS
        val playBuffer = bundledData?.getDouble(MusicService.PLAY_BUFFER_KEY)
                ?.let { Utils.toMillis(it).toInt() } ?: DEFAULT_BUFFER_FOR_PLAYBACK_MS
        val backBuffer = bundledData?.getDouble(MusicService.BACK_BUFFER_KEY)
                ?.let { Utils.toMillis(it).toInt() } ?: DEFAULT_BACK_BUFFER_DURATION_MS

        if (playBuffer < 0) {
            promise.reject(
                    "play_buffer_error",
                    "The value for playBuffer should be greater than or equal to zero."
            )
        }

        if (backBuffer < 0) {
            promise.reject(
                    "back_buffer_error",
                    "The value for backBuffer should be greater than or equal to zero."
            )
        }

        if (minBuffer < playBuffer) {
            promise.reject(
                    "min_buffer_error",
                    "The value for minBuffer should be greater than or equal to playBuffer."
            )
        }

        if (maxBuffer < minBuffer) {
            promise.reject(
                    "min_buffer_error",
                    "The value for maxBuffer should be greater than or equal to minBuffer."
            )
        }

        playerSetUpPromise = promise
        playerOptions = bundledData

        val context: ReactContext = reactApplicationContext

        val manager = LocalBroadcastManager.getInstance(context)
        eventHandler = MusicEvents(context)
        manager.registerReceiver(eventHandler!!, IntentFilter(EVENT_INTENT))

        Intent(context, MusicService::class.java).also { intent ->
            context.startService(intent)
            context.bindService(intent, this, 0)
        }
    }

    @ReactMethod
    fun isServiceRunning(callback: Promise) {
        callback.resolve(isServiceBound)
    }

    @ReactMethod
    fun destroy(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.destroyIfAllowed(true)
        unbindFromService()
    }

    private fun destroyIfAllowed() {
        // There's nothing to destroy if we have not been service bound yet.
        if (!isServiceBound) return

        musicService.destroyIfAllowed()
        if (!musicService.stopWithApp) return

        unbindFromService()
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

        val tracks: MutableList<Track> = mutableListOf()
        val bundleList = Arguments.toList(data)

        if (bundleList !is ArrayList) {
            callback.reject("invalid_parameter", "Was not given an array of tracks")
            return
        }

        bundleList.forEach {
            if (it is Bundle) {
                tracks.add(Track(reactApplicationContext, it, musicService.ratingType))
            } else {
                callback.reject("invalid_track_object", "Track was not a dictionary type")
            }
        }

        when {
            insertBeforeIndex < -1 || insertBeforeIndex > musicService.tracks.size -> {
                callback.reject("index_out_of_bounds", "The track index is out of bounds")
            }
            insertBeforeIndex == -1 -> {
                musicService.apply {
                    add(tracks)
                    callback.resolve(null)
                }
            }
            else -> {
                musicService.apply {
                    add(tracks, insertBeforeIndex)
                    callback.resolve(null)
                }
            }
        }
    }

    @ReactMethod
    fun remove(data: ReadableArray?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        val trackList = Arguments.toList(data)
        val queue = musicService.tracks

        if (trackList != null) {
            musicService.getCurrentTrackIndex {
                for (track in trackList) {
                    val index = if (track is Int) track else track.toString().toInt()

                    // We do not allow removal of the current item
                    if (index == it) {
                        Timber.e("This track is currently playing, so it can't be removed")
                        return@getCurrentTrackIndex
                    } else if (index >= 0 && index < queue.size) {
                        musicService.remove(index)
                    }
                }
            }
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun updateMetadataForTrack(index: Int, map: ReadableMap?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        if (index < 0 || index >= musicService.tracks.size) {
            callback.reject("index_out_of_bounds", "The index is out of bounds")
        } else {
            val context: ReactContext = reactApplicationContext
            val track = musicService.tracks[index]
            track.setMetadata(context, Arguments.toBundle(map), musicService.ratingType)
            musicService.updateMetadataForTrack(index, track)

            callback.resolve(null)
        }
    }

    @ReactMethod
    fun updateNowPlayingMetadata(map: ReadableMap?, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return
        if (musicService.tracks.isEmpty())
            callback.reject("no_current_item", "There is no current item in the player")

        val context: ReactContext = reactApplicationContext
        val metadata = Arguments.toBundle(map)
        musicService.updateNotificationMetadata(
                metadata?.getString("title"),
                metadata?.getString("artist"),
                Utils.getUri(context, metadata, "artwork")?.toString()
        )

        callback.resolve(null)
    }

    @ReactMethod
    fun clearNowPlayingMetadata(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        if (musicService.tracks.isEmpty())
            callback.reject("no_current_item", "There is no current item in the player")

        musicService.clearNotificationMetadata()
        callback.resolve(null)
    }

    @ReactMethod
    fun removeUpcomingTracks(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.removeUpcomingTracks()
        callback.resolve(null)
    }

    @ReactMethod
    fun skip(index: Int, initialTime: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.skip(index)

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun skipToNext(initialTime: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return
        musicService.skipToNext()

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun skipToPrevious(initialTime: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.skipToPrevious()

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun reset(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.stop()
        callback.resolve(null)
    }

    @ReactMethod
    fun play(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.play()
        callback.resolve(null)
    }

    @ReactMethod
    fun pause(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.pause()
        callback.resolve(null)
    }

    @ReactMethod
    fun stop(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.pause()
        callback.resolve(null)
    }

    @ReactMethod
    fun seekTo(seconds: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.seekTo(seconds)
        callback.resolve(null)
    }

    @ReactMethod
    fun setVolume(volume: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.setVolume(volume)
        callback.resolve(null)
    }

    @ReactMethod
    fun getVolume(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getVolume { callback.resolve(it) }
    }

    @ReactMethod
    fun setRate(rate: Float, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.setRate(rate)
        callback.resolve(null)
    }

    @ReactMethod
    fun getRate(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getRate {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun setRepeatMode(mode: Int, callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.setRepeatMode(RepeatMode.fromOrdinal(mode))
        callback.resolve(null)
    }

    @ReactMethod
    fun getRepeatMode(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.getRepeatMode {
            callback.resolve(it.ordinal)
        }
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

    companion object {
        val TAG: String = MusicModule::class.java.simpleName
    }
}
