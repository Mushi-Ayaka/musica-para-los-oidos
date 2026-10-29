package com.musica.paralosoidos.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object PlaylistTextCodec {
    private val json = Json { prettyPrint = true }

    fun exportNamesAsText(tracks: List<TrackEntity>): String =
        tracks.joinToString(separator = "\n", postfix = if (tracks.isEmpty()) "" else "\n") {
            it.title
        }

    fun exportPlaylistJson(name: String, tracks: List<TrackEntity>): String {
        require(name.isNotBlank()) { "La lista debe tener un nombre." }
        return json.encodeToString(
            JsonObject.serializer(),
            buildJsonObject {
                put("name", name.trim())
                put("songs", JsonArray(tracks.map { JsonPrimitive(it.title) }))
            }
        )
    }

    fun importPlaylistJson(text: String, library: List<TrackEntity>): MusicPlaylist {
        val root = json.parseToJsonElement(text) as? JsonObject
            ?: error("El JSON debe ser un objeto con nombre y canciones.")
        val name = (root["name"] as? JsonPrimitive)?.content?.trim()
            ?.takeIf(String::isNotBlank)
            ?: error("El JSON debe incluir un nombre de lista.")
        val songs = root["songs"] as? JsonArray
            ?: error("El campo 'songs' debe ser una lista de nombres.")
        require(songs.isNotEmpty()) { "La lista JSON no contiene canciones." }
        val normalizedLibrary = library.groupBy { it.title.normalizedName() }
        val ids = songs.mapIndexed { index, value ->
            val songName = (value as? JsonPrimitive)?.content?.trim()
                ?.takeIf(String::isNotBlank)
                ?: error("La canción ${index + 1} debe ser un nombre de texto.")
            val matches = normalizedLibrary[songName.normalizedName()].orEmpty()
            when (matches.size) {
                0 -> error("No se encontró la canción '$songName' en la biblioteca.")
                1 -> matches.single().id
                else -> error("El nombre '$songName' coincide con varias pistas; importa una lista con nombres únicos.")
            }
        }
        require(ids.distinct().size == ids.size) { "La lista JSON repite canciones." }
        return MusicPlaylist(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            trackIds = ids
        )
    }

    private fun String.normalizedName(): String = trim().lowercase()
}
