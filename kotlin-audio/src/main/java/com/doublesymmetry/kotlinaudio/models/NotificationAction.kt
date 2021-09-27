//package com.doublesymmetry.kotlinaudio.models
//
//import androidx.annotation.DrawableRes
//import com.doublesymmetry.kotlinaudio.players.AudioPlayer
//
//data class NotificationAction(
//    val title: String,
//    @DrawableRes val icon: Int,
////    val action: NotificationCompat.Action,
//    val type: NotificationActionType,
//    val isCompact: Boolean,
//) {
//    companion object {
//        fun createPlayPauseAction(player: AudioPlayer): NotificationAction {
//            return if (player.isPlaying) {
//                NotificationAction("Pause", android.R.drawable.ic_media_pause, NotificationActionType.ACTION_PAUSE, true)
//            } else {
//                NotificationAction("Play", android.R.drawable.ic_media_play, NotificationActionType.ACTION_PLAY, true)
//            }
//        }
//    }
//}
//
//sealed class NotificationActionType(open val value: String) {
//    object ACTION_STOP : NotificationActionType("action_stop")
//    object ACTION_PAUSE : NotificationActionType("action_pause")
//    object ACTION_PLAY : NotificationActionType("action_play")
//    class CUSTOM(override val value: String) : NotificationActionType(value)
//
//    companion object {
//        fun valueOf(value: String): NotificationActionType {
//            return when (value) {
//                ACTION_PLAY.value -> ACTION_PLAY
//                ACTION_PAUSE.value -> ACTION_PAUSE
//                ACTION_STOP.value -> ACTION_STOP
//                else -> CUSTOM(value)
//            }
////            if (value == ACTION_PLAY.value) return ACTION_PLAY
////            if (value == ACTION_PAUSE.value) return ACTION_PAUSE
////            if (value == A)
////            return CUSTOM(value)
//        }
//    }
//}