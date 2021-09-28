package com.guichaguri.trackplayer.model

import com.doublesymmetry.kotlinaudio.models.AudioPlayerState

val AudioPlayerState.asLibState: State
    get() {
        return when(this) {
            AudioPlayerState.READY -> State.Ready
            AudioPlayerState.BUFFERING -> State.Buffering
            AudioPlayerState.PAUSED -> State.Paused
            AudioPlayerState.PLAYING -> State.Playing
            AudioPlayerState.IDLE -> State.None
            AudioPlayerState.ENDED -> State.Stopped
        }
    }
