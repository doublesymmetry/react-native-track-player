package com.doublesymmetry.kotlinaudio.models

interface PlayerOptions {
    var alwaysPauseOnInterruption: Boolean
}

data class PlayerOptionsImpl(
    override var alwaysPauseOnInterruption: Boolean = false,
) : PlayerOptions
