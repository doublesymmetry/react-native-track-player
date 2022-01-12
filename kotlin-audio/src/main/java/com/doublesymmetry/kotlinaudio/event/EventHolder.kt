package com.doublesymmetry.kotlinaudio.event

class EventHolder internal constructor(private val notificationEventHolder: NotificationEventHolder, private val playerEventHolder: PlayerEventHolder) {
    val stateChange
        get() = playerEventHolder.stateChange

    val playbackEnd
        get() = playerEventHolder.playbackEnd

    val audioItemTransition
        get() = playerEventHolder.audioItemTransition

    val onAudioFocusChanged
        get() = playerEventHolder.onAudioFocusChanged

    val onPlaybackMetadata
        get() = playerEventHolder.onPlaybackMetadata

    val onNotificationButtonTapped
        get() = notificationEventHolder.onNotificationButtonTapped

    val notificationStateChange
        get() = notificationEventHolder.notificationStateChange

    val onMediaSessionCallbackTriggered
        get() = notificationEventHolder.onMediaSessionCallbackTriggered
}