package com.doublesymmetry.trackplayer.model

import android.content.Context
import android.os.Bundle

class NowPlayingMetadata(context: Context, bundle: Bundle?, ratingType: Int) : TrackMetadata() {
    var elapsedTime = 0.0
    override fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        super.setMetadata(context, bundle, ratingType)
        elapsedTime = bundle!!.getDouble("elapsedTime", 0.0)
    }

    init {
        setMetadata(context, bundle, ratingType)
    }
}