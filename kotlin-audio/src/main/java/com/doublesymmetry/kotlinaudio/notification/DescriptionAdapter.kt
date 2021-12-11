package com.doublesymmetry.kotlinaudio.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

interface NotificationMetadataProvider {
    fun getTitle(): String?
    fun getArtist(): String?
    fun getArtworkUrl(): String?
}

/**
 * Provides content assets of the media currently playing. If certain data is missing from [AudioItem], data from the media file's metadata is used instead.
 * @param context Some Android [Context].
 * @param pendingIntent The [PendingIntent] that should be fired when the notification is tapped.
 */
class DescriptionAdapter(private val metadataProvider: NotificationMetadataProvider, private val context: Context, private val pendingIntent: PendingIntent?): PlayerNotificationManager.MediaDescriptionAdapter {
    private var disposable: Disposable? = null

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return metadataProvider.getTitle() ?: player.mediaMetadata.displayTitle ?: ""
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return pendingIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return metadataProvider.getArtist() ?: player.mediaMetadata.artist ?: player.mediaMetadata.albumArtist
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback,
    ): Bitmap? {
        var artworkBitmap: Bitmap? = null

        val placeholderImage = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        placeholderImage.eraseColor(Color.DKGRAY)

        disposable?.dispose()

        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(getArtworkSource(metadataProvider.getArtworkUrl(), player.mediaMetadata))
            .target {
                artworkBitmap = (it as BitmapDrawable).bitmap
                callback.onBitmap(it.bitmap)
            }
            .build()

        disposable = imageLoader.enqueue(request)

        return artworkBitmap ?: placeholderImage
    }

    private fun getArtworkSource(artworkUrl: String?, mediaMetadata: MediaMetadata): Any? {
        val data: ByteArray? = mediaMetadata.artworkData

        return when {
            artworkUrl != null -> artworkUrl
            mediaMetadata.artworkUri != null -> mediaMetadata.artworkUri
            data != null -> BitmapFactory.decodeByteArray(data, 0, data.size)
            else -> null
        }
    }

    fun release() {
        disposable?.dispose()
    }
}