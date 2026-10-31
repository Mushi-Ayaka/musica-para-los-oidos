package com.musica.paralosoidos.ui.library

import com.musica.paralosoidos.data.TrackEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackFilterTest {
    private fun track(
        id: Long,
        title: String,
        artist: String = "Artista",
        album: String = "Álbum"
    ) = TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 1_000L,
        uri = "content://track/$id",
        displayName = "$title.mp3"
    )

    private fun apply(
        tracks: List<TrackEntity>,
        query: String = "",
        order: SortOrder = SortOrder.AZ,
        letter: Char? = null
    ): List<TrackEntity> = TrackFilter.apply(
        indexed = TrackFilter.index(tracks),
        query = query,
        order = order,
        letter = letter
    )

    @Test
    fun `search matches title artist and album ignoring case`() {
        val tracks = listOf(
            track(1, "Noche", "Daft Punk", "Discovery"),
            track(2, "Daylight", "Matt and Kim", "Grand"),
            track(3, "Aurora", "Björk", "Vespertine")
        )

        assertEquals(listOf(1L), apply(tracks, query = "noche").map { it.id })
        assertEquals(listOf(2L), apply(tracks, query = "matt").map { it.id })
        assertEquals(listOf(3L), apply(tracks, query = "vesper").map { it.id })
    }

    @Test
    fun `letter filter keeps titles starting with the letter`() {
        val tracks = listOf(
            track(1, "Alpha"),
            track(2, "Beta"),
            track(3, "Aurora")
        )

        assertEquals(listOf(1L, 3L), apply(tracks, letter = 'A').map { it.id })
    }

    @Test
    fun `hash filter keeps titles that do not start with a letter`() {
        val tracks = listOf(
            track(1, "Alpha"),
            track(2, "1234"),
            track(3, "...And Justice")
        )

        assertEquals(listOf(2L, 3L), apply(tracks, order = SortOrder.Recent, letter = '#').map { it.id })
    }

    @Test
    fun `sort az and za ignore case`() {
        val tracks = listOf(
            track(1, "beta"),
            track(2, "Alpha"),
            track(3, "Gamma")
        )

        assertEquals(listOf(2L, 1L, 3L), apply(tracks, order = SortOrder.AZ).map { it.id })
        assertEquals(listOf(3L, 1L, 2L), apply(tracks, order = SortOrder.ZA).map { it.id })
    }

    @Test
    fun `recent keeps the original order`() {
        val tracks = listOf(
            track(3, "Gamma"),
            track(1, "Alpha"),
            track(2, "Beta")
        )

        assertEquals(listOf(3L, 1L, 2L), apply(tracks, order = SortOrder.Recent).map { it.id })
    }
}
