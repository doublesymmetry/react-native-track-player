package com.doublesymmetry.trackplayer.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.Rating
import com.doublesymmetry.trackplayer.extensions.NumberExt.Companion.toMilliseconds
import com.doublesymmetry.trackplayer.utils.BundleUtils

abstract class TrackMetadata {
    var artwork: Uri? = null
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var date: String? = null
    var genre: String? = null
    var duration: Long? = null
    var rating: Rating? = null
    var mediaId: String? = null

    open fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        // Special handling for artwork:
        // - If undefined/null in bundle - keep existing artwork
        // - If empty string - explicitly clear artwork
        // - If valid URI - update artwork
        if (bundle?.containsKey("artwork") == true) {
            val artworkStr = bundle.getString("artwork")
            artwork = if (artworkStr?.isEmpty() == true) {
                null // Clear artwork for empty string
            } else {
                BundleUtils.getUri(context, bundle, "artwork") ?: artwork // Keep existing if new is invalid
            }
        }

        // For all other fields, only update if provided in bundle
        bundle?.getString("title")?.let { title = it }
        bundle?.getString("artist")?.let { artist = it }
        bundle?.getString("album")?.let { album = it }
        bundle?.getString("date")?.let { date = it }
        bundle?.getString("genre")?.let { genre = it }
        bundle?.getString("mediaId")?.let { mediaId = it }

        // Update duration if provided
        if (bundle?.containsKey("duration") == true) {
            duration = bundle.getDouble("duration").toMilliseconds()
        }

        // Update rating if provided
        if (bundle?.containsKey("rating") == true) {
            rating = BundleUtils.getRating(bundle, "rating", ratingType)
        }
    }
}
