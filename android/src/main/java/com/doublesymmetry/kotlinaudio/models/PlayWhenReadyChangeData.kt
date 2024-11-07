package com.doublesymmetry.kotlinaudio.models

import com.google.android.exoplayer2.Player

data class PlayWhenReadyChangeData(val playWhenReady: Boolean, val pausedBecauseReachedEnd: Boolean)
