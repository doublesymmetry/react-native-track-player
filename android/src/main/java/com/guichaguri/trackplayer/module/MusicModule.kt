package com.guichaguri.trackplayer.module

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.facebook.react.bridge.*
import com.google.android.exoplayer2.Player
import com.guichaguri.trackplayer.module_old.MusicEvents
import com.guichaguri.trackplayer.service_old.MusicBinder
import java.util.*
import javax.annotation.Nonnull

/**
 * @author Guichaguri
 */
class MusicModule(reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext), ServiceConnection {
    private var binder: MusicBinder? = null
    private var eventHandler: MusicEvents? = null
    private val initCallbacks = ArrayDeque<Runnable>()
    private var connecting = false
    private var options: Bundle? = null

    lateinit var queuedAudioPlayer: QueuedAudioPlayer

    @Nonnull
    override fun getName(): String {
        return "TrackPlayerModule"
    }
//
    override fun initialize() {
        val context: ReactContext = reactApplicationContext
        val manager = LocalBroadcastManager.getInstance(context)
//
//        context.runOnUiQueueThread { // TODO: Do this in lib
//            queuedAudioPlayer = QueuedAudioPlayer(context)
//        }
//
//        eventHandler = MusicEvents(context)
//        manager.registerReceiver(eventHandler!!, IntentFilter(Utils.EVENT_INTENT))
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
//        binder = service as MusicBinder
//        connecting = false
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
//        binder = null
//        connecting = false
    }

    /**
     * Waits for a connection to the service and/or runs the [Runnable] in the player thread
     */
    private fun waitForConnection(r: Runnable) {
//        if (binder != null) {
//            binder!!.post(r)
//            return
//        } else {
//            initCallbacks.add(r)
//        }
//        if (connecting) return
//        val context = reactApplicationContext
//
//         Binds the service to get a MediaWrapper instance
//        val intent = Intent(context, MusicService::class.java)
//        context.startService(intent)
//        intent.action = Utils.CONNECT_INTENT
//        context.bindService(intent, this, 0)
//        connecting = true
    }

    /* ****************************** API ****************************** */
    override fun getConstants(): Map<String, Any>? {
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
//        val options = Arguments.toBundle(data)
//        waitForConnection { binder!!.setupPlayer(options, promise) }
    }

    @ReactMethod
    fun destroy() {
//        // Ignore if it was already destroyed
//        if (binder == null && !connecting) return
//        try {
//            if (binder != null) {
//                binder!!.destroy()
//                binder = null
//            }
//            val context: ReactContext? = reactApplicationContext
//            context?.unbindService(this)
//        } catch (ex: Exception) {
//            // This method shouldn't be throwing unhandled errors even if something goes wrong.
//            Log.e(Utils.LOG, "An error occurred while destroying the service", ex)
//        }
    }

