package com.guichaguri.trackplayer.service

import android.os.Binder
import android.os.Bundle
import android.support.v4.media.RatingCompat
import com.facebook.react.bridge.Promise
import com.guichaguri.trackplayer.service.models.NowPlayingMetadata
import com.guichaguri.trackplayer.service.player.ExoPlayback

/**
 * @author Guichaguri
 */
class MusicBinder(private val service: MusicService, private val manager: MusicManager?) :
    Binder() {
    fun post(r: Runnable?) {
        service.handler!!.post(r!!)
    }

    // TODO remove?
    val playback: ExoPlayback<*>
        get() {
            var playback = manager?.playback

            // TODO remove?
            if (playback == null) {
                playback = manager!!.createLocalPlayback(Bundle())
                manager.switchPlayback(playback)
            }
            return playback
        }

    fun setupPlayer(bundle: Bundle?, promise: Promise) {
        manager!!.switchPlayback(manager.createLocalPlayback(bundle))
        promise.resolve(null)
    }

    fun updateOptions(bundle: Bundle?) {
        manager!!.setStopWithApp(bundle!!.getBoolean("stopWithApp", false))
        manager.setAlwaysPauseOnInterruption(bundle.getBoolean("alwaysPauseOnInterruption", false))
        manager.metadata.updateOptions(bundle)
    }

    fun updateNowPlayingMetadata(nowPlaying: NowPlayingMetadata) {
        val metadata = manager?.metadata

        // TODO elapsedTime
        metadata?.updateMetadata(playback, nowPlaying)
        metadata?.setActive(true)
    }

    fun clearNowPlayingMetadata() {
        manager?.metadata?.setActive(false)
    }

    val ratingType: Int
        get() = manager?.metadata?.ratingType ?: RatingCompat.RATING_NONE

    fun destroy() {
        service.destroy()
        service.stopSelf()
    }
}