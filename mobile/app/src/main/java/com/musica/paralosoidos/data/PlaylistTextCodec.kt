package com.musica.paralosoidos.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class AmbiguousTrackConflict(
    val songTitle: String,
    val candidates: List<TrackEntity>
)

data class PlaylistImportPlan(
    val playlistName: String,
    val autoResolvedTrackIds: List<Long>,
    val conflicts: List<AmbiguousTrackConflict>,
    val notFoundTitles: List<String>
)

object PlaylistTextCodec {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

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

    fun analyzePlaylistJson(text: String, library: List<TrackEntity>): PlaylistImportPlan {
        val root = json.parseToJsonElement(text) as? JsonObject
            ?: error("El JSON debe ser un objeto con nombre y canciones.")
        val name = (root["name"] as? JsonPrimitive)?.content?.trim()
            ?.takeIf(String::isNotBlank)
            ?: error("El JSON debe incluir un nombre de lista.")
        val songs = root["songs"] as? JsonArray
            ?: error("El campo 'songs' debe ser una lista de nombres.")
        require(songs.isNotEmpty()) { "La lista JSON no contiene canciones." }

        val normalizedLibrary = library.groupBy { it.title.normalizedName() }
        val autoResolvedIds = mutableListOf<Long>()
        val conflicts = mutableListOf<AmbiguousTrackConflict>()
        val notFound = mutableListOf<String>()

        for (value in songs) {
            val songName = (value as? JsonPrimitive)?.content?.trim() ?: continue
            if (songName.isBlank()) continue
            val matches = normalizedLibrary[songName.normalizedName()].orEmpty()
            when {
                matches.size == 1 -> {
                    autoResolvedIds.add(matches.first().id)
                }
                matches.size > 1 -> {
                    conflicts.add(AmbiguousTrackConflict(songName, matches))
                }
                else -> {
                    val partialMatches = library.filter {
                        it.title.normalizedName().contains(songName.normalizedName()) ||
                        songName.normalizedName().contains(it.title.normalizedName())
                    }
                    when {
                        partialMatches.size == 1 -> autoResolvedIds.add(partialMatches.first().id)
                        partialMatches.size > 1 -> conflicts.add(AmbiguousTrackConflict(songName, partialMatches))
                        else -> notFound.add(songName)
                    }
                }
            }
        }

        return PlaylistImportPlan(
            playlistName = name,
            autoResolvedTrackIds = autoResolvedIds.distinct(),
            conflicts = conflicts,
            notFoundTitles = notFound
        )
    }

    fun createPlaylist(name: String, trackIds: List<Long>): MusicPlaylist {
        require(name.isNotBlank()) { "La lista debe tener un nombre." }
        return MusicPlaylist(
            id = java.util.UUID.randomUUID().toString(),
            name = name.trim(),
            trackIds = trackIds.distinct()
        )
    }

    fun importPlaylistJson(text: String, library: List<TrackEntity>): MusicPlaylist {
        val plan = analyzePlaylistJson(text, library)
        if (plan.conflicts.isNotEmpty()) {
            error("El nombre '${plan.conflicts.first().songTitle}' coincide con varias pistas; importa una lista con nombres únicos.")
        }
        if (plan.notFoundTitles.isNotEmpty() && plan.autoResolvedTrackIds.isEmpty()) {
            error("No se encontró la canción '${plan.notFoundTitles.first()}' en la biblioteca.")
        }
        if (plan.autoResolvedTrackIds.isEmpty()) {
            error("La lista JSON no contiene canciones válidas de la biblioteca.")
        }
        return createPlaylist(plan.playlistName, plan.autoResolvedTrackIds)
    }

    private fun String.normalizedName(): String = trim().lowercase()
}
