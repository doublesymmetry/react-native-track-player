package com.doublesymmetry.kotlinaudio.models

public enum class SourceType {
    STREAM, FILE
}

interface AudioItem {
    var audioUrl: String
    var artist: String?
    var title: String?
    var albumTitle: String?
    val sourceType: SourceType
    val artwork: String?
}

data class DefaultAudioItem(
    override var audioUrl: String,
    override val sourceType: SourceType,
    override var artist: String? = null,
    override var title: String? = null,
    override var albumTitle: String? = null,
    override val artwork: String? = null
) : AudioItem