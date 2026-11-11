package com.musica.paralosoidos.data

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppContainer(context: Context) {
    private val backupJson = Json { prettyPrint = true }
    private val database = Room.databaseBuilder(
        context,
        MusicDatabase::class.java,
        "musica-para-los-oidos.db"
    ).addMigrations(
        MusicDatabase.MIGRATION_1_2,
        MusicDatabase.MIGRATION_2_3,
        MusicDatabase.MIGRATION_3_4,
        MusicDatabase.MIGRATION_4_5,
        MusicDatabase.MIGRATION_5_6,
        MusicDatabase.MIGRATION_6_7
    ).fallbackToDestructiveMigration().build()

    val tracks = database.trackDao()
    val catalogTracks = database.catalogTrackDao()
    val libraryRepository by lazy { LibraryRepository(this) }
    val downloadSettings = DownloadSettings(context)
    val audioOutputProfileSettings = AudioOutputProfileSettings(context)
    val preferences = AppPreferences(context)
    val autoBackup = AutoBackupManager(context, preferences, tracks)
    val youtubeDLRuntime by lazy { com.musica.paralosoidos.data.download.YoutubeDLRuntime(appContext) }
    val updateRepository by lazy { com.musica.paralosoidos.data.update.UpdateRepository(appContext, preferences) }
    val networkMonitor: NetworkMonitor by lazy { ConnectivityNetworkMonitor(appContext) }
    val downloadNotificationManager by lazy { DownloadNotificationManager(appContext) }
    private val okHttpClient: okhttp3.OkHttpClient by lazy { okhttp3.OkHttpClient() }
    val downloadStorageManager by lazy { DownloadStorageManager(appContext, downloadSettings) }
    val lyricsDownloadEngine by lazy { com.musica.paralosoidos.data.lyrics.LyricsDownloadEngine(okHttpClient) }
    val audioDownloadEngine by lazy {
        com.musica.paralosoidos.data.download.AudioDownloadEngine(appContext, youtubeDLRuntime)
    }
    val downloadQueueManager by lazy {
        DownloadQueueManager(
            context = appContext,
            notificationManager = downloadNotificationManager,
            storageManager = downloadStorageManager,
            audioDownloadEngine = audioDownloadEngine,
            lyricsDownloadEngine = lyricsDownloadEngine,
            downloadHistoryDao = downloadHistoryDao,
            onDownloadComplete = { _ ->
                runCatching { refreshLocalAudio(force = true) }
            }
        )
    }

    val trackTrackerManager by lazy {
        com.musica.paralosoidos.data.tracker.TrackTrackerManager(
            context = appContext,
            catalogTrackDao = catalogTracks,
            lyricsEngine = lyricsDownloadEngine,
            storageManager = downloadStorageManager,
            notificationManager = downloadNotificationManager,
            preferences = preferences,
            youtubeDLRuntime = youtubeDLRuntime
        )
    }

    val centralBackupManager by lazy {
        com.musica.paralosoidos.data.backup.CentralBackupManager(
            context = appContext,
            trackDao = tracks,
            catalogTrackDao = catalogTracks,
            downloadHistoryDao = downloadHistoryDao,
            downloadSettings = downloadSettings,
            preferences = preferences,
            audioOutputProfileSettings = audioOutputProfileSettings,
            downloadQueueManager = downloadQueueManager
        )
    }

    private val appContext = context.applicationContext
    val downloadHistoryDao = database.downloadHistoryDao()
    private val backupState = database.backupStateDao()
    private val resolver = appContext.contentResolver
    private val libraryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var libraryRefreshJob: Job? = null
    @Volatile private var lastLibraryRefreshAt = 0L

    init {
        libraryScope.launch(Dispatchers.IO) {
            try {
                youtubeDLRuntime.ensureReady()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                android.util.Log.e("AppContainer", "Fallo preparando yt-dlp", error)
            }
            runCatching { refreshLocalAudio(force = true) }
        }
        autoBackup.start()
        appContext.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    scheduleLibraryRefresh()
                }
            }
        )
    }

    private fun scheduleLibraryRefresh() {
        libraryRefreshJob?.cancel()
        libraryRefreshJob = libraryScope.launch {
            delay(MEDIA_STORE_DEBOUNCE_MS)
            runCatching { refreshLocalAudio(force = true) }
        }
    }

    suspend fun verifyAndSyncAudioMismatch() {
        withContext(Dispatchers.IO) {
            val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            var mediaStoreCount = 0
            runCatching {
                resolver.query(collection, arrayOf(MediaStore.Audio.Media._ID), selection, null, null)?.use {
                    mediaStoreCount = it.count
                }
            }
            val dbCount = tracks.count()
            if (mediaStoreCount != dbCount) {
                refreshLocalAudio(force = true)
            }
        }
    }

    suspend fun refreshLocalAudio(force: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastLibraryRefreshAt < LIBRARY_REFRESH_INTERVAL_MS) return
        withContext(Dispatchers.IO) {
            val found = queryLocalAudio()
            if (tracks.observeAll().first() != found) {
                tracks.replaceAll(found)
            }
            relinkBackupTracks(found)
        }
        lastLibraryRefreshAt = SystemClock.elapsedRealtime()
        runCatching { autoBackup.restoreIfNeeded() }
    }

    private fun queryLocalAudio(): List<TrackEntity> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val hasRelPath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val projection = if (hasRelPath) {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.RELATIVE_PATH
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
            )
        }
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
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

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

                    },

                                        folderPath = run {
                        val relColumn = if (hasRelPath) cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
                        val rawPath = if (dataColumn >= 0) cursor.getString(dataColumn) else null
                        val relPath = if (relColumn >= 0) cursor.getString(relColumn) else null
                        when {
                            !relPath.isNullOrBlank() -> relPath.trim().trimEnd('/')
                            !rawPath.isNullOrBlank() -> java.io.File(rawPath).parent.orEmpty()
                            else -> "Almacenamiento interno"
                        }
                    },
                    folderName = run {
                        val relColumn = if (hasRelPath) cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
                        val rawPath = if (dataColumn >= 0) cursor.getString(dataColumn) else null
                        val relPath = if (relColumn >= 0) cursor.getString(relColumn) else null
                        when {
                            !relPath.isNullOrBlank() -> relPath.trim().trimEnd('/').substringAfterLast('/').ifBlank { "Música" }
                            !rawPath.isNullOrBlank() -> java.io.File(rawPath).parentFile?.name.orEmpty().ifBlank { "Música" }
                            else -> "Música"
                        }
                    }
                )
            }
        } ?: error("No se pudo consultar la biblioteca multimedia del dispositivo.")

        return found
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

    suspend fun saveTrackLinks(track: TrackEntity, youtubeUrl: String?, lrcPath: String?, lrcSynced: Boolean) {
        val cleanUrl = youtubeUrl?.trim()?.takeIf(String::isNotBlank)
        val cleanLrc = lrcPath?.trim()?.takeIf(String::isNotBlank)
        val existing = catalogTracks.observeAll().first().firstOrNull { it.localUri == track.uri }
        if (existing != null) {
            val updated = existing.copy(
                youtubeUrl = cleanUrl,
                lrcPath = cleanLrc,
                lrcSynced = lrcSynced
            )
            catalogTracks.upsert(updated)
        } else {
            val newEntry = CatalogTrackEntity(
                id = "local-${track.id}",
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                youtubeUrl = cleanUrl,
                localUri = track.uri,
                lrcPath = cleanLrc,
                lrcSynced = lrcSynced,
                metadataJson = buildJsonObject {
                    put("id", track.id)
                    put("title", track.title)
                    put("artist", track.artist)
                    put("album", track.album)
                    put("youtubeUrl", cleanUrl.orEmpty())
                    put("lrcPath", cleanLrc.orEmpty())
                    put("lrcSynced", lrcSynced)
                    put("ytAttempted", true)
                }.toString()
            )
            catalogTracks.upsert(newEntry)
        }
    }

    private suspend fun relinkBackupTracks(localTracks: List<TrackEntity>) {
        val storedJson = backupState.getDataJson() ?: return
        val data = runCatching { Json.parseToJsonElement(storedJson).jsonObject }.getOrNull() ?: return
        val linkedData = BackupCodec.linkLocalTracks(data, localTracks).first
        val linkedJson = linkedData.toString()
        if (linkedJson != storedJson) {
            backupState.save(BackupStateEntity(dataJson = linkedJson))
        }
        val catalog = BackupCodec.toCatalogTracks(linkedData)
        if (catalogTracks.observeAll().first() != catalog) {
            catalogTracks.replaceAll(catalog)
        }
    }
}

private const val LIBRARY_REFRESH_INTERVAL_MS = 60_000L
private const val MEDIA_STORE_DEBOUNCE_MS = 1_500L
