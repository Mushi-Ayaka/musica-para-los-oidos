package com.musica.paralosoidos.data.tracker

import android.content.Context
import com.musica.paralosoidos.data.AppPreferences
import com.musica.paralosoidos.data.CatalogTrackDao
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.DownloadNotificationManager
import com.musica.paralosoidos.data.DownloadStorageManager
import com.musica.paralosoidos.data.DownloadSearchResult
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.download.MetadataCleaner
import com.musica.paralosoidos.data.lyrics.LyricsDownloadEngine
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class TrackSyncResult(
    val youtubeUrl: String?,
    val lrcContent: String?,
    val hasLrcFile: Boolean,
    val skippedAsSuspiciousTitle: Boolean = false
)

class TrackTrackerManager(
    private val context: Context,
    private val catalogTrackDao: CatalogTrackDao,
    private val lyricsEngine: LyricsDownloadEngine,
    private val storageManager: DownloadStorageManager,
    private val notificationManager: DownloadNotificationManager,
    private val preferences: AppPreferences,
    private val youtubeDLRuntime: com.musica.paralosoidos.data.download.YoutubeDLRuntime
) {

    suspend fun searchYouTubeCandidate(
        track: TrackEntity,
        allowSuspiciousTitle: Boolean = false
    ): DownloadSearchResult? = withContext(Dispatchers.IO) {
        if (!allowSuspiciousTitle && !isMusicTitle(track.title)) return@withContext null
        val clean = MetadataCleaner.clean(track.title, track.artist)
        val query = "${clean.artist} ${clean.title}".trim()
        if (query.isBlank()) return@withContext null

        // 1. Scraping HTTP instantaneo (sin esperar Python)
        val scraped = com.musica.paralosoidos.data.download.YouTubeSearchScraper.search(query, maxResults = 1)
        scraped.firstOrNull()?.let { return@withContext it }

        // 2. Respaldo mediante yt-dlp local
        try {
            youtubeDLRuntime.ensureReady()
            val request = YoutubeDLRequest("ytsearch1:$query").apply {
                addOption("--dump-json")
                addOption("--no-playlist")
            }
            val response = YoutubeDL.getInstance().execute(request)
            val output = response.out.trim()
            val line = output.lines().firstOrNull { it.trim().startsWith("{") } ?: return@withContext null
            val json = JSONObject(line)
            val videoId = json.optString("id", "")
            if (videoId.isNotBlank()) {
                val title = json.optString("title", "Sin título")
                DownloadSearchResult(
                    id = videoId,
                    title = title,
                    channel = json.optString("channel", "Desconocido"),
                    durationSeconds = json.optInt("duration", 0),
                    thumbnailUrl = json.optString(
                        "thumbnail",
                        "https://img.youtube.com/vi/$videoId/mqdefault.jpg"
                    )
                )
            } else null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            android.util.Log.w("TrackTrackerManager", "Error buscando YT match para ${track.title}", e)
            null
        }
    }

    suspend fun searchYouTubeMatch(track: TrackEntity): String? =
        searchYouTubeCandidate(track)?.let { "https://www.youtube.com/watch?v=${it.id}" }

    suspend fun searchLrcMatch(track: TrackEntity): String? = withContext(Dispatchers.IO) {
        val clean = MetadataCleaner.clean(track.title, track.artist)
        val durationSec = (track.durationMs / 1000).toInt()
        try {
            lyricsEngine.fetchLyrics(clean.title, clean.artist, durationSec)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    fun hasLocalLrcFile(track: TrackEntity): Boolean {
        // Check alongside the track file
        if (track.folderPath.isNotBlank()) {
            val nameWithoutExt = track.displayName.substringBeforeLast('.')
            val candidate = File(track.folderPath, "$nameWithoutExt.lrc")
            if (candidate.exists() && candidate.length() > 0) return true
        }
        
        // Check in user's configured lyrics folder (SAF storage)
        // Note: We cannot access Flow.value directly here, so we'll skip SAF check in this sync function
        // For now, we'll rely on the catalog entry which should have the path if LRC was downloaded via SAF
        
        // Check in app lyrics storage
        val appLrc = storageManager.getLyricsFile(track.title)
        return appLrc.exists() && appLrc.length() > 0
    }

    suspend fun downloadAndSaveLrc(track: TrackEntity, lrcContent: String): File? = withContext(Dispatchers.IO) {
        try {
            // 1. Intentar usar el directorio configurado por el usuario (SAF)
            val lyricsFolderUri = preferences.lyricsFolderUri.first()
            if (!lyricsFolderUri.isNullOrBlank()) {
                val success = saveLrcToUserFolder(track, lrcContent, lyricsFolderUri)
                if (success) {
                    // Para SAF, guardamos la URI en el catálogo
                    val safPath = "saf:$lyricsFolderUri/${sanitizeFileName(track.title)}.lrc"
                    updateCatalogWithLrcPath(track, safPath)
                    // Retornar un archivo de referencia simbólico
                    return@withContext File(safPath)
                }
            }
            
            // 2. Fallback: si no hay directorio configurado o falla, usar directorio junto al audio
            val destFile = if (track.folderPath.isNotBlank()) {
                val nameWithoutExt = track.displayName.substringBeforeLast('.')
                File(track.folderPath, "$nameWithoutExt.lrc")
            } else {
                // 3. Último fallback: directorio de la app
                storageManager.getLyricsFile(track.title)
            }
            
            lyricsEngine.writeLrcFile(destFile, lrcContent)
            updateCatalogWithLrcPath(track, destFile.absolutePath)
            destFile
        } catch (_: Exception) {
            null
        }
    }
    
    private suspend fun saveLrcToUserFolder(
        track: TrackEntity, 
        lrcContent: String, 
        folderUri: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val treeUri = android.net.Uri.parse(folderUri)
            val documentId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
            val documentUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(
                treeUri, 
                documentId
            )
            
            // Crear archivo en el directorio SAF
            val fileName = "${sanitizeFileName(track.title)}.lrc"
            val outputUri = android.provider.DocumentsContract.createDocument(
                contentResolver,
                documentUri,
                "text/plain",
                fileName
            ) ?: return@withContext false
            
            // Escribir contenido
            contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    writer.write(lrcContent)
                }
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("TrackTrackerManager", "Error guardando LRC en directorio SAF: ${e.message}")
            false
        }
    }
    
    private fun sanitizeFileName(title: String): String {
        return title.replace(Regex("""[<>:"/\\|?*]"""), "_")
            .replace(Regex("""\s+"""), "_")
            .trim('_')
    }
    
    private suspend fun updateCatalogWithLrcPath(track: TrackEntity, absolutePath: String) {
        val existing = catalogTrackDao.observeAll().first().firstOrNull { it.localUri == track.uri }
        if (existing != null) {
            catalogTrackDao.upsert(existing.copy(lrcPath = absolutePath, lrcSynced = true))
        } else {
            catalogTrackDao.upsert(
                CatalogTrackEntity(
                    id = "local-${track.id}",
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationMs = track.durationMs,
                    youtubeUrl = null,
                    localUri = track.uri,
                    metadataJson = "{}",
                    lrcPath = absolutePath,
                    lrcSynced = true
                )
            )
        }
    }

    suspend fun syncSingleTrack(
        track: TrackEntity,
        forceRefresh: Boolean = false
    ): TrackSyncResult = withContext(Dispatchers.IO) {
        val catalogList = catalogTrackDao.observeAll().first()
        val existing = catalogList.firstOrNull { it.localUri == track.uri }

        val hasLrcLocally = hasLocalLrcFile(track)
        var ytUrl = existing?.youtubeUrl
        var lrcText: String? = null
        var lrcSynced = existing?.lrcSynced ?: false
        val skippedAsSuspiciousTitle = !isMusicTitle(track.title)

        if (!skippedAsSuspiciousTitle && (forceRefresh || ytUrl.isNullOrBlank())) {
            val match = searchYouTubeMatch(track)
            if (!match.isNullOrBlank()) {
                ytUrl = match
            }
        }

        if (forceRefresh || (!hasLrcLocally && !lrcSynced)) {
            val lyrics = searchLrcMatch(track)
            if (!lyrics.isNullOrBlank()) {
                lrcText = lyrics
                lrcSynced = true
                if (!hasLrcLocally) {
                    // Notify user that lyrics are available
                    notificationManager.showLrcAvailableNotification(track.id.toString(), track.title)
                }
            }
        }

        // Save into catalog
        val updatedEntry = CatalogTrackEntity(
            id = existing?.id ?: "local-${track.id}",
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            youtubeUrl = ytUrl,
            localUri = track.uri,
            metadataJson = existing?.metadataJson ?: "{}",
            lrcPath = if (hasLrcLocally) "local" else existing?.lrcPath,
            lrcSynced = lrcSynced || hasLrcLocally
        )
        catalogTrackDao.upsert(updatedEntry)

        TrackSyncResult(
            youtubeUrl = ytUrl,
            lrcContent = lrcText,
            hasLrcFile = hasLrcLocally,
            skippedAsSuspiciousTitle = skippedAsSuspiciousTitle
        )
    }

    suspend fun runAutoTracking(
        tracks: List<TrackEntity>,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val intensity = preferences.trackerIntensity.first()
        val delayMs = when (intensity) {
            "LOW" -> 2000L
            "HIGH" -> 50L
            else -> 500L
        }

        val catalogMap = catalogTrackDao.observeAll().first().associateBy { it.localUri.orEmpty() }

        // Find tracks missing either YouTube or LRC
        val pending = tracks.filter { track ->
            val cat = catalogMap[track.uri]
            val hasYt = !cat?.youtubeUrl.isNullOrBlank()
            val hasLrc = (cat?.lrcSynced == true) || hasLocalLrcFile(track)
            (!hasYt || !hasLrc) && isMusicTitle(track.title)
        }

        val total = pending.size
        pending.forEachIndexed { index, track ->
            onProgress(index + 1, total)
            try {
                syncSingleTrack(track, forceRefresh = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                android.util.Log.w("TrackTrackerManager", "No se pudo sincronizar ${track.title}", error)
            }
            delay(delayMs)
        }
    }

    companion object {
        private val NUMERIC_ONLY = Regex("""^\d+$""")
        private val SHORT_ALPHANUMERIC = Regex("""^[A-Za-z0-9]{4,16}$""")
        private val HAS_SPACE_OR_COMMON_SEPARATOR = Regex("""[\s\-_.]""")
        // Prefijos de archivos generados automaticamente (WhatsApp, grabadoras, llamadas)
        private val GENERATED_FILE_PREFIXES = Regex(
            """^(AUD|PTT|VID|IMG|DOC|AUDIO|VOICE|REC|Recording|record|call_rec|llamada|grabacion|grabación|note|NOTE)[-_\d]""",
            RegexOption.IGNORE_CASE
        )
        // Patron de fecha como nombre: 20260629, 2026-06-29, 26062906, etc.
        private val DATE_PATTERN = Regex("""^\d{6,8}$""")
        // Patron alfanumerico con guion y numeros: AUD-20260629-WA0090
        private val WHATSAPP_PATTERN = Regex(
            """^[A-Z]{2,5}-\d{8}-[A-Z]{2}\d{4}$""",
            RegexOption.IGNORE_CASE
        )
        fun isMusicTitle(title: String): Boolean {
            val value = title.trim()
            if (value.length < 3) return false
            if (NUMERIC_ONLY.matches(value)) return false
            if (DATE_PATTERN.matches(value)) return false
            if (WHATSAPP_PATTERN.matches(value)) return false
            if (GENERATED_FILE_PREFIXES.containsMatchIn(value)) return false
            if (SHORT_ALPHANUMERIC.matches(value) &&
                !HAS_SPACE_OR_COMMON_SEPARATOR.containsMatchIn(value)
            ) return false
            return true
        }
    }
}
