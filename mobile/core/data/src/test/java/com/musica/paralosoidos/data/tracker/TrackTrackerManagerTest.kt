package com.musica.paralosoidos.data.tracker

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackTrackerManagerTest {
    @Test
    fun rejectsNumericAndShortTitles() {
        listOf("", "a", "ab", "123", "7725362").forEach { title ->
            assertFalse("Expected '$title' to be rejected", TrackTrackerManager.isMusicTitle(title))
        }
    }

    @Test
    fun rejectsCompactAlphanumericIdentifiersFromFourToSixteenCharacters() {
        listOf("7uvE73L470122", "Ab12", "A1B2C3").forEach { title ->
            assertFalse("Expected '$title' to be rejected", TrackTrackerManager.isMusicTitle(title))
        }
    }

    @Test
    fun acceptsTitlesWithWordsOrSeparatorsAndLongerIdentifiers() {
        listOf("Blue Monday", "AB12-CD", "song_123", "Abc", "A".repeat(17)).forEach { title ->
            assertTrue("Expected '$title' to be accepted", TrackTrackerManager.isMusicTitle(title))
        }
    }

    @Test
    fun trimsWhitespaceBeforeValidating() {
        assertFalse(TrackTrackerManager.isMusicTitle("  123  "))
        assertTrue(TrackTrackerManager.isMusicTitle("  Blue Monday  "))
    }
}
