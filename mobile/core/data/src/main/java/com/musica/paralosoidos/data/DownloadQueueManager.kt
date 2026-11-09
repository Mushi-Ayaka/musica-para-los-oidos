package com.musica.paralosoidos.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import com.musica.paralosoidos.data.download.AudioDownloadEngine
import com.musica.paralosoidos.data.lyrics.LyricsDownloadEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

class DownloadQueueManager(
    private val context: Context,
    private val notificationManager: DownloadNotificationManager,
    private val storageManager: DownloadStorageManager,
    private val audioDownloadEngine: AudioDownloadEngine,
    private val lyricsDownloadEngine: LyricsDownloadEngine,
    private val downloadHistoryDao: DownloadHistoryDao,
    private val onDownloadComplete: suspend (file: File) -> Unit = {}
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _queue = MutableStateFlow<List<DownloadTask>>(emptyList())
    val queue: StateFlow<List<DownloadTask>> = _queue.asStateFlow()

    val activeCount: StateFlow<Int> = _queue.map { tasks ->
        tasks.count { it.status == DownloadTaskStatus.QUEUED || it.status == DownloadTaskStatus.DOWNLOADING }
    }.stateIn(scope, SharingStarted.Eagerly, 0)

    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val taskId = intent?.getStringExtra(DownloadNotificationManager.EXTRA_TASK_ID) ?: return
            when (intent.action) {
                DownloadNotificationManager.ACTION_PAUSE_DOWNLOAD -> pause(taskId)
                DownloadNotificationManager.ACTION_RESUME_DOWNLOAD -> resume(taskId)
                DownloadNotificationManager.ACTION_CANCEL_DOWNLOAD -> cancel(taskId)
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(DownloadNotificationManager.ACTION_PAUSE_DOWNLOAD)
            addAction(DownloadNotificationManager.ACTION_RESUME_DOWNLOAD)
            addAction(DownloadNotificationManager.ACTION_CANCEL_DOWNLOAD)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(context, notificationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }

    fun enqueue(task: DownloadTask) {
        val current = _queue.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == task.id }
        if (existingIndex >= 0) {
            current[existingIndex] = task.copy(status = DownloadTaskStatus.QUEUED, progress = 0, errorMessage = null)
        } else {
            current.add(0, task.copy(status = DownloadTaskStatus.QUEUED, progress = 0, errorMessage = null))
        }
        _queue.value = current
        processNextIfNeeded()
    }

    fun pause(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        updateTask(taskId) { it.copy(status = DownloadTaskStatus.PAUSED, speed = "") }
        val task = _queue.value.firstOrNull { it.id == taskId }
        if (task != null) {
            notificationManager.showPausedNotification(task.id, task.title)
        }
        processNextIfNeeded()
    }

    fun resume(taskId: String) {
        updateTask(taskId) { it.copy(status = DownloadTaskStatus.QUEUED, errorMessage = null) }
        processNextIfNeeded()
    }

    fun retry(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        updateTask(taskId) { it.copy(status = DownloadTaskStatus.QUEUED, progress = 0, speed = "", errorMessage = null) }
        processNextIfNeeded()
    }

    fun cancel(taskId: String) {
        activeJobs[taskId]?.cancel()
        activeJobs.remove(taskId)
        val current = _queue.value.toMutableList()
        current.removeAll { it.id == taskId }
        _queue.value = current
        notificationManager.cancelNotification(taskId)
        processNextIfNeeded()
    }

    fun clearCompleted() {
        val current = _queue.value.toMutableList()
        val completed = current.filter { it.status == DownloadTaskStatus.COMPLETED }
        completed.forEach { notificationManager.cancelNotification(it.id) }
        current.removeAll { it.status == DownloadTaskStatus.COMPLETED }
        _queue.value = current
    }

    private fun processNextIfNeeded() {
        val downloading = _queue.value.firstOrNull { it.status == DownloadTaskStatus.DOWNLOADING }
        if (downloading != null && activeJobs.containsKey(downloading.id)) return
        val nextTask = _queue.value.firstOrNull { it.status == DownloadTaskStatus.QUEUED } ?: return
        startTaskExecution(nextTask)
    }

    private fun startTaskExecution(task: DownloadTask) {
        updateTask(task.id) { it.copy(status = DownloadTaskStatus.DOWNLOADING, errorMessage = null) }

        val job = scope.launch {
            try {
                notificationManager.showProgressNotification(task.id, task.title, 0, "")
                val downloadResult = audioDownloadEngine.download(
                    task = task,
                    storageManager = storageManager,
                    lyricsEngine = lyricsDownloadEngine,
                    onProgress = { progress, speed ->
                        updateTask(task.id) { it.copy(progress = progress, speed = speed) }
                        notificationManager.showProgressNotification(task.id, task.title, progress, speed)
                    }
                )
                val audioFile = downloadResult.audioFile
                MediaScannerHelper.scanAndAwait(context, audioFile)
                onDownloadComplete(audioFile)
                updateTask(task.id) { it.copy(status = DownloadTaskStatus.COMPLETED, progress = 100, speed = "") }
                val completedNotice = if (task.syncLyrics) {
                    if (downloadResult.hasLrc) "${task.title} (con letra)" else "${task.title} (sin letra)"
                } else {
                    task.title
                }
                notificationManager.showCompletedNotification(task.id, completedNotice)
                scope.launch {
                    downloadHistoryDao.insert(
                        DownloadHistoryEntity(
                            id = "${task.id}_completed",
                            videoId = task.videoId,
                            title = task.title,
                            channel = task.channel,
                            format = task.format.name,
                            bitrate = task.bitrate,
                            filePath = audioFile.absolutePath
                        )
                    )
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    updateTask(task.id) {
                        it.copy(status = DownloadTaskStatus.ERROR, errorMessage = e.message ?: "Error en la descarga", speed = "")
                    }
                    notificationManager.showErrorNotification(task.id, task.title, e.message)
                }
            } finally {
                activeJobs.remove(task.id)
                processNextIfNeeded()
            }
        }

        activeJobs[task.id] = job
    }

    private fun updateTask(taskId: String, transform: (DownloadTask) -> DownloadTask) {
        val current = _queue.value.toMutableList()
        val index = current.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            current[index] = transform(current[index])
            _queue.value = current
        }
    }
}
