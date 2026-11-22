package com.musica.paralosoidos.ui.library

import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.TrackEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackFilterTest {
    private fun track(
        id: Long,
        title: String,
        artist: String = "Artista",
        album: String = "Álbum",
        dateAdded: Long = 0L,
        dateModified: Long = 0L
    ) = TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = 1_000L,
        uri = "content://track/$id",
        displayName = "$title.mp3",
        dateAdded = dateAdded,
        dateModified = dateModified
    )

    private fun apply(
        tracks: List<TrackEntity>,
        query: String = "",
        order: SortOrder = SortOrder.AZ,
        letter: Char? = null,
        catalogMap: Map<String, CatalogTrackEntity> = emptyMap(),
        playCounts: Map<Long, Int> = emptyMap()
    ): List<TrackEntity> = TrackFilter.apply(
        indexed = TrackFilter.index(tracks),
        query = query,
        order = order,
        letter = letter,
        catalogMap = catalogMap,
        playCounts = playCounts
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
            track(2, "beta"),
            track(3, "Amber")
        )

        assertEquals(listOf(1L, 3L), apply(tracks, letter = 'a').map { it.id })
    }

    @Test
    fun `hash filter keeps titles that do not start with a letter`() {
        val tracks = listOf(
            track(1, "Alpha"),
            track(2, "1234"),
            track(3, "...And Justice")
        )

        assertEquals(listOf(3L, 2L), apply(tracks, order = SortOrder.AZ, letter = '#').map { it.id })
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
    fun `recent sorts by date added or modified descending`() {
        val tracks = listOf(
            track(1, "Old", dateAdded = 100L),
            track(2, "Newest", dateAdded = 500L),
            track(3, "Modified", dateModified = 300L)
        )

        assertEquals(listOf(2L, 3L, 1L), apply(tracks, order = SortOrder.Recent).map { it.id })
    }

    @Test
    fun `most played sorts by play count descending`() {
        val tracks = listOf(
            track(1, "Track A"),
            track(2, "Track B"),
            track(3, "Track C")
        )
        val counts = mapOf(1L to 5, 2L to 20, 3L to 1)
        assertEquals(listOf(2L, 1L, 3L), apply(tracks, order = SortOrder.MostPlayed, playCounts = counts).map { it.id })
    }

    @Test
    fun `linked and unlinked filters work properly`() {
        val tracks = listOf(
            track(1, "Linked Track"),
            track(2, "Unlinked Track")
        )
        val catMap = mapOf(
            "content://track/1" to CatalogTrackEntity(
                id = "1",
                title = "Linked Track",
                artist = "Artista",
                album = "Álbum",
                durationMs = 1000L,
                youtubeUrl = "https://youtube.com/watch?v=abc",
                localUri = "content://track/1",
                metadataJson = "{}"
            )
        )
        assertEquals(listOf(1L), apply(tracks, order = SortOrder.Linked, catalogMap = catMap).map { it.id })
        assertEquals(listOf(2L), apply(tracks, order = SortOrder.Unlinked, catalogMap = catMap).map { it.id })
    }
}
