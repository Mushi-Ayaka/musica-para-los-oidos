package com.musica.paralosoidos.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class MediaScannerHelperTest {

    @Test
    fun mimeTypeForMp3ReturnsAudioMpeg() {
        val file = File("test_song.mp3")
        assertEquals("audio/mpeg", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeForM4aReturnsAudioMp4() {
        val file = File("test_song.m4a")
        assertEquals("audio/mp4", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeForOggReturnsAudioOgg() {
        val file = File("test_song.ogg")
        assertEquals("audio/ogg", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeForWavReturnsAudioWav() {
        val file = File("test_song.wav")
        assertEquals("audio/x-wav", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeForFlacReturnsAudioFlac() {
        val file = File("test_song.flac")
        assertEquals("audio/flac", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeForWebmReturnsAudioGeneric() {
        val file = File("test_song.webm")
        assertEquals("audio/*", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeForUnknownExtensionReturnsFallback() {
        val file = File("test_song.xyz")
        assertEquals("audio/*", MediaScannerHelper.mimeTypeFor(file))
    }

    @Test
    fun mimeTypeIsCaseInsensitive() {
        val fileUpper = File("test_song.MP3")
        val fileMixed = File("test_song.M4A")
        assertEquals("audio/mpeg", MediaScannerHelper.mimeTypeFor(fileUpper))
        assertEquals("audio/mp4", MediaScannerHelper.mimeTypeFor(fileMixed))
    }
}
