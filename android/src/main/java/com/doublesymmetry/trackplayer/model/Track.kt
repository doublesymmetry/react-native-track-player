package com.doublesymmetry.trackplayer.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.doublesymmetry.kotlinaudio.models.AudioItemOptions
import com.doublesymmetry.kotlinaudio.models.MediaType
import com.doublesymmetry.trackplayer.utils.BundleUtils
import com.google.android.exoplayer2.upstream.RawResourceDataSource

/**
 * @author Milen Pivchev @mpivchev
 */
class Track(context: Context, bundle: Bundle, ratingType: Int) : TrackMetadata() {
    var uri: Uri? = null
    var resourceId: Int?
    var type = MediaType.DEFAULT
    var contentType: String?
    var userAgent: String?
    var originalItem: Bundle?
    var headers: MutableMap<String, String>? = null
    val queueId: Long

    override fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        super.setMetadata(context, bundle, ratingType)
        if (originalItem != null && originalItem != bundle) originalItem!!.putAll(bundle)
    }

    fun toAudioItem(): TrackAudioItem {
        return TrackAudioItem(this, type, uri.toString(), artist, title, album, artwork.toString(), duration,
                AudioItemOptions(headers, userAgent, resourceId))
    }

    init {
        resourceId = BundleUtils.getRawResourceId(context, bundle, "url")
        uri = if (resourceId == 0) {
            resourceId = null
            BundleUtils.getUri(context, bundle, "url")
        } else {
            RawResourceDataSource.buildRawResourceUri(resourceId!!)
        }
        val trackType = bundle.getString("type", "default")
        for (t in MediaType.values()) {
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
        originalItem = bundle
    }
}