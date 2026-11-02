package com.musica.paralosoidos.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class LrcLyricsTest {
    @Test
    fun parsesCentisecondAndMillisecondTimestampsAndMultipleTags() {
        val result = LrcLyrics.parse(
            """
                [00:02.25]Second
                [00:01.2][00:03.450]First
            """.trimIndent()
        )

        assertEquals(
            listOf(
                LyricLine(1_200, "First"),
                LyricLine(2_250, "Second"),
                LyricLine(3_450, "First")
            ),
            result
        )
    }

    @Test
    fun keepsUntimedLyricsAsPlainDocument() {
        val result = LrcLyrics.parseDocument(
            """
                [ar:Sia]
                Snowman
                (MP3_160K)

                Es invierno otra vez
            """.trimIndent()
        )

        assertEquals(false, result.synced)
        assertEquals(listOf("Snowman", "(MP3_160K)", "Es invierno otra vez"), result.lines.map { it.text })
    }

    @Test
    fun ignoresMetadataMalformedTimesAndEmptyLines() {
        val result = LrcLyrics.parse(
            """
                [ar:Artist]
                [00:61.00]Invalid seconds
                [00:04.00]
                [01:05]Valid
            """.trimIndent()
        )

        assertEquals(listOf(LyricLine(65_000, "Valid")), result)
    }
}
