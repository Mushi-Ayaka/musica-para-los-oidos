package com.musica.paralosoidos.data

import android.content.Context
import android.os.Environment
import java.io.File

class DownloadStorageManager(private val context: Context) {

    private val appContext = context.applicationContext

    // Try public Music dir first, fallback to app-specific external
    private val rootDir: File
        get() {
            val publicMusic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val preferred = File(publicMusic, "Musica para los Oidos")
            return if (publicMusic.canWrite()) preferred
            else File(appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Musica para los Oidos")
        }

    val songsDir: File get() = File(rootDir, "Canciones")
    val lyricsDir: File get() = File(rootDir, "Letras")

    fun ensureDirectoriesExist() {
        songsDir.mkdirs()
        lyricsDir.mkdirs()
    }

    fun sanitizeFileName(name: String): String =
        name.replace(Regex("""[/\\:*?"<>|]"""), "_").trim().take(200)

    fun getAudioFile(title: String, extension: String): File {
        val safe = sanitizeFileName(title)
        return File(songsDir, "$safe.$extension")
    }

    fun getLyricsFile(title: String): File {
        val safe = sanitizeFileName(title)
        return File(lyricsDir, "$safe.lrc")
    }

    fun isDownloaded(title: String): Boolean {
        val safe = sanitizeFileName(title)
        return listOf("mp3", "m4a", "webm", "ogg").any { ext ->
            File(songsDir, "$safe.$ext").exists()
        }
    }
}
