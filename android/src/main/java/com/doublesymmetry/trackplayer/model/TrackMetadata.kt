package com.doublesymmetry.trackplayer.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.RatingCompat
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toMilliseconds
import com.doublesymmetry.trackplayer.utils.Utils

abstract class TrackMetadata {
    var artwork: Uri? = null
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var date: String? = null
    var genre: String? = null
    var duration: Long = 0
    var rating: RatingCompat? = null
    open fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        artwork = Utils.getUri(context, bundle, "artwork")
        title = bundle!!.getString("title")
        artist = bundle.getString("artist")
        album = bundle.getString("album")
        date = bundle.getString("date")
        genre = bundle.getString("genre")
        duration = (bundle.getDouble("duration", 0.0)).toMilliseconds()
        rating = Utils.getRating(bundle, "rating", ratingType)
    }
}