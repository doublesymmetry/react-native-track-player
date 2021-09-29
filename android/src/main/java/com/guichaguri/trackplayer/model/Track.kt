package com.guichaguri.trackplayer.model

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.doublesymmetry.kotlinaudio.models.MediaType
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.guichaguri.trackplayer.service_old.Utils
import java.util.*

/**
 * @author Guichaguri
 */
class Track(context: Context, bundle: Bundle, ratingType: Int) : TrackMetadata() {
    var uri: Uri? = null
    var resourceId: Int
    var type = TrackType.DEFAULT
    var contentType: String?
    var userAgent: String?
    var originalItem: Bundle?
    var headers: MutableMap<String, String>? = null
    val queueId: Long

    override fun setMetadata(context: Context, bundle: Bundle?, ratingType: Int) {
        super.setMetadata(context, bundle, ratingType)
        if (originalItem != null && originalItem != bundle) originalItem!!.putAll(bundle)
    }

    override fun toMediaMetadata(): MediaMetadataCompat.Builder {
        val builder = super.toMediaMetadata()
        builder!!.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri.toString())
        return builder
    }

    fun toQueueItem(): MediaSessionCompat.QueueItem {
        val descr = MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setSubtitle(artist)
            .setMediaUri(uri)
            .setIconUri(artwork)
            .build()
        return MediaSessionCompat.QueueItem(descr, queueId)
    }

//    fun toMediaSource(ctx: Context?, playback: LocalPlayback): MediaSource {
//        // Updates the user agent if not set
//        if (userAgent == null || userAgent!!.isEmpty()) userAgent = Util.getUserAgent(
//            ctx!!, "react-native-track-player"
//        )
//        val ds: DataSource.Factory?
//        ds = if (resourceId != 0) {
//            try {
//                val raw = RawResourceDataSource(ctx!!)
//                raw.open(DataSpec(uri!!))
//                DataSource.Factory { raw }
//            } catch (ex: IOException) {
//                // Should never happen
//                throw RuntimeException(ex)
//            }
//        } else if (Utils.isLocal(uri)) {
//
//            // Creates a local source factory
//            DefaultDataSourceFactory(ctx!!, userAgent)
//        } else {
//
//            // Creates a default http source factory, enabling cross protocol redirects
//            val factory = DefaultHttpDataSourceFactory(
//                userAgent, null,
//                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
//                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
//                true
//            )
//            if (headers != null) {
//                factory.defaultRequestProperties.set(headers!!.toMap())
//            }
//            playback.enableCaching(factory)
//        }
//        return when (type) {
//            TrackType.DASH -> createDashSource(ds)
//            TrackType.HLS -> createHlsSource(ds)
//            TrackType.SMOOTH_STREAMING -> createSsSource(ds)
//            else -> ProgressiveMediaSource.Factory(
//                ds, DefaultExtractorsFactory()
//                    .setConstantBitrateSeekingEnabled(true)
//            )
//                .createMediaSource(uri!!)
//        }
//    }

//    private fun createDashSource(factory: DataSource.Factory?): MediaSource {
//        return DashMediaSource.Factory(DefaultDashChunkSource.Factory(factory!!), factory)
//            .createMediaSource(uri!!)
//    }
//
//    private fun createHlsSource(factory: DataSource.Factory?): MediaSource {
//        return HlsMediaSource.Factory(factory!!)
//            .createMediaSource(uri!!)
//    }
//
//    private fun createSsSource(factory: DataSource.Factory?): MediaSource {
//        return SsMediaSource.Factory(DefaultSsChunkSource.Factory(factory!!), factory)
//            .createMediaSource(uri!!)
//    }

    fun toAudioItem(): TrackAudioItem {
        return TrackAudioItem(
            this,
            MediaType.DEFAULT,
            uri.toString(),
            artist,
            title,
            album,
            artwork.toString()
        )
    }

    companion object {
        fun createTracks(context: Context, objects: List<*>?, ratingType: Int): List<Track>? {
            val tracks: MutableList<Track> = ArrayList()
            for (o in objects!!) {
                if (o is Bundle) {
                    tracks.add(Track(context, o, ratingType))
                } else {
                    return null
                }
            }
            return tracks
        }
    }

    init {
        resourceId = Utils.getRawResourceId(context, bundle, "url")
        uri = if (resourceId == 0) {
            Utils.getUri(context, bundle, "url")
        } else {
            RawResourceDataSource.buildRawResourceUri(resourceId)
        }
        val trackType = bundle.getString("type", "default")
        for (t in TrackType.values()) {
            if (t.name.equals(trackType, ignoreCase = true)) {
                type = t
                break
            }
        }
        contentType = bundle.getString("contentType")
        userAgent = bundle.getString("userAgent")
        val httpHeaders = bundle.getBundle("headers")
        if (httpHeaders != null) {
            headers = HashMap()
            for (header in httpHeaders.keySet()) {
                headers!![header] = httpHeaders.getString(header)!!
            }
        }
        setMetadata(context, bundle, ratingType)
        queueId = System.currentTimeMillis()
        originalItem = bundle
    }
}