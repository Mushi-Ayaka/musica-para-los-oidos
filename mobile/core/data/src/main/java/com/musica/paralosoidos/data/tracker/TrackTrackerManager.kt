package com.musica.paralosoidos.data.tracker

import android.content.Context
import com.musica.paralosoidos.data.AppPreferences
import com.musica.paralosoidos.data.CatalogTrackDao
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.DownloadNotificationManager
import com.musica.paralosoidos.data.DownloadStorageManager
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.download.MetadataCleaner
import com.musica.paralosoidos.data.lyrics.LyricsDownloadEngine
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

data class TrackSyncResult(
    val youtubeUrl: String?,
    val lrcContent: String?,
    val hasLrcFile: Boolean
)

class TrackTrackerManager(
    private val context: Context,
    private val catalogTrackDao: CatalogTrackDao,
    private val lyricsEngine: LyricsDownloadEngine,
    private val storageManager: DownloadStorageManager,
    private val notificationManager: DownloadNotificationManager,
    private val preferences: AppPreferences
) {

    suspend fun searchYouTubeMatch(track: TrackEntity): String? = withContext(Dispatchers.IO) {
        val clean = MetadataCleaner.clean(track.title, track.artist)
        val query = "${clean.artist} ${clean.title}".trim()
        if (query.isBlank()) return@withContext null

        try {
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
                "https://www.youtube.com/watch?v=$videoId"
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun searchLrcMatch(track: TrackEntity): String? = withContext(Dispatchers.IO) {
        val clean = MetadataCleaner.clean(track.title, track.artist)
        val durationSec = (track.durationMs / 1000).toInt()
        try {
            lyricsEngine.fetchLyrics(clean.title, clean.artist, durationSec)
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
        // Check in app lyrics storage
        val appLrc = storageManager.getLyricsFile(track.title)
        return appLrc.exists() && appLrc.length() > 0
    }

    suspend fun downloadAndSaveLrc(track: TrackEntity, lrcContent: String): File? = withContext(Dispatchers.IO) {
        try {
            val destFile = if (track.folderPath.isNotBlank()) {
                val nameWithoutExt = track.displayName.substringBeforeLast('.')
                File(track.folderPath, "$nameWithoutExt.lrc")
            } else {
                storageManager.getLyricsFile(track.title)
            }
            lyricsEngine.writeLrcFile(destFile, lrcContent)
            destFile
        } catch (_: Exception) {
            null
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

        if (forceRefresh || ytUrl.isNullOrBlank()) {
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
            hasLrcFile = hasLrcLocally
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
            !hasYt || !hasLrc
        }

        val total = pending.size
        pending.forEachIndexed { index, track ->
            onProgress(index + 1, total)
            runCatching {
                syncSingleTrack(track, forceRefresh = false)
            }
            delay(delayMs)
        }
    }
}
