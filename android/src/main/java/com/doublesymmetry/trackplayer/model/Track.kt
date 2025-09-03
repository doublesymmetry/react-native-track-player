package com.doublesymmetry.trackplayer.model

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.doublesymmetry.kotlinaudio.models.AudioItemOptions
import com.doublesymmetry.kotlinaudio.models.MediaType
import com.doublesymmetry.trackplayer.utils.BundleUtils
import androidx.media3.datasource.RawResourceDataSource

/**
 * @author Milen Pivchev @mpivchev
 */
@OptIn(UnstableApi::class)
class Track
    (context: Context, bundle: Bundle, ratingType: Int) : TrackMetadata() {
    var uri: Uri? = null
    var resourceId: Int?
    var type = MediaType.DEFAULT
    var contentType: String?
    var userAgent: String?
    var originalItem: Bundle
    var headers: HashMap<String, String>? = null
    val queueId: Long

    override fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        super.setMetadata(context, bundle, ratingType)
        // Only update originalItem with fields that were actually provided
        bundle?.keySet()?.forEach { key ->
            originalItem.putAll(Bundle().apply {
                putValue(key, bundle.get(key))
            })
        }
    }

    private fun Bundle.putValue(key: String, value: Any?) {
        when (value) {
            null -> remove(key)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Double -> putDouble(key, value)
            is Boolean -> putBoolean(key, value)
            is Bundle -> putBundle(key, value)
            // Add other types as needed
        }
    }

    fun toAudioItem(): TrackAudioItem {
        return TrackAudioItem(
            track = this,
            type = type,
            audioUrl = uri.toString(),
            artist = artist,
            title = title,
            albumTitle = album,
            artwork = artwork.toString(),
            duration = duration,
            options = AudioItemOptions(headers, userAgent, resourceId),
            mediaId = mediaId
        )
    }

    init {
        originalItem = bundle
        resourceId = BundleUtils.getRawResourceId(context, bundle, "url")
        uri = if (resourceId == 0) {
            resourceId = null
            BundleUtils.getUri(context, bundle, "url")
        } else {
            // RawResourceDataSource.buildRawResourceUri(resourceId!!)
            Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).path(Integer. toString(
                resourceId!!
            )).build()
        }
        val trackType = bundle.getString("type", "default")
        for (t in MediaType.entries) {
            if (t.name.equals(trackType, ignoreCase = true)) {
                type = t
                break
            }
        }
        contentType = bundle.getString("contentType")
        userAgent = bundle.getString("userAgent")
        val httpHeaders = bundle.getBundle("headers")
        if (httpHeaders != null) {
            headers = HashMap()
            for (header in httpHeaders.keySet()) {
                headers!![header] = httpHeaders.getString(header)!!
            }
        }
        setMetadata(context, bundle, ratingType)
        queueId = System.currentTimeMillis()
    }
}
