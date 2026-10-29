package com.musica.paralosoidos.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.room.Room
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.flow.first

class AppContainer(context: Context) {
    private val backupJson = Json { prettyPrint = true }
    private val database = Room.databaseBuilder(
        context,
        MusicDatabase::class.java,
        "musica-para-los-oidos.db"
    ).addMigrations(
        MusicDatabase.MIGRATION_1_2,
        MusicDatabase.MIGRATION_2_3,
        MusicDatabase.MIGRATION_3_4
    ).build()

    val tracks = database.trackDao()
    val catalogTracks = database.catalogTrackDao()
    val downloadSettings = DownloadSettings(context)
    val audioOutputProfileSettings = AudioOutputProfileSettings(context)
    val preferences = AppPreferences(context)
    private val backupState = database.backupStateDao()
    private val resolver = context.contentResolver

    suspend fun refreshLocalAudio() {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val found = mutableListOf<TrackEntity>()

        resolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                if (duration <= 0L) continue
                found += TrackEntity(
                    id = id,
                    title = cursor.getString(titleColumn) ?: "Título desconocido",
                    artist = cursor.getString(artistColumn)
                        ?.takeUnless { it == "<unknown>" }.orEmpty().ifBlank { "Artista desconocido" },
                    album = cursor.getString(albumColumn)
                        ?.takeUnless { it == "<unknown>" }.orEmpty(),
                    durationMs = duration,
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    displayName = cursor.getString(displayNameColumn).orEmpty(),
                    artworkUri = albumId.takeIf { it > 0L }?.let {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            it
                        ).toString()
                    }
                )
            }
        } ?: error("No se pudo consultar la biblioteca multimedia del dispositivo.")

        tracks.replaceAll(found)
        relinkBackupTracks(found)
    }

    suspend fun exportBackupJson(): String {
        val downloadSettingsState = downloadSettings.state.first()
        val outputProfileId = audioOutputProfileSettings.profileId.first()
        val storedData = backupState.getDataJson()?.let {
            runCatching { Json.parseToJsonElement(it).jsonObject }.getOrNull()
        } ?: buildJsonObject { }
        val timestamp = java.time.Instant.now().toString()
        val defaults = buildJsonObject {
            putJsonObject("lrcFilesMap") { }
            putJsonArray("playlists") { }
            putJsonArray("likedSongIds") { }
            putJsonArray("favoriteTitles") { }
            putJsonArray("eqPresets") { }
            putJsonObject("audioSettings") {
                put("volumePercent", 100)
                put("outputProfile", outputProfileId)
                put("playbackSpeed", 1.0)
                put("eqEnabled", true)
                put("selectedEqPresetId", "flat")
            }
            putJsonArray("customStylePresets") { }
            putJsonObject("themeVars") { }
            putJsonArray("monitoredFolders") { }
            put("trackSortOrder", "az")
            put("trackFolderFilter", "all")
            putJsonArray("downloadHistory") { }
            putJsonObject("cloudSyncMeta") {
                put("lastExportedAt", timestamp)
                put("provider", "local")
            }
            putJsonObject("downloadSettings") {
                put("format", downloadSettingsState.format?.key.orEmpty())
                downloadSettingsState.mp3Bitrate?.let { put("mp3Bitrate", it) }
            }
        }
        val storedAudioSettings = storedData["audioSettings"] as? JsonObject ?: JsonObject(emptyMap())
        val defaultAudioSettings = defaults["audioSettings"] as JsonObject
        val audioSettings = JsonObject(
            defaultAudioSettings + storedAudioSettings +
                ("outputProfile" to JsonPrimitive(outputProfileId))
        )
        val data = BackupCodec.portableData(
            JsonObject(
                defaults + storedData +
                    ("audioSettings" to audioSettings) +
                    ("downloadSettings" to buildJsonObject {
                        put("format", downloadSettingsState.format?.key.orEmpty())
                        downloadSettingsState.mp3Bitrate?.let { put("mp3Bitrate", it) }
                    })
            )
        )

        val savedTracks = data["tracks"] as? JsonArray ?: JsonArray(emptyList())
        val storedTracks = storedData["tracks"] as? JsonArray ?: JsonArray(emptyList())
        val savedIds = savedTracks.mapNotNull { item ->
            (item as? JsonObject)?.get("id")?.jsonPrimitive?.content
        }.toSet()
        val indexedTracks = tracks.observeAll().first()
        val currentTracks = indexedTracks.filterNot { track ->
            "local-${track.id}" in savedIds || storedTracks.any { item ->
                (item as? JsonObject)?.let { BackupCodec.representsLocalTrack(it, track) } == true
            }
        }.map { track ->
            buildJsonObject {
                put("id", "local-${track.id}")
                put("title", track.title)
                put("artist", track.artist)
                put("album", track.album)
                put("duration", track.durationMs / 1000)
                put("fileName", track.displayName)
                put("folder", "")
                put("genre", "Desconocido")
                put("mood", "")
                put("cover", "")
                put("plays", 0)
                put("addedAt", "")
                put("youtubeUrl", "")
                put("lyrics", JsonArray(emptyList()))
                put("isFavorite", false)
            }
        }
        val allTracks = JsonArray(savedTracks + currentTracks)
        val outputData = JsonObject(data + ("tracks" to allTracks))
        val envelope = buildJsonObject {
            put("app", "Musica para los Oidos")
            put("version", "1.0.0")
            put("schemaVersion", 3)
            put("createdAt", timestamp)
            putJsonObject("summary") {
                put("totalTracks", allTracks.size)
                put("totalPlaylists", (data["playlists"] as? JsonArray)?.size ?: 0)
                put("totalLikedSongs", (data["likedSongIds"] as? JsonArray)?.size ?: 0)
                put("totalEqPresets", (data["eqPresets"] as? JsonArray)?.size ?: 0)
                put("totalLrcFiles", (data["lrcFilesMap"] as? JsonObject)?.size ?: 0)
                put("totalDownloadsRecorded", (data["downloadHistory"] as? JsonArray)?.size ?: 0)
            }
            put("data", outputData)
        }
        return backupJson.encodeToString(JsonObject.serializer(), envelope)
    }

    suspend fun importBackupJson(json: String): BackupImportSummary {
        val data = BackupCodec.parseData(json)
        val settings = data["downloadSettings"] as? JsonObject
        val format = (settings?.get("format") as? JsonPrimitive)?.contentOrNull
            ?.takeIf(String::isNotBlank)
        val bitrate = (settings?.get("mp3Bitrate") as? JsonPrimitive)?.intOrNull
        downloadSettings.restore(format, bitrate)
        val audioSettings = data["audioSettings"] as? JsonObject
        val outputProfileId = audioSettings?.get("outputProfile")?.jsonPrimitive?.contentOrNull
        audioOutputProfileSettings.restore(outputProfileId)
        val (linkedData, summary) = BackupCodec.linkLocalTracks(data, tracks.observeAll().first())
        backupState.save(BackupStateEntity(dataJson = linkedData.toString()))
        catalogTracks.replaceAll(BackupCodec.toCatalogTracks(linkedData))
        return summary
    }

    suspend fun recordYouTubeAnalysis(trackId: String, youtubeUrl: String?) {
        val current = catalogTracks.observeAll().first()
        val target = current.singleOrNull { it.id == trackId }
            ?: error("La pista que se estaba analizando ya no está en el catálogo.")
        val updated = BackupCodec.withAnalyzerResult(target, youtubeUrl)
        catalogTracks.replaceAll(current.map { if (it.id == trackId) updated else it })

        val storedData = backupState.getDataJson()?.let {
            runCatching { Json.parseToJsonElement(it).jsonObject }.getOrNull()
        } ?: return
        val source = runCatching { Json.parseToJsonElement(target.metadataJson).jsonObject }.getOrNull()
            ?: return
        val sourceId = source["id"]?.jsonPrimitive?.content
        val catalog = storedData["tracks"] as? JsonArray ?: return
        val updatedTracks: List<JsonElement> = catalog.map { element ->
            val item = element as? JsonObject ?: return@map element
            val sameId = if (sourceId != null) {
                item["id"]?.jsonPrimitive?.content == sourceId
            } else {
                item == source
            }
            val sameTrack = item["title"]?.jsonPrimitive?.content == target.title &&
                item["artist"]?.jsonPrimitive?.content.orEmpty() == target.artist
            if (sameId && sameTrack) {
                JsonObject(
                    item +
                        ("youtubeUrl" to JsonPrimitive(youtubeUrl.orEmpty())) +
                        ("ytAttempted" to JsonPrimitive(true))
                )
            } else {
                element
            }
        }
        backupState.save(
            BackupStateEntity(
                dataJson = JsonObject(storedData + ("tracks" to JsonArray(updatedTracks))).toString()
            )
        )
    }

    suspend fun saveTrackYouTubeUrl(track: TrackEntity, youtubeUrl: String?) {
        val existing = catalogTracks.observeAll().first().firstOrNull { it.localUri == track.uri }
        if (existing != null) {
            recordYouTubeAnalysis(existing.id, youtubeUrl)
        } else {
            val cleanUrl = youtubeUrl?.trim()?.takeIf(String::isNotBlank)
            val newEntry = CatalogTrackEntity(
                id = "local-${track.id}",
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                youtubeUrl = cleanUrl,
                localUri = track.uri,
                metadataJson = buildJsonObject {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("youtubeUrl", cleanUrl.orEmpty())
                    put("ytAttempted", true)
                }.toString()
            )
            catalogTracks.upsert(newEntry)
        }
    }

    private suspend fun relinkBackupTracks(localTracks: List<TrackEntity>) {
        val data = backupState.getDataJson()?.let {
            runCatching { Json.parseToJsonElement(it).jsonObject }.getOrNull()
        } ?: return
        val linkedData = BackupCodec.linkLocalTracks(data, localTracks).first
        backupState.save(BackupStateEntity(dataJson = linkedData.toString()))
        catalogTracks.replaceAll(BackupCodec.toCatalogTracks(linkedData))
    }
}
