package com.guichaguri.trackplayer.service_old.player

import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.flac.VorbisComment
import com.google.android.exoplayer2.metadata.icy.IcyHeaders
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame
import com.google.android.exoplayer2.metadata.mp4.MdtaMetadataEntry
import com.guichaguri.trackplayer.service_old.MusicManager
import java.nio.charset.Charset

object SourceMetadata {
    /**
     * Reads metadata and triggers the metadata-received event
     */
    fun handleMetadata(manager: MusicManager, metadata: Metadata) {
        handleId3Metadata(manager, metadata)
        handleIcyMetadata(manager, metadata)
        handleVorbisCommentMetadata(manager, metadata)
        handleQuickTimeMetadata(manager, metadata)
    }

    /**
     * ID3 Metadata (MP3)
     *
     * https://en.wikipedia.org/wiki/ID3
     */
    private fun handleId3Metadata(manager: MusicManager, metadata: Metadata) {
        var title: String? = null
        var url: String? = null
        var artist: String? = null
        var album: String? = null
        var date: String? = null
        var genre: String? = null
        for (i in 0 until metadata.length()) {
            val entry = metadata[i]
            if (entry is TextInformationFrame) {
                // ID3 text tag
                val id3 = entry
                val id = id3.id.uppercase()
                if (id == "TIT2" || id == "TT2") {
                    title = id3.value
                } else if (id == "TALB" || id == "TOAL" || id == "TAL") {
                    album = id3.value
                } else if (id == "TOPE" || id == "TPE1" || id == "TP1") {
                    artist = id3.value
                } else if (id == "TDRC" || id == "TOR") {
                    date = id3.value
                } else if (id == "TCON" || id == "TCO") {
                    genre = id3.value
                }
            } else if (entry is UrlLinkFrame) {
                // ID3 URL tag
                val id3 = entry
                val id = id3.id.toUpperCase()
                if (id == "WOAS" || id == "WOAF" || id == "WOAR" || id == "WAR") {
                    url = id3.url
                }
            }
        }
        if (title != null || url != null || artist != null || album != null || date != null || genre != null) {
            manager.onMetadataReceived("id3", title, url, artist, album, date, genre)
        }
    }

    /**
     * Shoutcast / Icecast metadata (ICY protocol)
     *
     * https://cast.readme.io/docs/icy
     */
    private fun handleIcyMetadata(manager: MusicManager, metadata: Metadata) {
        for (i in 0 until metadata.length()) {
            val entry = metadata[i]
            if (entry is IcyHeaders) {
                // ICY headers
                val icy = entry
                manager.onMetadataReceived(
                    "icy-headers",
                    icy.name,
                    icy.url,
                    null,
                    null,
                    null,
                    icy.genre
                )
            } else if (entry is IcyInfo) {
                // ICY data
                val icy = entry
                var artist: String?
                var title: String?
                val index = if (icy.title == null) -1 else icy.title!!.indexOf(" - ")
                if (index != -1) {
                    artist = icy.title!!.substring(0, index)
                    title = icy.title!!.substring(index + 3)
                } else {
                    artist = null
                    title = icy.title
                }
                manager.onMetadataReceived("icy", title, icy.url, artist, null, null, null)
            }
        }
    }

    /**
     * Vorbis Comments (Vorbis, FLAC, Opus, Speex, Theora)
     *
     * https://xiph.org/vorbis/doc/v-comment.html
     */
    private fun handleVorbisCommentMetadata(manager: MusicManager, metadata: Metadata) {
        var title: String? = null
        var url: String? = null
        var artist: String? = null
        var album: String? = null
        var date: String? = null
        var genre: String? = null
        for (i in 0 until metadata.length()) {
            val entry = metadata[i] as? VorbisComment ?: continue
            val comment = entry
            val key = comment.key
            when (key) {
                "TITLE" -> {
                    title = comment.value
                }
                "ARTIST" -> {
                    artist = comment.value
                }
                "ALBUM" -> {
                    album = comment.value
                }
                "DATE" -> {
                    date = comment.value
                }
                "GENRE" -> {
                    genre = comment.value
                }
                "URL" -> {
                    url = comment.value
                }
            }
        }
        if (title != null || url != null || artist != null || album != null || date != null || genre != null) {
            manager.onMetadataReceived("vorbis-comment", title, url, artist, album, date, genre)
        }
    }

    /**
     * QuickTime MDTA metadata (mov, qt)
     *
     * https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/Metadata/Metadata.html
     */
    private fun handleQuickTimeMetadata(manager: MusicManager, metadata: Metadata) {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var date: String? = null
        var genre: String? = null
        for (i in 0 until metadata.length()) {
            val entry = metadata[i] as? MdtaMetadataEntry ?: continue
            val mdta = entry
            val key = mdta.key
            try {
                when (key) {
                    "com.apple.quicktime.title" -> {
                        title = String(mdta.value, Charset.defaultCharset())
                    }
                    "com.apple.quicktime.artist" -> {
                        artist = String(mdta.value, Charset.defaultCharset())
                    }
                    "com.apple.quicktime.album" -> {
                        album = String(mdta.value, Charset.defaultCharset())
                    }
                    "com.apple.quicktime.creationdate" -> {
                        date = String(mdta.value, Charset.defaultCharset())
                    }
                    "com.apple.quicktime.genre" -> {
                        genre = String(mdta.value, Charset.defaultCharset())
                    }
                }
            } catch (ex: Exception) {
                // Ignored
            }
        }
        if (title != null || artist != null || album != null || date != null || genre != null) {
            manager.onMetadataReceived("quicktime", title, null, artist, album, date, genre)
        }
    }
}