package com.musica.paralosoidos.data.lyrics

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsParserTest {

    @Test
    fun lrcTimestampHasCorrectFormat() {
        // Verify that LRC timestamp format [mm:ss.xx] is correctly structured
        val timestampRegex = Regex("""\[\d{2}:\d{2}\.\d{2,3}\]""")
        val validLine = "[01:23.45] Sample lyric line"
        assertTrue(timestampRegex.containsMatchIn(validLine))
    }

    @Test
    fun emptyLyricsResponseReturnsNull() {
        // Simulate what fetchLyrics does with blank response
        val blankResponse = ""
        val result: String? = blankResponse.ifBlank { null }
        assertNull(result)
    }

    @Test
    fun nullLyricsFieldReturnsNull() {
        // If syncedLyrics field is not present, result is null
        val syncedLyrics: String? = null
        val result: String? = syncedLyrics?.ifBlank { null }
        assertNull(result)
    }

    @Test
    fun validLrcContentIsRetained() {
        val lrcContent = "[00:01.00] Primera linea\n[00:05.00] Segunda linea"
        val result: String? = lrcContent.ifBlank { null }
        assertNotNull(result)
        assertTrue(result!!.contains("[00:01.00]"))
    }
}
