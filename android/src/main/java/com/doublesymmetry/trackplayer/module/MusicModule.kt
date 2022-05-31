package com.doublesymmetry.trackplayer.module

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.Capability
import com.doublesymmetry.kotlinaudio.models.RepeatMode
import com.doublesymmetry.trackplayer.extensions.asLibState
import com.doublesymmetry.trackplayer.interfaces.ActivityLifecycleCallbacksAdapter
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
 */
class MusicModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), ServiceConnection, ActivityLifecycleCallbacksAdapter {
    private var eventHandler: MusicEvents? = null
    private var playerOptions: Bundle? = null
    private var isServiceBound = false
    private var playerSetUpPromise: Promise? = null

    private lateinit var musicService: MusicService

    val context = reactContext

    @Nonnull
    override fun getName(): String {
        return "TrackPlayerModule"
    }

    override fun initialize() {
        currentActivity?.application?.registerActivityLifecycleCallbacks(this)
        Logger.addLogAdapter(AndroidLogAdapter())
    }

    override fun onActivityStarted(activity: Activity) {
        // Service MUST be rebound during activity onStart, if not already bound
        if (isServiceBound) return

        Intent(context, MusicService::class.java).also { intent ->
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        // Service MUST be unbound during activity onStop
        unbindService()
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Service MUST be destroyed during activity onDestroy
        destroyServiceIfAllowed()
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        // If a binder already exists, don't get a new one
        if (!::musicService.isInitialized) {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
            musicService = binder.service
            musicService.setupPlayer(playerOptions, playerSetUpPromise)
        }

        isServiceBound = true
    }

    /**
     * Called when a connection to the Service has been lost.
     */
    override fun onServiceDisconnected(name: ComponentName) {
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

    private fun unbindService() {
        // The music service will not stop unless we unbind it first.
        if (isServiceBound) {
            context.unbindService(this)
            isServiceBound = false
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
        constants["STATE_NONE"] = State.Idle.ordinal
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

        // Validate buffer keys.
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


        val manager = LocalBroadcastManager.getInstance(context)
        eventHandler = MusicEvents(context)
        manager.registerReceiver(eventHandler!!, IntentFilter(EVENT_INTENT))

        Intent(context, MusicService::class.java).also { intent ->
            ContextCompat.startForegroundService(context, intent)
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    @ReactMethod
    @Deprecated("Backwards compatible function from the old android implementation. Should be removed in V3")
    fun isServiceRunning(callback: Promise) {
        callback.resolve(isServiceBound)
    }

    @ReactMethod
    fun destroy(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return
        // This function can only be called manually, so we force the service to be destroyed.
        destroyServiceIfAllowed(true)
    }

    /**
     * Destroy the music service if it's configured to stop with the activities.
     * @see [MusicService.stopWithApp]
     */
    private fun destroyServiceIfAllowed(forceDestroy: Boolean = false) {
        if (!musicService.stopWithApp) return
        musicService.destroyIfAllowed(forceDestroy)
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
                tracks.add(Track(context, it, musicService.ratingType))
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
            val context: ReactContext = context
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

        val context: ReactContext = context
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
        musicService.removeUpcomingTracks()
        callback.resolve(null)
    }

    @ReactMethod
    fun skip(index: Int, initialTime: Float, callback: Promise) {
        musicService.skip(index)

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun skipToNext(initialTime: Float, callback: Promise) {
        musicService.skipToNext()

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun skipToPrevious(initialTime: Float, callback: Promise) {

        musicService.skipToPrevious()

        if (initialTime >= 0) {
            musicService.seekTo(initialTime)
        }

        callback.resolve(null)
    }

    @ReactMethod
    fun reset(callback: Promise) {
        if (verifyServiceBoundOrReject(callback)) return

        musicService.stopPlayer()
        callback.resolve(null)
    }

    @ReactMethod
    fun play(callback: Promise) {
//    if (verifyServiceBoundOrReject(callback)) return

        musicService.play()
        callback.resolve(null)
    }

    @ReactMethod
    fun pause(callback: Promise) {
//    if (verifyServiceBoundOrReject(callback)) return

        musicService.pause()
        callback.resolve(null)
    }

    @ReactMethod
    fun stop(callback: Promise) {
//    if (verifyServiceBoundOrReject(callback)) return

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
        musicService.getRate {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun setRepeatMode(mode: Int, callback: Promise) {
        musicService.setRepeatMode(RepeatMode.fromOrdinal(mode))
        callback.resolve(null)
    }

    @ReactMethod
    fun getRepeatMode(callback: Promise) {
        musicService.getRepeatMode {
            callback.resolve(it.ordinal)
        }
    }

    @ReactMethod
    fun getTrack(index: Int, callback: Promise) {
        if (index >= 0 && index < musicService.tracks.size) {
            callback.resolve(Arguments.fromBundle(musicService.tracks[index].originalItem))
        } else {
            callback.resolve(null)
        }
    }

    @ReactMethod
    fun getQueue(callback: Promise) {
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
        musicService.getDurationInSeconds {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getBufferedPosition(callback: Promise) {
        musicService.getBufferedPositionInSeconds {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getPosition(callback: Promise) {
        musicService.getPositionInSeconds {
            callback.resolve(it)
        }
    }

    @ReactMethod
    fun getState(callback: Promise) {
        if (!::musicService.isInitialized) {
            callback.resolve(State.Idle.ordinal)
        } else {
            callback.resolve(musicService.event.stateChange.value.asLibState.ordinal)
        }
    }
<<<<<<< HEAD
}
=======

    companion object {
        val TAG: String = MusicModule::class.java.simpleName
    }
}
>>>>>>> main