package com.musica.paralosoidos.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale
import java.util.UUID

@Serializable
internal data class AutoBackupFile(
    val app: String = APP_ID,
    val schemaVersion: Int = SCHEMA_VERSION,
    val exportedAt: Long = 0L,
    val playlists: List<BackupPlaylist> = emptyList(),
    val theme: ThemeSettingsState? = null,
    val eqPresets: List<SavedEqPreset> = emptyList()
) {
    companion object {
        const val APP_ID = "Musica para los Oidos"
        const val SCHEMA_VERSION = 1
    }
}

@Serializable
internal data class BackupPlaylist(
    val id: String = "",
    val name: String = "",
    val tracks: List<BackupTrack> = emptyList()
)

@Serializable
internal data class BackupTrack(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0L,
    val uri: String = "",
    val displayName: String = ""
)

internal object AutoBackupCodec {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(
        playlists: List<MusicPlaylist>,
        theme: ThemeSettingsState,
        eqPresets: List<SavedEqPreset>,
        library: List<TrackEntity>,
        exportedAt: Long
    ): String {
        val tracksById = library.associateBy { it.id }
        val file = AutoBackupFile(
            exportedAt = exportedAt,
            playlists = playlists.map { playlist ->
                BackupPlaylist(
                    id = playlist.id,
                    name = playlist.name,
                    tracks = playlist.trackIds.mapNotNull { tracksById[it] }.map(::toBackupTrack)
                )
            },
            theme = theme,
            eqPresets = eqPresets
        )
        return json.encodeToString(file)
    }

    fun decode(text: String): AutoBackupFile {
        val root = runCatching { json.parseToJsonElement(text).jsonObject }
            .getOrElse { throw IllegalArgumentException("La copia automática no contiene JSON válido.", it) }
        require(root["app"]?.jsonPrimitive?.content == AutoBackupFile.APP_ID) {
            "El archivo no corresponde a una copia de Música para los Oídos."
        }
        require(root["schemaVersion"]?.jsonPrimitive?.intOrNull == AutoBackupFile.SCHEMA_VERSION) {
            "La versión de la copia automática no es compatible."
        }
        require(root["playlists"] is JsonArray) {
            "La copia automática no contiene listas."
        }
        return json.decodeFromString(AutoBackupFile.serializer(), text)
    }

    fun toPlaylists(file: AutoBackupFile, library: List<TrackEntity>): List<MusicPlaylist> =
        file.playlists.mapNotNull { saved ->
            val name = saved.name.trim()
            if (name.isBlank()) return@mapNotNull null
            MusicPlaylist(
                id = saved.id.trim().ifBlank { UUID.randomUUID().toString() },
                name = name,
                trackIds = saved.tracks.mapNotNull { findTrack(it, library)?.id }.distinct()
            )
        }

    fun merge(local: List<MusicPlaylist>, restored: List<MusicPlaylist>): List<MusicPlaylist> {
        val ids = local.map { it.id }.toSet()
        val names = local.map { it.name.trim().lowercase(Locale.ROOT) }.toSet()
        return local + restored.filter { candidate ->
            candidate.id !in ids && candidate.name.trim().lowercase(Locale.ROOT) !in names
        }
    }

    private fun toBackupTrack(track: TrackEntity) = BackupTrack(
        title = track.title,
        artist = track.artist,
        album = track.album,
        durationMs = track.durationMs,
        uri = track.uri,
        displayName = track.displayName
    )

    private fun findTrack(saved: BackupTrack, library: List<TrackEntity>): TrackEntity? {
        library.firstOrNull {
            it.uri == saved.uri && (
                saved.title.isBlank() ||
                    it.title.equals(saved.title, ignoreCase = true) ||
                    (saved.displayName.isNotBlank() && it.displayName == saved.displayName)
                )
        }?.let { return it }
        library.firstOrNull {
            it.displayName == saved.displayName && it.title.equals(saved.title, ignoreCase = true)
        }?.let { return it }

        val title = saved.title.normalized()
        val artist = saved.artist.normalized()
        if (title.isNotBlank()) {
            if (artist.isNotBlank()) {
                library.firstOrNull {
                    it.title.normalized() == title && it.artist.normalized() == artist
                }?.let { return it }
            }
            library.filter { it.title.normalized() == title }.singleOrNull()?.let { return it }
        }
        if (saved.displayName.isNotBlank()) {
            library.filter { it.displayName == saved.displayName }.singleOrNull()?.let { return it }
        }
        return null
    }

    private fun String.normalized(): String = trim()
        .lowercase(Locale.ROOT)
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
}
