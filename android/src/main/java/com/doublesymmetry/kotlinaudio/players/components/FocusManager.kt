package com.doublesymmetry.kotlinaudio.players.components

import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.doublesymmetry.kotlinaudio.models.PlayerOptions

class FocusManager (
    private val context: Context,
    private val listener: AudioManager.OnAudioFocusChangeListener,
    private val options: PlayerOptions
) {
    var hasAudioFocus = false
    var alwaysPauseOnInterruption = options.alwaysPauseOnInterruption
    private var focus: AudioFocusRequestCompat? = null

    fun requestAudioFocus() {
        if (hasAudioFocus) return

        val manager = ContextCompat.getSystemService(context, AudioManager::class.java)

        focus = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(listener)
            .setAudioAttributes(
                AudioAttributesCompat.Builder()
                    .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                    .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setWillPauseWhenDucked(alwaysPauseOnInterruption)
            .build()

        val result: Int = if (manager != null && focus != null) {
            AudioManagerCompat.requestAudioFocus(manager, focus!!)
        } else {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    }

    fun abandonAudioFocusIfHeld() {
        if (!hasAudioFocus) return

        val manager = ContextCompat.getSystemService(context, AudioManager::class.java)

        val result: Int = if (manager != null && focus != null) {
            AudioManagerCompat.abandonAudioFocusRequest(manager, focus!!)
        } else {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED
        }

        hasAudioFocus = (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    }

}