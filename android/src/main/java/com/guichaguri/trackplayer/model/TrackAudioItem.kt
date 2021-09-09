package com.guichaguri.trackplayer.model

import com.doublesymmetry.kotlinaudio.models.AudioItem

data class TrackAudioItem(
    val track: Track,
    override var audioUrl: String,
    override var artist: String? = null,
    override var title: String? = null,
    override var albumTitle: String? = null,
    override val artwork: String? = null
): AudioItem