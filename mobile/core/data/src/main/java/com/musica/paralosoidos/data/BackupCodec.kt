package com.musica.paralosoidos.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

data class BackupImportSummary(
    val totalTracks: Int,
    val matchedTracks: Int
) {
    val unmatchedTracks: Int
        get() = totalTracks - matchedTracks
}

internal object BackupCodec {
    fun parseData(json: String): JsonObject {
        val envelope = runCatching { Json.parseToJsonElement(json).jsonObject }
            .getOrElse { throw IllegalArgumentException("El archivo no contiene un JSON válido.", it) }
        require(envelope["app"]?.stringValue() == "Musica para los Oidos") {
            "El archivo no corresponde a una copia de seguridad de Música para los Oídos."
        }
        val data = envelope["data"] as? JsonObject
            ?: throw IllegalArgumentException("La copia no contiene la sección de datos.")
        require(data["tracks"] is JsonArray) {
            "La copia no contiene un catálogo de pistas válido."
        }
        val tracks = (data["tracks"] as JsonArray).map { track ->
            if (track is JsonObject) JsonObject(track - "matchedFileName" - "matchedLocalUri") else track
        }
        return JsonObject(data + ("tracks" to JsonArray(tracks)))
    }

    fun linkLocalTracks(
        data: JsonObject,
        localTracks: List<TrackEntity>
    ): Pair<JsonObject, BackupImportSummary> {
        val catalog = data["tracks"] as? JsonArray
            ?: return data to BackupImportSummary(0, 0)
        val linkedTracks = catalog.map { element ->
            val track = element as? JsonObject ?: return@map element
            val title = track["title"]?.stringValue() ?: return@map element
            val artist = track["artist"]?.stringValue().orEmpty()
            val titleMatches = localTracks.filter { it.title == title }
            val candidates = if (titleMatches.size > 1 && artist.isNotBlank()) {
                titleMatches.filter { it.artist == artist }
            } else {
                titleMatches
            }
            val matched = candidates.singleOrNull()
            if (matched == null) {
                JsonObject(track - "matchedFileName" - "matchedLocalUri")
            } else {
                JsonObject(
                    track +
                        ("matchedFileName" to JsonPrimitive(matched.displayName)) +
                        ("matchedLocalUri" to JsonPrimitive(matched.uri))
                )
            }
        }
        val linkedData = JsonObject(data + ("tracks" to JsonArray(linkedTracks)))
        val matchedCount = linkedTracks.count { element ->
            (element as? JsonObject)?.containsKey("matchedLocalUri") == true
        }
        return linkedData to BackupImportSummary(catalog.size, matchedCount)
    }

    fun portableData(data: JsonObject): JsonObject {
        val catalog = data["tracks"] as? JsonArray ?: return data
        val tracks = catalog.map { track ->
            if (track is JsonObject) JsonObject(track - "matchedFileName" - "matchedLocalUri") else track
        }
        return JsonObject(data + ("tracks" to JsonArray(tracks)))
    }

    fun toCatalogTracks(data: JsonObject): List<CatalogTrackEntity> {
        val catalog = data["tracks"] as? JsonArray ?: return emptyList()
        return catalog.mapIndexedNotNull { index, element ->
            val track = element as? JsonObject ?: return@mapIndexedNotNull null
            val title = track["title"].stringValue()?.takeIf(String::isNotBlank)
                ?: return@mapIndexedNotNull null
            val artist = track["artist"].stringValue().orEmpty()
            val sourceId = track["id"].stringValue()
                ?: "entry-$index-${title.hashCode().toUInt().toString(16)}"
            val id = "$sourceId-${artist.hashCode().toUInt().toString(16)}"
            CatalogTrackEntity(
                id = id,
                title = title,
                artist = artist,
                album = track["album"].stringValue().orEmpty(),
                durationMs = (track["duration"]?.jsonPrimitive?.longOrNull ?: 0L) * 1000L,
                youtubeUrl = track["youtubeUrl"].stringValue()?.takeIf(String::isNotBlank),
                localUri = track["matchedLocalUri"].stringValue(),
                metadataJson = track.toString()
            )
        }
    }

    fun withAnalyzerResult(
        track: CatalogTrackEntity,
        youtubeUrl: String?
    ): CatalogTrackEntity {
        val metadata = runCatching { Json.parseToJsonElement(track.metadataJson).jsonObject }
            .getOrElse { throw IllegalArgumentException("No se pudieron leer los metadatos de la pista.", it) }
        val updatedMetadata = JsonObject(
            metadata +
                ("youtubeUrl" to JsonPrimitive(youtubeUrl.orEmpty())) +
                ("ytAttempted" to JsonPrimitive(true))
        )
        return track.copy(
            youtubeUrl = youtubeUrl,
            metadataJson = updatedMetadata.toString()
        )
    }

    fun acceptsSearchResult(trackTitle: String, resultTitle: String): Boolean {
        val wanted = tokens(trackTitle)
        val found = tokens(resultTitle)
        if (wanted.isEmpty() || found.isEmpty()) return false
        val intersection = wanted.intersect(found).size.toFloat()
        val similarity = intersection / wanted.size
        return similarity >= 0.6f
    }

    fun needsYouTubeAnalysis(track: CatalogTrackEntity): Boolean {
        val attempted = runCatching {
            Json.parseToJsonElement(track.metadataJson).jsonObject["ytAttempted"]
                ?.jsonPrimitive?.content?.toBooleanStrictOrNull() == true
        }.getOrDefault(false)
        return track.youtubeUrl.isNullOrBlank() && !attempted
    }

    fun representsLocalTrack(savedTrack: JsonObject, localTrack: TrackEntity): Boolean =
        savedTrack["id"].stringValue() == "local-${localTrack.id}" ||
            savedTrack["matchedLocalUri"].stringValue() == localTrack.uri ||
            (
                savedTrack["title"].stringValue() == localTrack.title &&
                    savedTrack["artist"].stringValue() == localTrack.artist &&
                    savedTrack["matchedFileName"].stringValue() == localTrack.displayName
                )

    private fun JsonElement?.stringValue(): String? =
        if (this == null || this === JsonNull) null else runCatching { jsonPrimitive.content }.getOrNull()

    private fun tokens(value: String): Set<String> =
        value.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
            .toSet()
}
