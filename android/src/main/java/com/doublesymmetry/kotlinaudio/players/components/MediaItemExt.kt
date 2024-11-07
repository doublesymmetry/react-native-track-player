package com.doublesymmetry.kotlinaudio.players.components

import com.doublesymmetry.kotlinaudio.models.AudioItemHolder
import com.google.android.exoplayer2.MediaItem

fun MediaItem.getAudioItemHolder(): AudioItemHolder {
    return localConfiguration!!.tag as AudioItemHolder
}
