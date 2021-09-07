package com.guichaguri.trackplayer.models

import com.doublesymmetry.kotlinaudio.models.AudioItem
import com.doublesymmetry.kotlinaudio.models.SourceType

data class TrackAudioItem(
    val track: Track,
    override var audioUrl: String,
    override val sourceType: SourceType,
    override var artist: String? = null,
    override var title: String? = null,
    override var albumTitle: String? = null,
    override val artwork: String? = null
): AudioItem