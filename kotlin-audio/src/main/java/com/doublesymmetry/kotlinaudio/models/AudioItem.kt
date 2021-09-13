package com.doublesymmetry.kotlinaudio.models

interface AudioItem {
    var audioUrl: String
    var artist: String?
    var title: String?
    var albumTitle: String?
    val artwork: String?
}

data class DefaultAudioItem(
    override var audioUrl: String,
    override var artist: String? = null,
    override var title: String? = null,
    override var albumTitle: String? = null,
    override val artwork: String? = null
) : AudioItem