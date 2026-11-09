package com.musica.paralosoidos.data.download

import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataCleanerTest {

    @Test
    fun clean_removesOfficialVideoBrackets() {
        val result = MetadataCleaner.clean(
            rawTitle = "Dua Lipa - Levitating (Official Music Video) [HD]",
            channelName = "Dua Lipa"
        )
        assertEquals("Dua Lipa", result.artist)
        assertEquals("Levitating", result.title)
    }

    @Test
    fun clean_handlesEnDashDelimiter() {
        val result = MetadataCleaner.clean(
            rawTitle = "Bad Bunny – Ojitos Lindos (Visualizer)",
            channelName = "Bad Bunny"
        )
        assertEquals("Bad Bunny", result.artist)
        assertEquals("Ojitos Lindos", result.title)
    }

    @Test
    fun clean_usesChannelWhenNoDelimiterPresent() {
        val result = MetadataCleaner.clean(
            rawTitle = "Bohemian Rhapsody (Remastered 2011)",
            channelName = "Queen - Topic"
        )
        assertEquals("Queen", result.artist)
        assertEquals("Bohemian Rhapsody", result.title)
    }

    @Test
    fun clean_removesAudioOficialNoise() {
        val result = MetadataCleaner.clean(
            rawTitle = "tiki tiki - Audio Oficial",
            channelName = "Artista Oficial"
        )
        assertEquals("tiki tiki", result.artist)
    }
}
