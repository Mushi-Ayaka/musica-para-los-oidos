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
}
