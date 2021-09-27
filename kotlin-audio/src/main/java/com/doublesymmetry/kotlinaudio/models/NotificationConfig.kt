package com.doublesymmetry.kotlinaudio.models

import androidx.annotation.DrawableRes
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*

data class NotificationConfig(val buttons: List<NotificationButton>)

sealed class NotificationButton(@DrawableRes val icon: Int?) {
    class PLAY(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class PAUSE(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class STOP(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class FORWARD(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
    class REWIND(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
    class NEXT(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
    class PREVIOUS(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)

    enum class Action {
        PLAY, PAUSE, STOP, FORWARD, REWIND, NEXT, PREVIOUS
    }

    companion object {
        internal fun valueOf(value: String): Action {
            return when(value) {
                ACTION_PLAY -> Action.PLAY
                ACTION_PAUSE -> Action.PAUSE
                ACTION_STOP -> Action.STOP
                ACTION_FAST_FORWARD -> Action.FORWARD
                ACTION_REWIND -> Action.REWIND
                ACTION_NEXT -> Action.NEXT
                ACTION_PREVIOUS -> Action.PREVIOUS
                else -> error("No such action exists")
            }
        }
    }
}