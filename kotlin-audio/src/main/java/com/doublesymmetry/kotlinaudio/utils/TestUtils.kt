package com.doublesymmetry.kotlinaudio.utils

fun isJUnitTest(): Boolean {
    for (element in Thread.currentThread().stackTrace) {
        if (element.className.startsWith("org.junit.")) {
            return true
        }
    }
    return false
}