package com.musica.paralosoidos.data

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PlaylistTextCodecTest {
    private val library = listOf(
        track(id = 1, title = "Primera"),
        track(id = 2, title = "Segunda")
    )

    @Test
    fun exportsOnlyTrackNamesToText() {
        assertEquals(
            "Primera\nSegunda\n",
            PlaylistTextCodec.exportNamesAsText(library)
        )
    }

    @Test
    fun importsJsonPlaylistByCaseInsensitiveTrackName() {
        val imported = PlaylistTextCodec.importPlaylistJson(
            """{"name":"Favoritas","songs":["primera","SEGUNDA"]}""",
            library
        )

        assertEquals("Favoritas", imported.name)
        assertEquals(listOf(1L, 2L), imported.trackIds)
    }

    @Test
    fun rejectsMissingAndAmbiguousTracks() {
        assertThrows(IllegalStateException::class.java) {
            PlaylistTextCodec.importPlaylistJson(
                """{"name":"No encontrada","songs":["Otra"]}""",
                library
            )
        }
        assertThrows(IllegalStateException::class.java) {
            PlaylistTextCodec.importPlaylistJson(
                """{"name":"Ambigua","songs":["Primera"]}""",
                library + track(id = 3, title = "Primera")
            )
        }
    }

    @Test
    fun exportsJsonWithStableNameAndSongsFields() {
        val result = kotlinx.serialization.json.Json.parseToJsonElement(
            PlaylistTextCodec.exportPlaylistJson("Favoritas", library)
        ).jsonObject

        assertEquals("Favoritas", result.getValue("name").jsonPrimitive.content)
        assertEquals(
            listOf("Primera", "Segunda"),
            result.getValue("songs").jsonArray.map { it.jsonPrimitive.content }
        )
    }

    private fun track(id: Long, title: String) = TrackEntity(
        id = id,
        title = title,
        artist = "Artista",
        album = "Álbum",
        durationMs = 1_000,
        uri = "content://track/$id",
        displayName = "$title.mp3"
    )
}
