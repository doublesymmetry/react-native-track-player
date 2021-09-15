package com.doublesymmetry.kotlinaudio.models

data class NotificationOptions(
    val minBuffer: Int?,
    val maxBuffer: Int?,
    val playBuffer: Int?,
    val backBuffer: Int?
)
