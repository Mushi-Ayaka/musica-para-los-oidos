package com.musica.paralosoidos.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBackupCodecTest {
    private fun track(
        id: Long,
        title: String,
        artist: String = "Artista",
        displayName: String = "$title.mp3"
    ) = TrackEntity(
        id = id,
        title = title,
        artist = artist,
        album = "Álbum",
        durationMs = 1_000L,
        uri = "content://track/$id",
        displayName = displayName
    )

    private fun playlist(id: String, name: String, trackIds: List<Long>) =
        MusicPlaylist(id = id, name = name, trackIds = trackIds)

    @Test
    fun `encode and decode keep playlists and settings`() {
        val library = listOf(track(1, "Uno"), track(2, "Dos"))
        val encoded = AutoBackupCodec.encode(
            playlists = listOf(playlist("p1", "Favoritas", listOf(1L, 2L))),
            theme = ThemeSettingsState(presetId = "ocean"),
            eqPresets = listOf(
                SavedEqPreset("eq1", "Vocal", List(10) { 2 }, 0, 10)
            ),
            library = library,
            exportedAt = 123L
        )

        val decoded = AutoBackupCodec.decode(encoded)

        assertEquals(123L, decoded.exportedAt)
        assertEquals(1, decoded.playlists.size)
        assertEquals("Favoritas", decoded.playlists.single().name)
        assertEquals(listOf("Uno", "Dos"), decoded.playlists.single().tracks.map { it.title })
        assertEquals("ocean", decoded.theme?.presetId)
        assertEquals("Vocal", decoded.eqPresets.single().name)
    }

    @Test
    fun `restore prefers uri and falls back to normalized title`() {
        val library = listOf(
            track(10, "Canción", "Artista", "cancion.mp3"),
            track(20, "Otra", "Otro", "otra.mp3")
        )
        val backup = AutoBackupFile(
            playlists = listOf(
                BackupPlaylist(
                    id = "p1",
                    name = "Lista",
                    tracks = listOf(
                        BackupTrack(
                            title = "Canción",
                            artist = "Artista",
                            uri = "content://track/10",
                            displayName = "cancion.mp3"
                        ),
                        BackupTrack(
                            title = "  OTRA ",
                            artist = "Otro",
                            displayName = "otra.mp3"
                        )
                    )
                )
            )
        )

        val restored = AutoBackupCodec.toPlaylists(backup, library)

        assertEquals(listOf(10L, 20L), restored.single().trackIds)
    }

    @Test
    fun `merge skips playlists with the same id or name`() {
        val local = listOf(playlist("p1", "Favoritas", listOf(1L)))
        val restored = listOf(
            playlist("p1", "Otra copia", listOf(2L)),
            playlist("p2", "favoritas", listOf(3L)),
            playlist("p3", "Nuevas", listOf(4L))
        )

        val merged = AutoBackupCodec.merge(local, restored)

        assertEquals(listOf("p1", "p3"), merged.map { it.id })
        assertTrue(merged.last().trackIds.contains(4L))
    }
}