    @ReactMethod
    fun updateOptions(data: ReadableMap?, callback: Promise) {
//        // keep options as we may need them for correct MetadataManager reinitialization later
//        options = Arguments.toBundle(data)
//        waitForConnection {
//            binder!!.updateOptions(options)
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun add(tracks: ReadableArray?, insertBeforeIndex: Int, callback: Promise) {
//        val bundleList = Arguments.toList(tracks)
//        waitForConnection {
//            val trackList: List<Track> = try {
//                Track.createTracks(
//                    reactApplicationContext, bundleList, binder?.ratingType!!
//                )!!
//            } catch (ex: Exception) {
//                callback.reject("invalid_track_object", ex)
//                return@waitForConnection
//            }
//
//            //TODO: Remove Tracks and stick to just AudioItems
//
//            val items = trackList.map {
//                DefaultAudioItem(it.uri.toString(), SourceType.FILE, it.artist, it.title, it.album, it.artwork.toString())
//            }
//
//            queuedAudioPlayer.add(items)
//
////            val queue = binder?.playback?.queue
////            // -1 means no index was passed and therefore should be inserted at the end.
////            val index = if (insertBeforeIndex != -1) insertBeforeIndex else queue!!.size
////            if (index < 0 || index > queue!!.size) {
////                callback.reject("index_out_of_bounds", "The track index is out of bounds")
////            } else if (trackList == null || trackList.isEmpty()) {
////                callback.reject("invalid_track_object", "Track is missing a required key")
////            } else if (trackList.size == 1) {
////                binder?.playback?.add(trackList[0], index, callback)
////            } else {
////
////                binder?.playback?.add(trackList, index, callback)
//////                binder?.playback?.add(trackList, index, callback)
////            }
//        }
    }

    @ReactMethod
    fun remove(tracks: ReadableArray?, callback: Promise) {
//        val trackList = Arguments.toList(tracks)
//        waitForConnection {
//            val queue = binder?.playback?.queue
//            val indexes: MutableList<Int> = ArrayList()
//            for (o in trackList!!) {
//                val index = if (o is Int) o else o.toString().toInt()
//
//                // we do not allow removal of the current item
//                val currentIndex = binder?.playback?.currentTrackIndex
//                if (index == currentIndex) continue
//                if (index >= 0 && index < queue!!.size) {
//                    indexes.add(index)
//                }
//            }
//            if (!indexes.isEmpty()) {
//                binder?.playback?.remove(indexes, callback)
//            } else {
//                callback.resolve(null)
//            }
//        }
    }

    @ReactMethod
    fun updateMetadataForTrack(index: Int, map: ReadableMap?, callback: Promise) {
//        waitForConnection {
//            val playback = binder?.playback
//            val queue = playback!!.queue
//            if (index < 0 || index >= queue!!.size) {
//                callback.reject("index_out_of_bounds", "The index is out of bounds")
//            } else {
//                val track = queue[index]
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
//        waitForConnection {
//            binder?.playback?.removeUpcomingTracks()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun skip(index: Int, callback: Promise?) {
//        waitForConnection { binder?.playback?.skip(index, callback!!) }
    }

    @ReactMethod
    fun skipToNext(callback: Promise?) {
//        waitForConnection { binder?.playback?.skipToNext(callback!!) }
    }

    @ReactMethod
    fun skipToPrevious(callback: Promise?) {
//        waitForConnection { binder?.playback?.skipToPrevious(callback!!) }
    }

    @ReactMethod
    fun reset(callback: Promise) {
//        waitForConnection {
//            binder?.playback?.reset()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun play(callback: Promise) {
//        waitForConnection {
//            binder?.playback?.play()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun pause(callback: Promise) {
//        waitForConnection {
//            binder?.playback?.pause()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun stop(callback: Promise) {
//        waitForConnection {
//            binder?.playback?.stop()
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun seekTo(seconds: Float, callback: Promise) {
//        waitForConnection {
//            val secondsToSkip = Utils.toMillis(seconds.toDouble())
//            binder?.playback?.seekTo(secondsToSkip)
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun setVolume(volume: Float, callback: Promise) {
//        waitForConnection {
//            binder?.playback?.volume = volume
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun getVolume(callback: Promise) {
//        waitForConnection { callback.resolve(binder?.playback?.volume) }
    }

    @ReactMethod
    fun setRate(rate: Float, callback: Promise) {
//        waitForConnection {
//            binder?.playback?.rate = rate
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun getRate(callback: Promise) {
//        waitForConnection { callback.resolve(binder?.playback?.rate) }
    }

    @ReactMethod
    fun setRepeatMode(mode: Int, callback: Promise) {
//        waitForConnection {
//            binder?.playback?.repeatMode = mode
//            callback.resolve(null)
//        }
    }

    @ReactMethod
    fun getRepeatMode(callback: Promise) {
//        waitForConnection { callback.resolve(binder?.playback?.repeatMode) }
    }

    @ReactMethod
    fun getTrack(index: Int, callback: Promise) {
//        waitForConnection {
//            val tracks = binder?.playback?.queue
//            if (index >= 0 && index < tracks!!.size) {
//                callback.resolve(Arguments.fromBundle(tracks[index]!!.originalItem))
//            } else {
//                callback.resolve(null)
//            }
//        }
    }

    @ReactMethod
    fun getQueue(callback: Promise) {
//        waitForConnection {
//            val queue = ArrayList<Bundle?>()
//            val tracks = binder?.playback?.queue
//            for (track in tracks!!) {
//                queue.add(track?.originalItem)
//            }
//            callback.resolve(Arguments.fromList(queue))
//        }
    }

    @ReactMethod
    fun getCurrentTrack(callback: Promise) {
//        waitForConnection { callback.resolve(binder?.playback?.currentTrackIndex) }
    }

    @ReactMethod
    fun getDuration(callback: Promise) {
//        waitForConnection {
//            val duration = binder?.playback?.duration
//            if (duration == C.TIME_UNSET) {
//                callback.resolve(Utils.toSeconds(0))
//            } else {
//                callback.resolve(Utils.toSeconds(duration!!))
//            }
//        }
    }

    @ReactMethod
    fun getBufferedPosition(callback: Promise) {
//        waitForConnection {
//            val position = binder?.playback?.bufferedPosition
//            if (position == C.POSITION_UNSET.toLong()) {
//                callback.resolve(Utils.toSeconds(0))
//            } else {
//                callback.resolve(Utils.toSeconds(position!!))
//            }
//        }
    }

    @ReactMethod
    fun getPosition(callback: Promise) {
//        waitForConnection {
//            val position = binder?.playback?.position
//            if (position == C.POSITION_UNSET.toLong()) {
//                callback.reject("unknown", "Unknown position")
//            } else {
//                callback.resolve(Utils.toSeconds(position!!))
//            }
//        }
    }

    @ReactMethod
    fun getState(callback: Promise) {
//        if (binder == null) {
//            callback.resolve(PlaybackStateCompat.STATE_NONE)
//        } else {
//            waitForConnection { callback.resolve(binder!!.playback.state) }
//        }
    }
}