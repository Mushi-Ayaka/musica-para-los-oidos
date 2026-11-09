package com.musica.paralosoidos.data.download

import android.content.Context
import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.DownloadStorageManager
import com.musica.paralosoidos.data.DownloadTask
import com.musica.paralosoidos.data.lyrics.LyricsDownloadEngine
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

data class AudioDownloadResult(
    val audioFile: File,
    val hasLrc: Boolean
)

class AudioDownloadEngine(private val context: Context) {

    suspend fun download(
        task: DownloadTask,
        storageManager: DownloadStorageManager,
        lyricsEngine: LyricsDownloadEngine,
        onProgress: (progress: Int, speed: String) -> Unit
    ): AudioDownloadResult = coroutineScope {
        storageManager.ensureDirectoriesExist()

        val extension = when (task.format) {
            AudioOutputFormat.MP3 -> "mp3"
            else -> "m4a"
        }
        val outputFile = storageManager.getAudioFile(task.title, extension)

        // Tarea B (async): LRCLIB fetch con try-catch independiente
        val lyricsDeferred: Deferred<String?> = if (task.syncLyrics) {
            async(Dispatchers.IO) {
                try {
                    lyricsEngine.fetchLyrics(task.title, task.channel, 0)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            async { null }
        }

        // Tarea A (async): Descarga del flujo de audio mediante yt-dlp
        val audioDeferred = async(Dispatchers.IO) {
            var ytReady = false
            for (attempt in 1..25) {
                ytReady = runCatching {
                    com.yausername.ffmpeg.FFmpeg.getInstance().init(context.applicationContext)
                    com.yausername.youtubedl_android.YoutubeDL.getInstance().init(context.applicationContext)
                    true
                }.getOrDefault(false)
                if (ytReady) break
                onProgress(0, "Preparando motor...")
                kotlinx.coroutines.delay(1000)
            }
            if (!ytReady) {
                throw IllegalStateException("El motor de descarga no pudo inicializarse a tiempo. Por favor reintenta.")
            }

            val outputTemplate = outputFile.absolutePath
                .let { path ->
                    val withoutExt = if (path.contains('.')) path.substringBeforeLast('.') else path
                    "$withoutExt.%(ext)s"
                }

            val request = YoutubeDLRequest("https://www.youtube.com/watch?v=${task.videoId}").apply {
                addOption("-x")
                when (task.format) {
                    AudioOutputFormat.MP3 -> {
                        addOption("--audio-format", "mp3")
                        val bitrateValue = task.bitrate ?: 192
                        addOption("--audio-quality", "${bitrateValue}K")
                    }
                    else -> {
                        addOption("--audio-format", "best")
                    }
                }
                addOption("-o", outputTemplate)
                addOption("--no-playlist")
                addOption("--no-part")
            }

            YoutubeDL.getInstance().execute(request) { progressValue, _, _ ->
                val pct = progressValue.toInt().coerceIn(0, 99)
                onProgress(pct, "")
            }

            // Encontrar el archivo resultante
            val actualFile = listOf("mp3", "m4a", "webm", "ogg", "opus").map { ext ->
                File(outputFile.parent, outputFile.nameWithoutExtension + ".$ext")
            }.firstOrNull { it.exists() && it.length() > 0 } ?: outputFile

            actualFile
        }

        // Esperar la descarga del audio
        val completedAudioFile = audioDeferred.await()
        onProgress(100, "")

        // Esperar la tarea de letra sin bloquear
        val lyricsContent = runCatching { lyricsDeferred.await() }.getOrNull()
        var lrcSaved = false
        if (!lyricsContent.isNullOrBlank()) {
            val lrcFile = storageManager.getLyricsFile(task.title)
            lyricsEngine.writeLrcFile(lrcFile, lyricsContent)
            lrcSaved = lrcFile.exists() && lrcFile.length() > 0
        }

        AudioDownloadResult(
            audioFile = completedAudioFile,
            hasLrc = lrcSaved
        )
    }
}
