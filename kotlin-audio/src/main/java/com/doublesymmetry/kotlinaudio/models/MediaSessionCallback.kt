package com.doublesymmetry.kotlinaudio.models

import android.os.Bundle
import android.support.v4.media.RatingCompat


sealed class MediaSessionCallback {
    class RATING(val rating: RatingCompat, val extras: Bundle?): MediaSessionCallback()
}
