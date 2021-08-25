package com.doublesymmetry.kotlinaudio.models

enum class AudioItemTransitionReason {
    /**
     * Playback has automatically transitioned to the next [AudioItem].
     *
     * This reason also indicates a transition caused by another player.
     */
    AUTO,

    /**
     * A seek to another [AudioItem] has occurred. Usually triggered when calling
     * [QueuedAudioPlayer.next][com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer.next]
     * or [QueuedAudioPlayer.previous][com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer.previous].
     */
    SEEK_TO_ANOTHER_AUDIO_ITEM,

    /**
     * The [AudioItem] has been repeated.
     */
    REPEAT,

    /**
     * The current [AudioItem] has changed because of a change in the queue. This can either be if
     * the [AudioItem] previously being played has been removed, or when the queue becomes non-empty
     * after being empty.
     */
    QUEUE_CHANGED
}