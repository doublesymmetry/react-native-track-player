package com.doublesymmetry.kotlinaudio.event

import com.doublesymmetry.kotlinaudio.models.MediaSessionCallback
import com.doublesymmetry.kotlinaudio.models.NotificationButton
import com.doublesymmetry.kotlinaudio.models.NotificationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NotificationEventHolder {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var _onNotificationButtonTapped = MutableSharedFlow<NotificationButton>()
    var onNotificationButtonTapped = _onNotificationButtonTapped.asSharedFlow()

    private var _notificationStateChange = MutableSharedFlow<NotificationState>(1)
    var notificationStateChange = _notificationStateChange.asSharedFlow()

    private var _onMediaSessionCallbackTriggered = MutableSharedFlow<MediaSessionCallback>()
    var onMediaSessionCallbackTriggered = _onMediaSessionCallbackTriggered.asSharedFlow()

    internal fun updateOnNotificationButtonTapped(button: NotificationButton) {
        coroutineScope.launch {
            _onNotificationButtonTapped.emit(button)
        }
    }

    internal fun updateNotificationState(state: NotificationState) {
        coroutineScope.launch {
            _notificationStateChange.emit(state)
        }
    }

    internal fun updateOnMediaSessionCallbackTriggered(callback: MediaSessionCallback) {
        coroutineScope.launch {
            _onMediaSessionCallbackTriggered.emit(callback)
        }
    }
}