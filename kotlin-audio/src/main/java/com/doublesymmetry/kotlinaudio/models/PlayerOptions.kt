package com.doublesymmetry.kotlinaudio.models

interface PlayerOptions {
    var alwaysPauseOnInterruption: Boolean
}

internal class PlayerOptionsImpl(
    override var alwaysPauseOnInterruption: Boolean = false,
) : PlayerOptions
