package com.doublesymmetry.kotlinaudio.models

interface PlayerOptions {
    val alwaysPauseOnInterruption: Boolean
}

data class PlayerOptionsImpl(
    override val alwaysPauseOnInterruption: Boolean = false,
) : PlayerOptions
