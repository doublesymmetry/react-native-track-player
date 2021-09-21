package com.doublesymmetry.kotlinaudio.models

import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer

class NotificationOptions(private val exoPlayer: ExoPlayer) {
    var actions: List<NotificationAction?> = emptyList()
        set(value) {
            exoPlayer
            field = value
        }
}

data class NotificationAction(
    val action: NotificationCompat.Action,
    val isCompact: Boolean,
)

//data class Actions(
//    val previous: NotificationCompat.Action,
////    val next: NotificationCompat.Action,
////    val rewind: NotificationCompat.Action,
////    val play: NotificationCompat.Action,
////    val pause: NotificationCompat.Action,
////    val stop: NotificationCompat.Action,
////    val forward: NotificationCompat.Action,
//) {
//    fun setPrevious() {
//
//    }
//}
//
//data class NotificationAction(
//    val title: String,
//    val icon: Drawable,
//)
