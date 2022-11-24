package com.doublesymmetry.trackplayer.extensions

class NumberExt {
    companion object {
        fun Number.toSeconds(): Double {
            return this.toDouble() / 1000
        }

        fun Number.toMilliseconds(): Long {
            return (this.toDouble() * 1000).toLong()
        }
    }
}