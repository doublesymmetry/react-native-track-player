package com.doublesymmetry.kotlinaudio.models

import androidx.core.app.NotificationCompat

data class NotificationAction(
    val action: NotificationCompat.Action,
    val type: NotificationActionType,
    val isCompact: Boolean,
)

sealed class NotificationActionType(open val value: String) {
    object ACTION_STOP : NotificationActionType("action_stop")
    object ACTION_PLAY : NotificationActionType("action_play")
    class CUSTOM(override val value: String) : NotificationActionType(value)

    companion object {
        fun valueOf(value: String): NotificationActionType {
            return CUSTOM(value)
        }
    }
}