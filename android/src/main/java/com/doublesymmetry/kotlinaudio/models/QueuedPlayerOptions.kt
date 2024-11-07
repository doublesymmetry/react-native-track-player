package com.doublesymmetry.kotlinaudio.models

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

interface QueuedPlayerOptions : PlayerOptions {
    override var alwaysPauseOnInterruption: Boolean
    var repeatMode: RepeatMode
}

class DefaultQueuedPlayerOptions(
    private val exoPlayer: ExoPlayer,
    override var alwaysPauseOnInterruption: Boolean = false,
) : QueuedPlayerOptions {
    // Functions in data classes might or might not be a bit of a code smell.
    // I'm using the passed exoPlayer which breaks separation of concerns. But it's also useful.
    // More here: https://www.reddit.com/r/Kotlin/comments/ehqe4e/why_is_it_bad_practice_to_have_functions_in_data/
    // TODO: Figure out a way for this function to be outside of this data class
    override var repeatMode: RepeatMode
        get() {
            return when (exoPlayer.repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
        }
        set(value) {
            when (value) {
                RepeatMode.ALL -> exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                RepeatMode.ONE -> exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                RepeatMode.OFF -> exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            }
        }
}

enum class RepeatMode {
    OFF, ONE, ALL;

    companion object {
        fun fromOrdinal(ordinal: Int): RepeatMode {
            return when (ordinal) {
                0 -> OFF
                1 -> ONE
                2 -> ALL
                else -> error("Wrong ordinal")
            }
        }
    }
}
