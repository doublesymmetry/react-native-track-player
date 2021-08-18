package com.doublesymmetry.kotlinaudio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadata
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.imageLoader
import coil.request.ImageRequest
import com.doublesymmetry.kotlinaudio.models.AudioItem

//@InternalCoroutinesApi
//@SuppressLint("UnspecifiedImmutableFlag")
class PlaybackNotificationManager(private val context: Context, private val audioItem: AudioItem) {

//    private val context: Context by inject()
//    private val sessionManager: SessionManager by inject()
//    private val likedTracksManager: LikedTracksManager by inject()
//
//    private var socketTrack: SocketTrack? = null
    private val channelId: String
    private var artworkBitmap: Bitmap? = null
//    private var artworkRequestDisposable: Disposable? = null

    private val mediaSessionCompat = MediaSessionCompat(context, MEDIA_SESSION_TAG)

    init {
        // Remove seekbar
        val mediaMetadata = MediaMetadata.Builder().putLong(MediaMetadata.METADATA_KEY_DURATION, NO_DURATION).build()
        mediaSessionCompat.setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata))

        channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        } else {
            ""
        }
    }

    fun createNotification(): Notification {
        return initBaseNotificationBuilder()
            .setWhen(System.currentTimeMillis() - 50) // TODO: Set playtime
            .build()
    }

    fun refreshNotification(notification: Notification) {
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notification)
        }
    }

    private fun initBaseNotificationBuilder(): NotificationCompat.Builder {
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle().also {
            it.setShowActionsInCompactView(0)
            it.setMediaSession(mediaSessionCompat.sessionToken)
        }

        val boldTitle = SpannableString(audioItem.title ?: context.getString(R.string.unnamed_track))
        boldTitle.setSpan(StyleSpan(Typeface.BOLD), 0, boldTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.exo_icon_play) //TODO: Custom icon
            .setContentTitle(boldTitle)
            .setContentText(audioItem.artist)
            .setLargeIcon(getLargeIcon())
            .setStyle(mediaStyle)
            .setOngoing(true)
            .setUsesChronometer(true)
//            .addAction(createMuteUnmuteAction())
//            .addAction(createLikeUnlikeAction())
//            .setContentIntent(createContentIntent())
//            .setDeleteIntent(createDeleteIntent())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = CHANNEL_ID
        val channelName = context
            .getString(R.string.playback_channel_name)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        channel.description = "Used when playing music"
        channel.setSound(null, null)

        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    private fun getLargeIcon(): Bitmap? {
//        if (socketTrack == null) {
//            artworkBitmap = null
//        }

        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(audioItem.artwork)
            .target { artworkBitmap = (it as BitmapDrawable).bitmap }
            .build()

        imageLoader.enqueue(request)
        return artworkBitmap
    }

//    private fun createMuteUnmuteAction(): NotificationCompat.Action {
//        // TODO: Look into why this is true always
//        return if (true) {
//            val playIntent = PendingIntent.getBroadcast(context, 0, Intent(NOTIFICATION_PLAYBACK_ACTION_UNMUTE), PendingIntent.FLAG_UPDATE_CURRENT)
//            NotificationCompat.Action.Builder(R.drawable.ic_mute, context.getString(R.string.play), playIntent).build()
//        } else {
//            val pauseIntent = PendingIntent.getBroadcast(context, 0, Intent(NOTIFICATION_PLAYBACK_ACTION_MUTE), PendingIntent.FLAG_UPDATE_CURRENT)
//            NotificationCompat.Action.Builder(R.drawable.ic_unmute, context.getString(R.string.pause), pauseIntent).build()
//        }
//    }
//
//    private fun createLikeUnlikeAction(): NotificationCompat.Action {
//        val currentTrack = sessionManager.queue.value.firstOrNull()
//        val isTrackLiked = likedTracksManager.isTrackLiked(currentTrack)
//
//        return if (isTrackLiked) {
//            val pauseIntent = PendingIntent.getBroadcast(context, 0, Intent(NOTIFICATION_PLAYBACK_ACTION_UNLIKE), PendingIntent.FLAG_UPDATE_CURRENT)
//            NotificationCompat.Action.Builder(R.drawable.ic_liked_notification, context.getString(R.string.unlike), pauseIntent).build()
//        } else {
//            val playIntent = PendingIntent.getBroadcast(context, 0, Intent(NOTIFICATION_PLAYBACK_ACTION_LIKE), PendingIntent.FLAG_UPDATE_CURRENT)
//            NotificationCompat.Action.Builder(R.drawable.ic_unliked_notification, context.getString(R.string.like), playIntent).build()
//        }
//    }
//
//    private fun createDeleteIntent(): PendingIntent {
//        return PendingIntent.getBroadcast(context, 0, Intent(NOTIFICATION_PLAYBACK_ACTION_DISMISS), PendingIntent.FLAG_UPDATE_CURRENT)
//    }
//
//    private fun createContentIntent(): PendingIntent {
//        val intent = Intent(context, MainActivity::class.java)
//        sessionManager.session.value?.let {
//            intent.putExtra(MainActivity.SESSION_EXTRA, globalJson.encodeToString(it.toJsonObject()))
//        }
//
//        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//    }

    companion object {
        private const val MEDIA_SESSION_TAG = "one_beat_media_session"
        private const val NO_DURATION = -1L
        private const val CHANNEL_ID = "onebeat_playback"
        const val NOTIFICATION_ID = 1
    }
}
