package com.guichaguri.trackplayer.module

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.models.AudioPlayerState
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer.RepeatMode.*
import com.facebook.react.bridge.*
import com.google.android.exoplayer2.Player
import com.guichaguri.trackplayer.model.Track
import com.guichaguri.trackplayer.module_old.MusicEvents
import com.guichaguri.trackplayer.module_old.MusicEvents.Companion.EVENT_INTENT
import com.guichaguri.trackplayer.service.MusicService
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.annotation.Nonnull

/**
 * @author Guichaguri
 */
class MusicModule(private val reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext), ServiceConnection {
    private var binder: MusicService.MusicBinder? = null
    private var eventHandler: MusicEvents? = null
    private val initCallbacks = ArrayDeque<Runnable>()
    private var playerOptions: Bundle? = null
    //    private var connecting = false
    private var isServiceBound = false
    private var options: Bundle? = null
    private var playerSetUpPromise: Promise? = null

    private lateinit var musicService: MusicService

    private val mainScope = MainScope()

    @Nonnull
    override fun getName(): String {
        return "TrackPlayerModule"
    }

    //
    override fun initialize() {
        val context: ReactContext = reactApplicationContext
        val manager = LocalBroadcastManager.getInstance(context)

        Logger.addLogAdapter(AndroidLogAdapter())

//        if (!isServiceBound)
//            context.bindService(Intent(context, MusicService::class.java), this, Context.BIND_AUTO_CREATE)
//
//        context.runOnUiQueueThread { // TODO: Do this in lib
//            queuedAudioPlayer = QueuedAudioPlayer(context)
//        }
////
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
        musicService.setupPlayer(playerOptions)

        isServiceBound = true
        playerSetUpPromise?.resolve(null)
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

    /* ****************************** API ****************************** */
    override fun getConstants(): Map<String, Any> {
        val constants: MutableMap<String, Any> = HashMap()

        // Capabilities
        constants["CAPABILITY_PLAY"] = PlaybackStateCompat.ACTION_PLAY
        constants["CAPABILITY_PLAY_FROM_ID"] = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
        constants["CAPABILITY_PLAY_FROM_SEARCH"] = PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
        constants["CAPABILITY_PAUSE"] = PlaybackStateCompat.ACTION_PAUSE
        constants["CAPABILITY_STOP"] = PlaybackStateCompat.ACTION_STOP
        constants["CAPABILITY_SEEK_TO"] = PlaybackStateCompat.ACTION_SEEK_TO
        constants["CAPABILITY_SKIP"] = PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
        constants["CAPABILITY_SKIP_TO_NEXT"] = PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        constants["CAPABILITY_SKIP_TO_PREVIOUS"] = PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        constants["CAPABILITY_SET_RATING"] = PlaybackStateCompat.ACTION_SET_RATING
        constants["CAPABILITY_JUMP_FORWARD"] = PlaybackStateCompat.ACTION_FAST_FORWARD
        constants["CAPABILITY_JUMP_BACKWARD"] = PlaybackStateCompat.ACTION_REWIND

        // States
        constants["STATE_NONE"] = PlaybackStateCompat.STATE_NONE
        constants["STATE_READY"] = PlaybackStateCompat.STATE_PAUSED
        constants["STATE_PLAYING"] = PlaybackStateCompat.STATE_PLAYING
        constants["STATE_PAUSED"] = PlaybackStateCompat.STATE_PAUSED
        constants["STATE_STOPPED"] = PlaybackStateCompat.STATE_STOPPED
        constants["STATE_BUFFERING"] = PlaybackStateCompat.STATE_BUFFERING
        constants["STATE_CONNECTING"] = PlaybackStateCompat.STATE_CONNECTING

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
        playerSetUpPromise = promise
        playerOptions = Arguments.toBundle(data)

        if (!isServiceBound)
            reactContext?.bindService(Intent(reactContext, MusicService::class.java), this, Context.BIND_AUTO_CREATE)
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
//        // keep options as we may need them for correct MetadataManager reinitialization later
//        options = Arguments.toBundle(data)
//        waitForConnection {
//            binder!!.updateOptions(options)
        callback.resolve(null)
//        }
    }

    @ReactMethod
    fun add(data: ReadableArray?, insertBeforeIndex: Int, callback: Promise) {
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
//        val data = Arguments.toBundle(map)
//        waitForConnection {
//            val metadata = NowPlayingMetadata(reactApplicationContext, data, binder?.ratingType!!)
//            binder!!.updateNowPlayingMetadata(metadata)
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun clearNowPlayingMetadata(callback: Promise) {
//        waitForConnection {
//            binder!!.clearNowPlayingMetadata()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun removeUpcomingTracks(callback: Promise) {
        musicService.removeUpcomingTracks()
        callback.resolve(null)
    }

    @ReactMethod
    fun skip(index: Int, callback: Promise?) {
        musicService.skip(index)
        callback?.resolve(null)
    }

    @ReactMethod
    fun skipToNext(callback: Promise?) {
        musicService.skipToNext()
        callback?.resolve(null)
    }

    @ReactMethod
    fun skipToPrevious(callback: Promise?) {
        musicService.skipToPrevious()
        callback?.resolve(null)
    }

    @ReactMethod
    fun reset(callback: Promise) {
        musicService.destroy()
        callback.resolve(null)
    }

    @ReactMethod
    fun play(callback: Promise) {
        musicService.play()
        callback.resolve(null)
//        waitForConnection {
//            binder?.playback?.play()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun pause(callback: Promise) {
        musicService.pause()
        callback.resolve(null)
//        waitForConnection {
//            binder?.playback?.pause()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun stop(callback: Promise) {
        musicService.pause()
        callback.resolve(null)

//        waitForConnection {
//            binder?.playback?.stop()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun seekTo(seconds: Float, callback: Promise) {
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
        musicService.volume = volume
        callback.resolve(null)
    }

    @ReactMethod
    fun getVolume(callback: Promise) {
        callback.resolve(musicService.volume)
    }

    @ReactMethod
    fun setRate(rate: Float, callback: Promise) {
        musicService.rate = rate
        callback.resolve(null)
    }

    @ReactMethod
    fun getRate(callback: Promise) {
        callback.resolve(musicService.rate)
    }

    @ReactMethod
    fun setRepeatMode(mode: Int, callback: Promise) {
        musicService.repeatMode = QueuedAudioPlayer.RepeatMode.fromOrdinal(mode)
        callback.resolve(null)
    }

    @ReactMethod
    fun getRepeatMode(callback: Promise) {
        callback.resolve(musicService.repeatMode.ordinal)
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
            callback.resolve(PlaybackStateCompat.STATE_NONE)
        } else {
            mainScope.launch {
                musicService.event.stateChange.collect {
                    when (it) {
                        AudioPlayerState.PLAYING -> {
                            callback.resolve(PlaybackStateCompat.STATE_PLAYING)
                        }
                        else -> {
                            callback.resolve(PlaybackStateCompat.STATE_PAUSED)
                        }
                    }
                }
            }
        }
    }
}