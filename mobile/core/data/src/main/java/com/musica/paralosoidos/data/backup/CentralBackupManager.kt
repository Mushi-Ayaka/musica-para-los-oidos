package com.musica.paralosoidos.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.musica.paralosoidos.data.AppPreferences
import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.AudioOutputProfileSettings
import com.musica.paralosoidos.data.AutoBackupCodec
import com.musica.paralosoidos.data.CatalogTrackDao
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.DownloadHistoryDao
import com.musica.paralosoidos.data.DownloadHistoryEntity
import com.musica.paralosoidos.data.DownloadQueueManager
import com.musica.paralosoidos.data.DownloadSettings
import com.musica.paralosoidos.data.DownloadTask
import com.musica.paralosoidos.data.DownloadTaskStatus
import com.musica.paralosoidos.data.TrackDao
import com.musica.paralosoidos.data.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

data class CentralBackupImportResult(
    val playlistCount: Int,
    val trackMetadataCount: Int,
    val missingLinkedTracks: List<TrackMetadataBackup>
)

class CentralBackupManager(
    private val context: Context,
    private val trackDao: TrackDao,
    private val catalogTrackDao: CatalogTrackDao,
    private val downloadHistoryDao: DownloadHistoryDao,
    private val downloadSettings: DownloadSettings,
    private val preferences: AppPreferences,
    private val audioOutputProfileSettings: AudioOutputProfileSettings,
    private val downloadQueueManager: DownloadQueueManager
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportBundle(
        currentPlayback: PlaybackBackupState? = null
    ): String = withContext(Dispatchers.IO) {
        val catalog = catalogTrackDao.observeAll().first()
        val localTracks = trackDao.observeAll().first()
        val localTracksByUri = localTracks.associateBy { it.uri }

        val tracksMetadata = catalog.map { cat ->
            TrackMetadataBackup(
                title = cat.title,
                artist = cat.artist,
                album = cat.album,
                durationMs = cat.durationMs,
                uri = cat.localUri.orEmpty(),
                youtubeUrl = cat.youtubeUrl,
                lrcPath = cat.lrcPath,
                lrcSynced = cat.lrcSynced
            )
        }

        val playlists = preferences.playlists.first().map { pl ->
            val plTracks = pl.trackIds.mapNotNull { id -> localTracks.firstOrNull { it.id == id } }
            com.musica.paralosoidos.data.BackupPlaylist(
                id = pl.id,
                name = pl.name,
                tracks = plTracks.map {
                    com.musica.paralosoidos.data.BackupTrack(
                        title = it.title,
                        artist = it.artist,
                        album = it.album,
                        durationMs = it.durationMs,
                        uri = it.uri,
                        displayName = it.displayName
                    )
                },
                coverUri = pl.coverUri
            )
        }

        val hiddenFolders = preferences.hiddenFolders.first()
        val downloadHistory = downloadHistoryDao.observeAll().first().map { h ->
            DownloadHistoryItemBackup(
                id = h.id,
                videoId = h.videoId,
                title = h.title,
                channel = h.channel,
                format = h.format,
                bitrate = h.bitrate,
                filePath = h.filePath,
                downloadedAt = h.downloadedAt
            )
        }

        val dlSettings = downloadSettings.state.first()
        val dlConcurrency = preferences.downloadConcurrency.first()
        val dlRetry = preferences.downloadRetryCount.first()

        val downloadConfig = DownloadConfigBackup(
            defaultFormat = dlSettings.format?.name ?: "MP3",
            defaultBitrate = dlSettings.mp3Bitrate ?: 320,
            syncLyrics = dlSettings.syncLyrics,
            concurrency = dlConcurrency,
            retryCount = dlRetry
        )

        val audioEq = AudioEqBackupState(
            mediaVolume = preferences.mediaVolume.first(),
            speakerBoost = preferences.speakerBoost.first(),
            eqEnabled = preferences.eqEnabled.first(),
            activeEqPreset = preferences.activeEqPreset.first(),
            customEqPresets = preferences.customEqPresets.first(),
            eqBands = preferences.eqBands.first(),
            bassBoost = preferences.bassBoost.first(),
            virtualizer = preferences.virtualizer.first(),
            outputProfileId = audioOutputProfileSettings.profileId.first()
        )

        val trackerConfig = TrackerConfigBackup(
            circleColorHex = preferences.trackerCircleColor.first(),
            frequency = preferences.trackerFrequency.first(),
            intensity = preferences.trackerIntensity.first()
        )

        val theme = preferences.themeSettings.first()
        val lyricsSpeed = preferences.lyricsScrollSpeed.first()

        val appearance = AppearanceBackupState(
            themeSettings = theme,
            lyricsScrollSpeed = lyricsSpeed
        )

        val bundle = AppDataBackupBundle(
            exportedAt = System.currentTimeMillis(),
            playback = currentPlayback,
            tracks = tracksMetadata,
            playlists = playlists,
            hiddenFolders = hiddenFolders,
            downloadHistory = downloadHistory,
            downloadConfig = downloadConfig,
            audioEq = audioEq,
            trackerConfig = trackerConfig,
            appearance = appearance,
            autoBackupInterval = preferences.autoBackupInterval.first()
        )

        json.encodeToString(bundle)
    }

    suspend fun importBundle(jsonString: String): CentralBackupImportResult = withContext(Dispatchers.IO) {
        val bundle = json.decodeFromString<AppDataBackupBundle>(jsonString)

        // 1. Playlists
        val localTracks = trackDao.observeAll().first()
        val restoredPlaylists = bundle.playlists.mapNotNull { saved ->
            val name = saved.name.trim()
            if (name.isBlank()) return@mapNotNull null
            com.musica.paralosoidos.data.MusicPlaylist(
                id = saved.id.ifBlank { java.util.UUID.randomUUID().toString() },
                name = name,
                trackIds = saved.tracks.mapNotNull { t ->
                    localTracks.firstOrNull { it.uri == t.uri || it.title.equals(t.title, ignoreCase = true) }?.id
                }.distinct(),
                coverUri = saved.coverUri
            )
        }
        val currentPlaylists = preferences.playlists.first()
        val mergedPlaylists = AutoBackupCodec.merge(currentPlaylists, restoredPlaylists)
        preferences.savePlaylists(mergedPlaylists)

        // 2. Hidden folders
        if (bundle.hiddenFolders.isNotEmpty()) {
            preferences.saveHiddenFolders(bundle.hiddenFolders)
        }

        // 3. Audio & EQ
        bundle.audioEq?.let { eq ->
            preferences.saveMediaVolume(eq.mediaVolume)
            preferences.saveSpeakerBoost(eq.speakerBoost)
            preferences.saveEqEnabled(eq.eqEnabled)
            if (eq.activeEqPreset.isNotBlank()) preferences.saveActiveEqPreset(eq.activeEqPreset)
            if (eq.customEqPresets.isNotEmpty()) preferences.saveCustomEqPresets(eq.customEqPresets)
            if (eq.eqBands.isNotEmpty()) preferences.saveEqBands(eq.eqBands)
            preferences.saveBassBoost(eq.bassBoost)
            preferences.saveVirtualizer(eq.virtualizer)
            eq.outputProfileId?.let { audioOutputProfileSettings.save(it) }
        }

        // 4. Tracker Config
        bundle.trackerConfig?.let { tr ->
            preferences.saveTrackerCircleColor(tr.circleColorHex)
            preferences.saveTrackerFrequency(tr.frequency)
            preferences.saveTrackerIntensity(tr.intensity)
        }

        // 5. Download Config
        bundle.downloadConfig?.let { dl ->
            val format = runCatching { AudioOutputFormat.valueOf(dl.defaultFormat) }.getOrDefault(AudioOutputFormat.MP3)
            downloadSettings.save(format, dl.defaultBitrate)
            downloadSettings.saveSyncLyrics(dl.syncLyrics)
            preferences.saveDownloadConcurrency(dl.concurrency)
            preferences.saveDownloadRetryCount(dl.retryCount)
        }

        // 6. Appearance
        bundle.appearance?.let { app ->
            app.themeSettings?.let { preferences.saveThemeSettings(it) }
            preferences.saveLyricsScrollSpeed(app.lyricsScrollSpeed)
        }

        // 7. Auto Backup Interval
        preferences.saveAutoBackupInterval(bundle.autoBackupInterval)

        // 8. Catalog Tracks & Track Linking
        val localUris = localTracks.map { it.uri }.toSet()
        val localTitles = localTracks.map { it.title.trim().lowercase() }.toSet()

        val missingLinkedTracks = mutableListOf<TrackMetadataBackup>()

        bundle.tracks.forEach { trk ->
            val isLocal = trk.uri in localUris || trk.title.trim().lowercase() in localTitles
            if (!isLocal && !trk.youtubeUrl.isNullOrBlank()) {
                missingLinkedTracks.add(trk)
            }

            val matchingLocal = localTracks.firstOrNull { it.uri == trk.uri || it.title.equals(trk.title, ignoreCase = true) }
            val entry = CatalogTrackEntity(
                id = "import-${trk.title.hashCode().toUInt()}",
                title = trk.title,
                artist = trk.artist,
                album = trk.album,
                durationMs = trk.durationMs,
                youtubeUrl = trk.youtubeUrl,
                localUri = matchingLocal?.uri ?: trk.uri,
                metadataJson = "{}",
                lrcPath = trk.lrcPath,
                lrcSynced = trk.lrcSynced
            )
            catalogTrackDao.upsert(entry)
        }

        CentralBackupImportResult(
            playlistCount = mergedPlaylists.size,
            trackMetadataCount = bundle.tracks.size,
            missingLinkedTracks = missingLinkedTracks
        )
    }

    fun enqueueMissingTracks(missingTracks: List<TrackMetadataBackup>) {
        missingTracks.forEach { track ->
            val ytUrl = track.youtubeUrl.orEmpty()
            val videoId = when {
                ytUrl.contains("v=") -> ytUrl.substringAfter("v=").substringBefore("&").substringBefore("?")
                ytUrl.contains("youtu.be/") -> ytUrl.substringAfter("youtu.be/").substringBefore("?")
                else -> track.title.hashCode().toString()
            }

            val task = DownloadTask(
                id = "restore_${videoId}_${System.currentTimeMillis()}",
                videoId = videoId,
                title = track.title,
                channel = track.artist,
                thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                format = AudioOutputFormat.MP3,
                bitrate = 320,
                syncLyrics = track.lrcSynced,
                status = DownloadTaskStatus.QUEUED
            )
            downloadQueueManager.enqueue(task)
        }
    }

    fun createGoogleDriveIntent(backupFileUri: Uri): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, backupFileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - Musica para los Oidos")
            setPackage("com.google.android.apps.docs")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pm = context.packageManager
        return if (shareIntent.resolveActivity(pm) != null) {
            shareIntent
        } else {
            // Fallback: system chooser
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, backupFileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - Musica para los Oidos")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Guardar copia en la nube o compartir"
            )
        }
    }

    suspend fun exportZipBundle(
        outputStream: java.io.OutputStream,
        currentPlayback: PlaybackBackupState? = null
    ) = withContext(Dispatchers.IO) {
        val jsonString = exportBundle(currentPlayback)
        val zipOut = java.util.zip.ZipOutputStream(outputStream)

        // 1. backup.json
        val jsonEntry = java.util.zip.ZipEntry("backup.json")
        zipOut.putNextEntry(jsonEntry)
        zipOut.write(jsonString.toByteArray(Charsets.UTF_8))
        zipOut.closeEntry()

        // 2. Wallpapers
        val theme = preferences.themeSettings.first()
        val allWallpapers = mutableSetOf<String>()
        if (theme.wallpaperUri.isNotBlank()) allWallpapers.add(theme.wallpaperUri)
        theme.customPresets.forEach {
            if (it.settings.wallpaperUri.isNotBlank()) allWallpapers.add(it.settings.wallpaperUri)
        }
        for (wUri in allWallpapers) {
            val fPath = if (wUri.startsWith("file://")) wUri.removePrefix("file://") else wUri
            val file = File(fPath)
            if (file.exists() && file.isFile) {
                val entry = java.util.zip.ZipEntry("wallpapers/${file.name}")
                zipOut.putNextEntry(entry)
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }

        // 3. Playlist covers
        val playlists = preferences.playlists.first()
        for (pl in playlists) {
            val cUri = pl.coverUri
            if (!cUri.isNullOrBlank()) {
                val fPath = if (cUri.startsWith("file://")) cUri.removePrefix("file://") else cUri
                val file = File(fPath)
                if (file.exists() && file.isFile) {
                    val entry = java.util.zip.ZipEntry("playlist_covers/${file.name}")
                    zipOut.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
        }

        zipOut.finish()
    }

    suspend fun importBundleStream(inputStream: java.io.InputStream): CentralBackupImportResult = withContext(Dispatchers.IO) {
        val buffered = if (inputStream.markSupported()) inputStream else java.io.BufferedInputStream(inputStream)
        buffered.mark(4)
        val header = ByteArray(4)
        val readBytes = buffered.read(header)
        buffered.reset()

        val isZip = readBytes == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()

        if (isZip) {
            val zipIn = java.util.zip.ZipInputStream(buffered)
            var jsonContent: String? = null
            val coversDir = File(context.filesDir, "playlist_covers").apply { mkdirs() }

            var entry = zipIn.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name == "backup.json") {
                    jsonContent = zipIn.bufferedReader(Charsets.UTF_8).readText()
                } else if (name.startsWith("wallpapers/")) {
                    val fName = name.substringAfterLast("/")
                    if (fName.isNotBlank()) {
                        val destFile = File(context.filesDir, fName)
                        destFile.outputStream().use { zipIn.copyTo(it) }
                    }
                } else if (name.startsWith("playlist_covers/")) {
                    val fName = name.substringAfterLast("/")
                    if (fName.isNotBlank()) {
                        val destFile = File(coversDir, fName)
                        destFile.outputStream().use { zipIn.copyTo(it) }
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }

            if (jsonContent != null) {
                importBundle(jsonContent)
            } else {
                error("El archivo ZIP no contiene backup.json válido.")
            }
        } else {
            val jsonContent = buffered.bufferedReader(Charsets.UTF_8).readText()
            importBundle(jsonContent)
        }
    }

    suspend fun importThemes(inputStream: java.io.InputStream): Int = withContext(Dispatchers.IO) {
        val buffered = if (inputStream.markSupported()) inputStream else java.io.BufferedInputStream(inputStream)
        buffered.mark(4)
        val header = ByteArray(4)
        val readBytes = buffered.read(header)
        buffered.reset()

        val isZip = readBytes == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()

        val jsonString: String = if (isZip) {
            val zipIn = java.util.zip.ZipInputStream(buffered)
            var extractedJson: String? = null
            var entry = zipIn.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name == "backup.json" || name.endsWith(".json")) {
                    extractedJson = zipIn.bufferedReader(Charsets.UTF_8).readText()
                } else if (name.startsWith("wallpapers/")) {
                    val fName = name.substringAfterLast("/")
                    if (fName.isNotBlank()) {
                        val destFile = File(context.filesDir, fName)
                        destFile.outputStream().use { zipIn.copyTo(it) }
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            extractedJson ?: error("No se encontró archivo de temas en el ZIP.")
        } else {
            buffered.bufferedReader(Charsets.UTF_8).readText()
        }

        val currentTheme = preferences.themeSettings.first()
        val importedPresets = mutableListOf<com.musica.paralosoidos.data.SavedThemePreset>()

        runCatching {
            val bundle = json.decodeFromString<AppDataBackupBundle>(jsonString)
            bundle.appearance?.themeSettings?.let { ts ->
                importedPresets.addAll(ts.customPresets)
                importedPresets.add(
                    com.musica.paralosoidos.data.SavedThemePreset(
                        id = "imported-${System.currentTimeMillis()}",
                        name = "Tema importado",
                        settings = ts.copy(customPresets = emptyList())
                    )
                )
            }
        }.onFailure {
            runCatching {
                val presets = json.decodeFromString<List<com.musica.paralosoidos.data.SavedThemePreset>>(jsonString)
                importedPresets.addAll(presets)
            }.onFailure {
                val single = json.decodeFromString<com.musica.paralosoidos.data.ThemeSettingsState>(jsonString)
                importedPresets.addAll(single.customPresets)
            }
        }

        val existingIds = currentTheme.customPresets.map { it.id }.toSet()
        val uniqueNew = importedPresets.filterNot { it.id in existingIds }
        val merged = currentTheme.customPresets + uniqueNew
        preferences.saveThemeSettings(currentTheme.copy(customPresets = merged))
        uniqueNew.size
    }

}
