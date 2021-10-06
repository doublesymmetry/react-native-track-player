package com.doublesymmetry.trackplayer.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
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
        duration = Utils.toMillis(bundle.getDouble("duration", 0.0))
        rating = Utils.getRating(bundle, "rating", ratingType)
    }

    open fun toMediaMetadata(): MediaMetadataCompat.Builder? {
        val builder = MediaMetadataCompat.Builder()
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
        builder.putString(MediaMetadataCompat.METADATA_KEY_DATE, date)
        builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)

        if (duration > 0) {
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }

        if (artwork != null) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artwork.toString())
        }

        if (rating != null) {
            builder.putRating(MediaMetadataCompat.METADATA_KEY_RATING, rating)
        }

        return builder
    }
}