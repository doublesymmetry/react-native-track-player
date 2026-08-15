package com.doublesymmetry.trackplayer.model

import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackMetadataTest {
    @Test
    fun fromIcy_normalizesWindows1252PunctuationBeforeSplittingTitle() {
        val metadata = Metadata(
            IcyInfo(byteArrayOf(), "Probe\u0092s - Hatchback\u0092s", null)
        )

        val result = PlaybackMetadata.fromIcy(metadata)

        assertEquals("Probe’s", result?.artist)
        assertEquals("Hatchback’s", result?.title)
    }

    @Test
    fun normalizeLegacyIcyPunctuation_mapsOnlyDefinedWindows1252C1Characters() {
        val cases = listOf(
            "\u0080" to "€",
            "\u0081" to "\u0081",
            "\u0082" to "‚",
            "\u0083" to "ƒ",
            "\u0084" to "„",
            "\u0085" to "…",
            "\u0086" to "†",
            "\u0087" to "‡",
            "\u0088" to "ˆ",
            "\u0089" to "‰",
            "\u008A" to "Š",
            "\u008B" to "‹",
            "\u008C" to "Œ",
            "\u008D" to "\u008D",
            "\u008E" to "Ž",
            "\u008F" to "\u008F",
            "\u0090" to "\u0090",
            "\u0091" to "‘",
            "\u0092" to "’",
            "\u0093" to "“",
            "\u0094" to "”",
            "\u0095" to "•",
            "\u0096" to "–",
            "\u0097" to "—",
            "\u0098" to "˜",
            "\u0099" to "™",
            "\u009A" to "š",
            "\u009B" to "›",
            "\u009C" to "œ",
            "\u009D" to "\u009D",
            "\u009E" to "ž",
            "\u009F" to "Ÿ",
            "Déjà vu" to "Déjà vu",
            "Already correct ’" to "Already correct ’"
        )

        cases.forEach { (input, expected) ->
            assertEquals("input U+${input.first().code.toString(16)}", expected, input.normalizeLegacyIcyPunctuation())
        }
    }
}
