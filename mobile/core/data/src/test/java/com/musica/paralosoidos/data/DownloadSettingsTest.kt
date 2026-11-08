package com.musica.paralosoidos.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadSettingsTest {
    @Test
    fun downloadSettingsRequireAnExplicitFormatAndQualityChoice() {
        assertFalse(DownloadSettingsState().isConfigured)
        assertTrue(DownloadSettingsState(AudioOutputFormat.BEST).isConfigured)
        assertFalse(DownloadSettingsState(AudioOutputFormat.MP3).isConfigured)
        assertFalse(DownloadSettingsState(AudioOutputFormat.MP3, 999).isConfigured)
        assertTrue(DownloadSettingsState(AudioOutputFormat.MP3, 320).isConfigured)
    }

    @Test
    fun downloadSettingsDefaultsSyncLyricsToTrue() {
        val defaultState = DownloadSettingsState()
        assertTrue(defaultState.syncLyrics)

        val customState = DownloadSettingsState(format = AudioOutputFormat.MP3, mp3Bitrate = 320, syncLyrics = false)
        assertFalse(customState.syncLyrics)
    }
}
