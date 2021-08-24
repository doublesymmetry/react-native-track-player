package com.doublesymmetry.kotlinaudio.models

enum class AudioPlayerState {
    /**
     * An [AudioItem] is being loaded for playback.
     */
    LOADING,

    /**
     * The current [AudioItem] is loaded, and the player is ready to start playing.
     */
    READY,

    /**
     * The current [AudioItem] is playing, but is currently buffering.
     */
    BUFFERING,

    /**
     * The player is paused.
     */
    PAUSED,

    /**
     * The player is playing.
     */
    PLAYING,

    /**
     * No [AudioItem] is loaded and the player is doing nothing.
     */
    IDLE
}