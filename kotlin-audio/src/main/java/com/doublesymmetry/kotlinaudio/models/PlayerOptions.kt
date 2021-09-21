package com.doublesymmetry.kotlinaudio.models

interface PlayerOptions {
    var alwaysPauseOnInterruption: Boolean
}

class PlayerOptionsImpl(
    override var alwaysPauseOnInterruption: Boolean = false,
) : PlayerOptions
