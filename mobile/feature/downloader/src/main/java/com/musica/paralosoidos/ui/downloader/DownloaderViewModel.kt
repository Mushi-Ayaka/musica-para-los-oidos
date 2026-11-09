package com.musica.paralosoidos.ui.downloader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.DownloadHistoryDao
import com.musica.paralosoidos.data.DownloadHistoryEntity
import com.musica.paralosoidos.data.DownloadQueueManager
import com.musica.paralosoidos.data.DownloadSearchResult
import com.musica.paralosoidos.data.DownloadSettings
import com.musica.paralosoidos.data.DownloadSettingsState
import com.musica.paralosoidos.data.DownloadTask
import com.musica.paralosoidos.data.DownloadTaskStatus
import com.musica.paralosoidos.data.NetworkMonitor
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class DownloaderViewModel(
    private val context: Context,
    private val downloadSettings: DownloadSettings,
    private val networkMonitor: NetworkMonitor,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadHistoryDao: DownloadHistoryDao
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<DownloadSearchResult>>(emptyList())
    val searchResults: StateFlow<List<DownloadSearchResult>> = _searchResults.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = networkMonitor.isCurrentlyOnline()
    )

    private val _isServiceBlocked = MutableStateFlow(false)
    val isServiceBlocked: StateFlow<Boolean> = _isServiceBlocked.asStateFlow()

    private val _serviceNoticeMessage = MutableStateFlow<String?>(null)
    val serviceNoticeMessage: StateFlow<String?> = _serviceNoticeMessage.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private val _isQueueSheetOpen = MutableStateFlow(false)
    val isQueueSheetOpen: StateFlow<Boolean> = _isQueueSheetOpen.asStateFlow()

    val queue: StateFlow<List<DownloadTask>> = downloadQueueManager.queue
    val activeCount: StateFlow<Int> = downloadQueueManager.activeCount

    val settings: StateFlow<DownloadSettingsState> = downloadSettings.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DownloadSettingsState()
    )

    val downloadHistory: StateFlow<List<DownloadHistoryEntity>> = downloadHistoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun openQueueSheet() {
        _isQueueSheetOpen.value = true
    }

    fun closeQueueSheet() {
        _isQueueSheetOpen.value = false
    }

    fun search(rawQuery: String) {
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) return
        if (!isOnline.value) {
            _userFeedbackMessage.value = "Sin conexion a Internet para realizar busquedas."
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _serviceNoticeMessage.value = null

            try {
                // If query is direct YouTube link, parse video ID
                val isDirectUrl = trimmed.contains("youtube.com") || trimmed.contains("youtu.be")
                delay(400) // Small debounce

                val results = if (isDirectUrl) {
                    val videoId = extractYouTubeVideoId(trimmed)
                    listOf(
                        DownloadSearchResult(
                            id = videoId,
                            title = "Pista de YouTube: $videoId",
                            channel = "Enlace directo",
                            durationSeconds = 210,
                            thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                            viewCount = 100_000
                        )
                    )
                } else {
                    // Real search via yt-dlp
                    val searchResults = performYoutubeSearch(trimmed)
                    searchResults
                }
                _searchResults.value = results
            } catch (e: Exception) {
                // Autoprotection circuit-breaker
                val errorMsg = e.message.orEmpty()
                if (errorMsg.contains("Sign in", ignoreCase = true) ||
                    errorMsg.contains("Bot", ignoreCase = true) ||
                    errorMsg.contains("unsupported", ignoreCase = true)
                ) {
                    _isServiceBlocked.value = true
                    _serviceNoticeMessage.value = "Servicio de descargas temporalmente no disponible por cambios en YouTube. Se actualizara automaticamente."
                } else {
                    _userFeedbackMessage.value = "No se pudieron obtener resultados de YouTube."
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    private suspend fun performYoutubeSearch(query: String): List<DownloadSearchResult> =
        withContext(Dispatchers.IO) {
            val request = YoutubeDLRequest("ytsearch5:$query").apply {
                addOption("--dump-json")
                addOption("--flat-playlist")
                addOption("--no-playlist")
            }
            val response = YoutubeDL.getInstance().execute(request)
            val output = response.out.trim()
            val results = mutableListOf<DownloadSearchResult>()
            output.lines().forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.startsWith("{")) {
                    runCatching {
                        val json = org.json.JSONObject(trimmedLine)
                        val id = json.optString("id", "")
                        val title = json.optString("title", "Sin titulo")
                        val channel = json.optString("uploader", json.optString("channel", "Desconocido"))
                        val duration = json.optLong("duration", 0L).toInt()
                        val rawThumb = json.optString("thumbnail", "")
                        val thumbnail = if (rawThumb.isNotBlank()) rawThumb else "https://img.youtube.com/vi/$id/hqdefault.jpg"
                        val viewCount = json.optLong("view_count", 0L)
                        if (id.isNotBlank()) {
                            results.add(
                                DownloadSearchResult(
                                    id = id,
                                    title = title,
                                    channel = channel,
                                    durationSeconds = duration,
                                    thumbnailUrl = thumbnail,
                                    viewCount = viewCount
                                )
                            )
                        }
                    }
                }
            }
            results
        }

    fun updateQuality(format: AudioOutputFormat, bitrate: Int?) {
        viewModelScope.launch {
            downloadSettings.save(format, bitrate)
        }
    }

    fun toggleSyncLyrics(syncLyrics: Boolean) {
        viewModelScope.launch {
            downloadSettings.saveSyncLyrics(syncLyrics)
        }
    }

    fun downloadTrack(
        result: DownloadSearchResult,
        format: AudioOutputFormat,
        bitrate: Int?
    ) {
        if (!isOnline.value) {
            _userFeedbackMessage.value = "Sin conexion a Internet. No se puede iniciar la descarga."
            return
        }

        val currentSettings = settings.value
        val task = DownloadTask(
            id = "${result.id}_${System.currentTimeMillis()}",
            videoId = result.id,
            title = result.title,
            channel = result.channel,
            thumbnailUrl = result.thumbnailUrl,
            format = format,
            bitrate = bitrate,
            syncLyrics = currentSettings.syncLyrics,
            status = DownloadTaskStatus.QUEUED
        )

        downloadQueueManager.enqueue(task)

        val qualityDesc = if (format == AudioOutputFormat.BEST) "Original" else "${bitrate ?: 320}K"
        _userFeedbackMessage.value = "Pista agregada a la cola: ${result.title} ($qualityDesc)"
    }

    fun pauseTask(taskId: String) {
        downloadQueueManager.pause(taskId)
    }

    fun resumeTask(taskId: String) {
        downloadQueueManager.resume(taskId)
    }

    fun retryTask(taskId: String) {
        downloadQueueManager.retry(taskId)
    }

    fun cancelTask(taskId: String) {
        downloadQueueManager.cancel(taskId)
    }

    fun clearCompletedTasks() {
        downloadQueueManager.clearCompleted()
    }

    fun retryService() {
        _isServiceBlocked.value = false
        _serviceNoticeMessage.value = null
        val current = _query.value.trim()
        if (current.isNotBlank()) {
            search(current)
        }
    }

    fun dismissUserFeedback() {
        _userFeedbackMessage.value = null
    }

    private fun extractYouTubeVideoId(url: String): String {
        return when {
            url.contains("v=") -> url.substringAfter("v=").substringBefore("&").substringBefore("?")
            url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
            url.contains("embed/") -> url.substringAfter("embed/").substringBefore("?")
            else -> "track"
        }
    }

    class Factory(
        private val context: Context,
        private val downloadSettings: DownloadSettings,
        private val networkMonitor: NetworkMonitor,
        private val downloadQueueManager: DownloadQueueManager,
        private val downloadHistoryDao: DownloadHistoryDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DownloaderViewModel(context, downloadSettings, networkMonitor, downloadQueueManager, downloadHistoryDao) as T
        }
    }
}
