package com.musica.paralosoidos.playback

import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsMatcherTest {
    @Test
    fun matchesPrivateLrcWithEncodingSuffix() {
        val score = LyricsMatcher.score(
            audioFileName = "Sia - Snowman(MP3_160K).mp3",
            trackTitle = "Snowman",
            trackArtist = "Sia",
            candidateName = "Sia - Snowman(MP3_160K)_private.lrc"
        )

        assertTrue("Expected a strong match, got $score", score >= 0.5)
    }

    @Test
    fun ignoresAccentsAndNoiseWords() {
        val score = LyricsMatcher.score(
            audioFileName = "Café Tacvba - Eres (Official Audio).mp3",
            trackTitle = "Eres",
            trackArtist = "Café Tacvba",
            candidateName = "Cafe Tacvba - Eres lyrics.lrc"
        )

        assertTrue("Expected a strong match, got $score", score >= 0.5)
    }

    @Test
    fun rejectsUnrelatedCandidates() {
        val score = LyricsMatcher.score(
            audioFileName = "Sia - Snowman.mp3",
            trackTitle = "Snowman",
            trackArtist = "Sia",
            candidateName = "Queen - Bohemian Rhapsody.lrc"
        )

        assertTrue("Expected a low score, got $score", score < 0.5)
    }
}
