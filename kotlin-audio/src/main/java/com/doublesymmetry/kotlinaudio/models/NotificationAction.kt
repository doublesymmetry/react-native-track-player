package com.doublesymmetry.kotlinaudio.models

import androidx.annotation.DrawableRes

data class NotificationAction(
    val title: String,
    @DrawableRes val icon: Int,
//    val action: NotificationCompat.Action,
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