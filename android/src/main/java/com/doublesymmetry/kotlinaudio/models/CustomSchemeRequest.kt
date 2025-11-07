package com.doublesymmetry.kotlinaudio.models

data class CustomSchemeRequest (
    val id: String,
    val uri: String
)

data class CustomSchemeResponse (
    val id: String,
    val newUri: String?,
    val headerprops: MutableMap<String, String>?
)
