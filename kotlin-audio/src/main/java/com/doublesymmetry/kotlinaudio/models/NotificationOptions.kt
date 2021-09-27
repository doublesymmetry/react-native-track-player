package com.doublesymmetry.kotlinaudio.models

import androidx.annotation.DrawableRes
import com.doublesymmetry.kotlinaudio.notification.NotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*

class NotificationOptions internal constructor(private val notificationManager: NotificationManager) {
    var buttons: List<NotificationButton?> = emptyList()
        set(value) {
            field = value
            notificationManager.apply {
                buttons.clear()
                buttons.addAll(field)

                refresh()
            }
        }
}

sealed class NotificationButton(@DrawableRes drawable: Int?, isCompact: Boolean) {
    class PLAY(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)
    class PAUSE(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)
    class STOP(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)
    class FORWARD(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)
    class REWIND(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)
    class NEXT(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)
    class PREVIOUS(@DrawableRes drawable: Int? = null, isCompact: Boolean = false): NotificationButton(drawable, isCompact)

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