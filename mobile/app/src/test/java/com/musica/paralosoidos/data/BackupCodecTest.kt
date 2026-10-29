package com.musica.paralosoidos.data

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {
    @Test
    fun importPreservesYoutubeLinksPlaylistsAndUnknownSettings() {
        val data = BackupCodec.parseData(
            """
            {
              "app":"Musica para los Oidos",
              "data":{
                "tracks":[{"id":7,"title":"Tema","artist":"Artista","youtubeUrl":"https://youtu.be/example"}],
                "playlists":[{"id":"pl-1","name":"Favoritos","songIds":[7]}],
                "audioSettings":{"playbackSpeed":1.25},
                "futureSetting":{"enabled":true}
              }
            }
            """.trimIndent()
        )

        assertEquals(
            "https://youtu.be/example",
            data["tracks"]!!.jsonArray[0].jsonObject["youtubeUrl"]!!.jsonPrimitive.content
        )
        assertEquals("Favoritos", data["playlists"]!!.jsonArray[0].jsonObject["name"]!!.jsonPrimitive.content)
        assertTrue("audioSettings" in data)
        assertTrue("futureSetting" in data)
    }

    @Test
    fun linksOnlyExactUniqueTitleOrArtistDisambiguation() {
        val data = BackupCodec.parseData(
            """
            {
              "app":"Musica para los Oidos",
              "data":{"tracks":[
                {"id":1,"title":"Tema","artist":"Artista A"},
                {"id":2,"title":"Duplicada","artist":"Artista B"},
                {"id":3,"title":"duplicada","artist":"Artista C"},
                {"id":4,"title":"Ambigua","artist":""}
              ]}
            }
            """.trimIndent()
        )
        val (linked, summary) = BackupCodec.linkLocalTracks(
            data,
            listOf(
                localTrack(1, "Tema", "Artista A", "Tema.mp3"),
                localTrack(2, "Duplicada", "Artista A", "Duplicada-1.mp3"),
                localTrack(3, "Duplicada", "Artista B", "Duplicada-2.mp3"),
                localTrack(4, "duplicada", "Artista C", "duplicada.mp3"),
                localTrack(5, "Ambigua", "Artista A", "Ambigua-1.mp3"),
                localTrack(6, "Ambigua", "Artista B", "Ambigua-2.mp3")
            )
        )
        val items = linked["tracks"]!!.jsonArray.map { it.jsonObject }

        assertEquals("Tema.mp3", items[0]["matchedFileName"]!!.jsonPrimitive.content)
        assertEquals("Duplicada-2.mp3", items[1]["matchedFileName"]!!.jsonPrimitive.content)
        assertEquals("duplicada.mp3", items[2]["matchedFileName"]!!.jsonPrimitive.content)
        assertNull(items[3]["matchedFileName"])
        assertFalse("matchedLocalUri" in items[3])
        assertEquals("content://media/1", items[0]["matchedLocalUri"]!!.jsonPrimitive.content)
        assertEquals(3, summary.matchedTracks)
        assertEquals(1, summary.unmatchedTracks)
    }

    @Test
    fun portableBackupDoesNotContainDeviceSpecificFileLinks() {
        val local = localTrack(1, "Tema", "A", "Tema.mp3")
        val linked = BackupCodec.linkLocalTracks(
            BackupCodec.parseData(
                """{"app":"Musica para los Oidos","data":{"tracks":[{"id":1,"title":"Tema","artist":"A"}]}}"""
            ),
            listOf(local)
        )

        assertTrue(BackupCodec.representsLocalTrack(linked.first["tracks"]!!.jsonArray.single().jsonObject, local))
        val portable = BackupCodec.portableData(linked.first)
        val track = portable["tracks"]!!.jsonArray.single().jsonObject

        assertFalse("matchedFileName" in track)
        assertFalse("matchedLocalUri" in track)
    }

    @Test
    fun normalizedCatalogKeepsYoutubeUrlAndMatchedLocalUri() {
        val local = localTrack(9, "Tema local", "Artista", "Tema local.mp3")
        val (linked, _) = BackupCodec.linkLocalTracks(
            BackupCodec.parseData(
                """
                {
                  "app":"Musica para los Oidos",
                  "data":{"tracks":[
                    {"id":22,"title":"Tema local","artist":"Artista","duration":195,
                     "youtubeUrl":"https://youtu.be/abc","lyrics":["línea"]}
                  ]}
                }
                """.trimIndent()
            ),
            listOf(local)
        )

        val saved = BackupCodec.toCatalogTracks(linked).single()

        assertEquals("Tema local", saved.title)
        assertEquals("https://youtu.be/abc", saved.youtubeUrl)
        assertEquals(local.uri, saved.localUri)
        assertTrue("lyrics" in saved.metadataJson)
    }

    @Test
    fun analyzerAcceptsMatchingTitlesAndRejectsUnrelatedResults() {
        assertTrue(BackupCodec.acceptsSearchResult("Amanecer Azul", "Amanecer Azul - Video Oficial"))
        assertFalse(BackupCodec.acceptsSearchResult("Amanecer Azul", "Azul Profundo"))
        assertFalse(BackupCodec.acceptsSearchResult("Mi", "Mi canción"))
    }

    @Test
    fun analyzerResultIsPersistedAndNotQueuedAgain() {
        val track = BackupCodec.toCatalogTracks(
            BackupCodec.parseData(
                """{"app":"Musica para los Oidos","data":{"tracks":[{"id":17,"title":"Tema","artist":"Artista"}]}}"""
            )
        ).single()

        assertTrue(BackupCodec.needsYouTubeAnalysis(track))
        val analyzed = BackupCodec.withAnalyzerResult(track, null)
        assertFalse(BackupCodec.needsYouTubeAnalysis(analyzed))
        assertTrue("ytAttempted" in analyzed.metadataJson)
    }

    private fun localTrack(id: Long, title: String, artist: String, fileName: String) =
        TrackEntity(
            id = id,
            title = title,
            artist = artist,
            album = "",
            durationMs = 180_000,
            uri = "content://media/$id",
            displayName = fileName
        )
}
