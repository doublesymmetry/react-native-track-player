package com.doublesymmetry.kotlinaudio.models

import android.app.PendingIntent
import androidx.annotation.DrawableRes
import com.google.android.exoplayer2.ui.PlayerNotificationManager.*

/**
 * Used to configure the player notification.
 * @param buttons Provide customized notification buttons. They will be shown by default. Note that buttons can still be shown and hidden at runtime by using the functions in [NotificationManager][com.doublesymmetry.kotlinaudio.notification.NotificationManager], but they will have the default icon if not set explicitly here.
 * @param accentColor The accent color of the notification.
 * @param smallIcon The small icon of the notification which is also shown in the system status bar.
 * @param pendingIntent The [PendingIntent] that would be called when tapping on the notification itself.
 */
data class NotificationConfig(
    val buttons: List<NotificationButton>,
    val accentColor: Int? = null,
    @DrawableRes val smallIcon: Int? = null,
    val pendingIntent: PendingIntent? = null
)

/**
 * Provide customized notification buttons. They will be shown by default. Note that buttons can still be shown and hidden at runtime by using the functions in [NotificationManager][com.doublesymmetry.kotlinaudio.notification.NotificationManager], but they will have the default icon if not set explicitly here.
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showPlayPauseButton]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showStopButton]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showStopButtonCompact]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showBackwardButton]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showBackwardButtonCompact]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showForwardButton]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showForwardButtonCompact]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showNextButton]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showNextButtonCompact]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showPreviousButton]
 * @see [com.doublesymmetry.kotlinaudio.notification.NotificationManager.showPreviousButtonCompact]
 */
sealed class NotificationButton(@DrawableRes val icon: Int?) {
    class PLAY(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class PAUSE(@DrawableRes icon: Int? = null): NotificationButton(icon)
    class STOP(@DrawableRes icon: Int? = null, val isCompact: Boolean = false): NotificationButton(icon)
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