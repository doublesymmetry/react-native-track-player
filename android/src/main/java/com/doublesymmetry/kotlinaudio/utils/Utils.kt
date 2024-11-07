package com.doublesymmetry.kotlinaudio.utils

import android.content.ContentResolver
import android.net.Uri
import com.google.android.exoplayer2.upstream.RawResourceDataSource

fun isUriLocalFile(uri: Uri?): Boolean {
    if (uri == null) return false
    val scheme = uri.scheme
    val host = uri.host
    if((scheme == "http" || scheme == "https") && (host == "localhost" || host == "127.0.0.1" || host == "[::1]"))
    {
        return false
    }
    return scheme == null || scheme == ContentResolver.SCHEME_FILE || scheme == ContentResolver.SCHEME_ANDROID_RESOURCE || scheme == ContentResolver.SCHEME_CONTENT || scheme == RawResourceDataSource.RAW_RESOURCE_SCHEME || scheme == "res" || host == null
}