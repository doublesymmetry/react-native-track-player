package com.doublesymmetry.kotlinaudio.models

import android.app.PendingIntent
import androidx.annotation.DrawableRes
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*

/**
 * Used to configure the player notification.
 * @param buttons The buttons that would appear on the notification.
 * @param pendingIntent The [PendingIntent] that would be called when tapping on the notification itself.
 */
data class NotificationConfig(val buttons: List<NotificationButton>, val pendingIntent: PendingIntent? = null)

sealed class NotificationButton(@DrawableRes val icon: Int?) {
    class PLAY(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class PAUSE(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class STOP(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class FORWARD(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
    class BACKWARD(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
    class NEXT(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
    class PREVIOUS(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)

    companion object {
        internal fun valueOf(value: String): NotificationButton {
            return when(value) {
                ACTION_PLAY -> PLAY()
                ACTION_PAUSE -> PAUSE()
                ACTION_STOP -> STOP()
                ACTION_FAST_FORWARD -> FORWARD()
                ACTION_REWIND -> BACKWARD()
                ACTION_NEXT -> NEXT()
                ACTION_PREVIOUS -> PREVIOUS()
                else -> error("No such button exists")
            }
        }
    }
}