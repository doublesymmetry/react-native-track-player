package com.doublesymmetry.kotlinaudio.players.components

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.DefaultDashChunkSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.extractor.DefaultExtractorsFactory
import com.doublesymmetry.kotlinaudio.event.PlayerEventHolder
import com.doublesymmetry.kotlinaudio.utils.isUriLocalFile
import com.doublesymmetry.kotlinaudio.models.CustomSchemeResponse
import okio.IOException
import java.util.UUID


@OptIn(UnstableApi::class)
class MediaFactory (
    private val context: Context,
    private val cache: SimpleCache?,
    private val playerEventHolder: PlayerEventHolder,
    private val customSchemeResponses: MutableList<CustomSchemeResponse>
) : MediaSource.Factory {

    companion object {
        private const val DEFAULT_USER_AGENT = "react-native-track-player"
    }

    var customUrlPrefix: String? = null

    private val mediaFactory = DefaultMediaSourceFactory(context)

    override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
        return mediaFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        return mediaFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
    }

    override fun getSupportedTypes(): IntArray {
        return mediaFactory.supportedTypes
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {

        val userAgent = mediaItem.mediaMetadata.extras?.getString("user-agent") ?: DEFAULT_USER_AGENT
        val headers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaItem.mediaMetadata.extras?.getSerializable("headers", HashMap::class.java)
        } else {
            mediaItem.mediaMetadata.extras?.getSerializable("headers")
        }
        val resourceId = mediaItem.mediaMetadata.extras?.getInt("resource-id")
        // HACK: why are these capitalized?
        val resourceType = mediaItem.mediaMetadata.extras?.getString("type")?.lowercase()
        val uri = Uri.parse(mediaItem.mediaMetadata.extras?.getString("uri")!!)
        val customPrefix = customUrlPrefix
        val factory: DataSource.Factory = when {
            resourceId != 0 && resourceId != null -> {
                val raw = RawResourceDataSource(context)
                raw.open(DataSpec(uri))
                DataSource.Factory { raw }
            }
            ((customPrefix == null) || !uri.toString().startsWith(customPrefix)) && isUriLocalFile(uri) -> {
                DefaultDataSource.Factory(context)
            }
            else -> {
                val tempFactory = DefaultHttpDataSource.Factory().apply {
                    setUserAgent(userAgent)
                    setAllowCrossProtocolRedirects(true)

                    headers?.let {
                        setDefaultRequestProperties(it as HashMap<String, String>)
                    }
                }

                val httpFactory = enableCaching(tempFactory)

                // If the uri matches our current custom scheme prefix we'll use ResolvingDataSource so that we can override
                // each new connection to the file. Otherwise we'll just use DefaultHttpDataSource directly.
                val returnFactory = if ((customPrefix == null) || !uri.toString().startsWith(customPrefix)) httpFactory else {
                    val resolver = ResolvingDataSource.Resolver { dataSpec ->
                        val reqId = UUID.randomUUID().toString()
                        // Emit event about the new request
                        playerEventHolder.updateCustomSchemeRequest(reqId, dataSpec.uri.toString())

                        var loopCount = 0
                        var response: CustomSchemeResponse? = null

                        // We'll time out and throw an exception after (waitMs * waitIterations) milliseconds.
                        val waitMs: Long = 100
                        val waitIterations = 600

                        while(response == null && loopCount < waitIterations){
                            Thread.sleep(waitMs)
                            try {
                                response = customSchemeResponses.first{ it.id == reqId }
                                customSchemeResponses.remove(response)
                            } catch (e: NoSuchElementException) {
                                loopCount++
                            }
                        }
                        if(response == null) throw IOException("Custom scheme request timed out while waiting for response")

                        val headers = if (response.headerprops != null) response.headerprops!! else mapOf<String,String>()
                        val url = if (response.newUri != null) response.newUri!! else dataSpec.uri.toString()
                        dataSpec.withAdditionalHeaders((headers)).withUri(Uri.parse(url))
                    }
                    ResolvingDataSource.Factory(httpFactory, resolver)
                }

                returnFactory
            }
        }

        return when (resourceType) {
            "dash" -> createDashSource(mediaItem, factory)
            "hls" -> createHlsSource(mediaItem, factory)
            "smoothstreaming" -> createSsSource(mediaItem, factory)
            else -> createProgressiveSource(mediaItem, factory)
        }
    }

    private fun createDashSource(mediaItem: MediaItem, factory: DataSource.Factory?): MediaSource {
        return DashMediaSource.Factory(DefaultDashChunkSource.Factory(factory!!), factory)
            .createMediaSource(mediaItem)
    }

    private fun createHlsSource(mediaItem: MediaItem, factory: DataSource.Factory?): MediaSource {
        return HlsMediaSource.Factory(factory!!)
            .createMediaSource(mediaItem)
    }

    private fun createSsSource(mediaItem: MediaItem, factory: DataSource.Factory?): MediaSource {
        return SsMediaSource.Factory(DefaultSsChunkSource.Factory(factory!!), factory)
            .createMediaSource(mediaItem)
    }

    private fun createProgressiveSource(
        mediaItem: MediaItem,
        factory: DataSource.Factory
    ): ProgressiveMediaSource {
        return ProgressiveMediaSource.Factory(
            factory, DefaultExtractorsFactory()
                .setConstantBitrateSeekingEnabled(true)
        )
            .createMediaSource(mediaItem)
    }

    private fun enableCaching(factory: DataSource.Factory): DataSource.Factory {
        if (cache == null) {
            return factory
        } else {
            return CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(factory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }
}
