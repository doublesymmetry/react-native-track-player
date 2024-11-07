package com.doublesymmetry.kotlinaudio.models

import android.graphics.Bitmap

interface AudioItem {
    var audioUrl: String
    val type: MediaType
    var artist: String?
    var title: String?
    var albumTitle: String?
    val artwork: String?
    val duration: Long?
    val options: AudioItemOptions?
}

data class AudioItemOptions(
    val headers: MutableMap<String, String>? = null,
    val userAgent: String? = null,
    val resourceId: Int? = null
)

enum class MediaType(val value: String) {
    /**
     * The default media type. Should be used for streams over HTTP or files
     */
    DEFAULT("default"),

    /**
     * The DASH media type for adaptive streams. Should be used with DASH manifests.
     */
    DASH("dash"),

    /**
     * The HLS media type for adaptive streams. Should be used with HLS playlists.
     */
    HLS("hls"),

    /**
     * The SmoothStreaming media type for adaptive streams. Should be used with SmoothStreaming manifests.
     */
    SMOOTH_STREAMING("smoothstreaming");
}

data class DefaultAudioItem(
    override var audioUrl: String,

    /**
     * Set to [MediaType.DEFAULT] by default.
     */
    override val type: MediaType = MediaType.DEFAULT,

    override var artist: String? = null,
    override var title: String? = null,
    override var albumTitle: String? = null,
    override var artwork: String? = null,
    override val duration: Long? = null,
    override val options: AudioItemOptions? = null,
) : AudioItem

class AudioItemHolder(
    var audioItem: AudioItem
) {
    var artworkBitmap: Bitmap? = null
}
