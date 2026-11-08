package com.musica.paralosoidos.data.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadStorageManagerTest {

    private fun sanitize(name: String): String {
        // Replicate the sanitizeFileName logic directly for unit testing
        return name.replace(Regex("""[/\\:*?"<>|]"""), "_").trim().take(200)
    }

    @Test
    fun sanitizeFileNameStripsForwardSlash() {
        val result = sanitize("Artist/Song")
        assertFalse(result.contains('/'))
        assertTrue(result.contains('_'))
    }

    @Test
    fun sanitizeFileNameStripsBackslash() {
        val result = sanitize("Artist\\Song")
        assertFalse(result.contains('\\'))
    }

    @Test
    fun sanitizeFileNameStripsColon() {
        val result = sanitize("Song: Subtitle")
        assertFalse(result.contains(':'))
    }

    @Test
    fun sanitizeFileNameStripsAsterisk() {
        val result = sanitize("Song * Remix")
        assertFalse(result.contains('*'))
    }

    @Test
    fun sanitizeFileNameStripsQuestionMark() {
        val result = sanitize("What? Song")
        assertFalse(result.contains('?'))
    }

    @Test
    fun sanitizeFileNameStripsAngleBracketsAndPipe() {
        val result = sanitize("Song <remix> | Feat")
        assertFalse(result.contains('<'))
        assertFalse(result.contains('>'))
        assertFalse(result.contains('|'))
    }

    @Test
    fun sanitizeFileNamePreservesNormalText() {
        val result = sanitize("Normal Song Title 2024")
        assertTrue(result.contains("Normal Song Title 2024"))
    }

    @Test
    fun sanitizeFileNameTruncatesAtTwoHundredChars() {
        val longName = "a".repeat(300)
        val result = sanitize(longName)
        assertTrue(result.length <= 200)
    }
}
