package com.doublesymmetry.kotlinaudio.models

data class BufferOptions(
    val minBuffer: Int?,
    val maxBuffer: Int?,
    val playBuffer: Int?,
    val backBuffer: Int?
)
