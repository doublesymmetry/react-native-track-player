package com.doublesymmetry.kotlinaudio.players

import com.doublesymmetry.kotlinaudio.models.AudioPlayerState
import com.doublesymmetry.kotlinaudio.models.AudioPlayerState.IDLE
import com.doublesymmetry.kotlinaudio.models.PlaybackEndedReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventHolder {
    private var _stateChange = MutableStateFlow<AudioPlayerState?>(IDLE)
    var stateChange = _stateChange.asStateFlow()

    private var _playbackEnd = MutableStateFlow<PlaybackEndedReason?>(null)
    var playbackEnd = _playbackEnd.asStateFlow()

    internal fun updateAudioPlayerState(state: AudioPlayerState) {
        _stateChange.value = state
    }

    internal fun updatePlaybackEndedReason(reason: PlaybackEndedReason) {
        _playbackEnd.value = reason
    }
}