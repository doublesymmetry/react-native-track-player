package com.doublesymmetry.trackplayer.model

import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.flac.VorbisComment
import com.google.android.exoplayer2.metadata.icy.IcyHeaders
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.metadata.id3.UrlLinkFrame
import com.google.android.exoplayer2.metadata.mp4.MdtaMetadataEntry

data class PlaybackMetadata(
    val source: String,
    var title: String? = null,
    var url: String? = null,
    var artist: String? = null,
    var album: String? = null,
    var date: String? = null,
    var genre: String? = null
) {
    companion object {
        /**
         * ID3 Metadata (MP3)
         * https://en.wikipedia.org/wiki/ID3
         */
        fun fromId3Metadata(metadata: Metadata): PlaybackMetadata? {
            var handled = false

            var title: String? = null
            var url: String? = null
            var artist: String? = null
            var album: String? = null
            var date: String? = null
            var genre: String? = null

            (0 until metadata.length()).forEach { i ->
                when (val entry = metadata[i]) {
                    is TextInformationFrame -> {
                        when (entry.id.uppercase()) {
                            "TIT2", "TT2" -> {
                                handled = true
                                title = entry.value
                            }
                            "TALB", "TOAL", "TAL" -> {
                                handled = true
                                album = entry.value
                            }
                            "TOPE", "TPE1", "TP1" -> {
                                handled = true
                                artist = entry.value
                            }
                            "TDRC", "TOR" -> {
                                handled = true
                                date = entry.value
                            }
                            "TCON", "TCO" -> {
                                handled = true
                                genre = entry.value
                            }

                        }
                    }
                    is UrlLinkFrame -> {
                        when (entry.id.uppercase()) {
                            "WOAS", "WOAF", "WOAR", "WAR" -> {
                                handled = true;
                                url = entry.url;
                            }
                        }
                    }
                }
            }


            return if (handled) PlaybackMetadata("id3", title, url, artist, album, date, genre) else null
        }

        /**
         * Shoutcast / Icecast metadata (ICY protocol)
         * https://cast.readme.io/docs/icy
         */
        fun fromIcy(metadata: Metadata): PlaybackMetadata? {
            for (i in 0 until metadata.length()) {
                when (val entry = metadata[i]) {
                    is IcyHeaders -> {
                        return PlaybackMetadata("icy-headers", title = entry.name, url = entry.url, genre = entry.genre)
                    }
                    is IcyInfo -> {
                        val artist: String?
                        val title: String?
                        val normalizedTitle = entry.title?.normalizeLegacyIcyPunctuation()
                        val index = normalizedTitle?.indexOf(" - ") ?: -1
                        if (index != -1) {
                            artist = normalizedTitle!!.substring(0, index)
                            title = normalizedTitle.substring(index + 3)
                        } else {
                            artist = null
                            title = normalizedTitle
                        }

                        return PlaybackMetadata("icy", title = title, url = entry.url, artist = artist)
                    }
                }
            }

            return null
        }

        /**
         * Vorbis Comments (Vorbis, FLAC, Opus, Speex, Theora)
         * https://xiph.org/vorbis/doc/v-comment.html
         */
        fun fromVorbisComment(metadata: Metadata): PlaybackMetadata? {
            var handled = false;

            var title: String? = null
            var url: String? = null
            var artist: String? = null
            var album: String? = null
            var date: String? = null
            var genre: String? = null

            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                if (entry is VorbisComment) {
                    when (entry.key) {
                        "TITLE" -> {
                            handled = true
                            title = entry.value;
                        }
                        "ARTIST" -> {
                            handled = true
                            artist = entry.value;
                        }
                        "ALBUM" -> {
                            handled = true
                            album = entry.value;
                        }
                        "DATE" -> {
                            handled = true
                            date = entry.value
                        }
                        "GENRE" -> {
                            handled = true
                            genre = entry.value
                        }
                        "URL" -> {
                            handled = true
                            url = entry.value
                        }
                    }
                }
            }
            return if (handled) PlaybackMetadata("vorbis-comment", title, url, artist, album, date, genre) else null
        }

        /**
         * QuickTime MDTA metadata (mov, qt)
         * https://developer.apple.com/library/archive/documentation/QuickTime/QTFF/Metadata/Metadata.html
         */
        fun fromQuickTime(metadata: Metadata): PlaybackMetadata? {
            var handled = false;

            var title: String? = null
            var artist: String? = null
            var album: String? = null
            var date: String? = null
            var genre: String? = null

            for (i in 0 until metadata.length()) {
                val entry = metadata[i];
                if (entry is MdtaMetadataEntry) {
                    when (entry.key) {
                        "com.apple.quicktime.title" -> {
                            handled = true
                            title = entry.value.toString();
                        }
                        "com.apple.quicktime.artist" -> {
                            handled = true
                            artist = entry.value.toString();
                        }
                        "com.apple.quicktime.album" -> {
                            handled = true
                            album = entry.value.toString();
                        }
                        "com.apple.quicktime.creationdate" -> {
                            handled = true
                            date = entry.value.toString();
                        }
                        "com.apple.quicktime.genre" -> {
                            handled = true
                            genre = entry.value.toString();
                        }
                    }
                }
            }

            return if (handled) PlaybackMetadata("quicktime", title = title, artist = artist, album = album, date = date, genre = genre) else null
        }
    }
}

/**
 * ExoPlayer exposes non-UTF-8 ICY metadata as ISO-8859-1. Some streams use
 * Windows-1252 punctuation in the C1 range, so map only its defined characters.
 */
internal fun String.normalizeLegacyIcyPunctuation(): String = map { character ->
    when (character) {
        '\u0080' -> '€'
        '\u0082' -> '‚'
        '\u0083' -> 'ƒ'
        '\u0084' -> '„'
        '\u0085' -> '…'
        '\u0086' -> '†'
        '\u0087' -> '‡'
        '\u0088' -> 'ˆ'
        '\u0089' -> '‰'
        '\u008A' -> 'Š'
        '\u008B' -> '‹'
        '\u008C' -> 'Œ'
        '\u008E' -> 'Ž'
        '\u0091' -> '‘'
        '\u0092' -> '’'
        '\u0093' -> '“'
        '\u0094' -> '”'
        '\u0095' -> '•'
        '\u0096' -> '–'
        '\u0097' -> '—'
        '\u0098' -> '˜'
        '\u0099' -> '™'
        '\u009A' -> 'š'
        '\u009B' -> '›'
        '\u009C' -> 'œ'
        '\u009E' -> 'ž'
        '\u009F' -> 'Ÿ'
        else -> character
    }
}.joinToString("")
