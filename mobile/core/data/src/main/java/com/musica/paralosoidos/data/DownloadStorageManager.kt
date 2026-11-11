package com.musica.paralosoidos.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.first
import java.io.File

data class PublishedAudio(
    val uri: Uri,
    val mediaScanFile: File?
)

class DownloadStorageManager(
    context: Context,
    private val downloadSettings: DownloadSettings
) {
    private val appContext = context.applicationContext

    private val stagingDir: File
        get() = File(appContext.cacheDir, "pending-audio-downloads")

    private val lyricsDir: File
        get() = File(
            appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: appContext.filesDir,
            "Musica para los Oidos/Letras"
        )

    fun ensureDirectoriesExist() {
        check(stagingDir.exists() || stagingDir.mkdirs()) {
            "No se pudo crear el directorio temporal de descargas."
        }
        check(lyricsDir.exists() || lyricsDir.mkdirs()) {
            "No se pudo crear el directorio de letras."
        }
    }

    fun sanitizeFileName(name: String): String =
        name.replace(Regex("""[/\\:*?"<>|]"""), "_").trim().take(200)

    fun getAudioFile(title: String, extension: String): File =
        File(stagingDir, "${sanitizeFileName(title)}.$extension")

    suspend fun publishAudioFile(source: File): PublishedAudio {
        val folderUri = downloadSettings.state.first().outputFolderUri
            ?.let(Uri::parse)
            ?: throw IllegalStateException("Elige una carpeta para guardar las descargas.")
        val tree = DocumentFile.fromTreeUri(appContext, folderUri)
            ?: throw IllegalStateException("No se pudo abrir la carpeta de descargas elegida.")
        check(tree.canWrite()) {
            "La app ya no tiene permiso para escribir en la carpeta elegida. Selecciónala de nuevo."
        }

        val songsDirectory = tree.findFile("Canciones")
            ?: tree.createDirectory("Canciones")
            ?: throw IllegalStateException("No se pudo crear la carpeta Canciones en la ubicación elegida.")
        check(songsDirectory.isDirectory && songsDirectory.canWrite()) {
            "La carpeta Canciones no permite guardar archivos."
        }
        val displayName = uniqueDisplayName(songsDirectory, source.name)
        val destination = songsDirectory.createFile(mimeTypeFor(source), displayName)
            ?: throw IllegalStateException("No se pudo crear el archivo en la carpeta seleccionada.")

        try {
            val output = appContext.contentResolver.openOutputStream(destination.uri, "w")
                ?: throw IllegalStateException("Android no permitió escribir el archivo en la carpeta elegida.")
            source.inputStream().use { input ->
                output.use { input.copyTo(it) }
            }
        } catch (error: Exception) {
            destination.delete()
            throw error
        } finally {
            source.delete()
        }

        return PublishedAudio(
            uri = destination.uri,
            mediaScanFile = resolvePrimaryStorageFile(folderUri, "Canciones/$displayName")
        )
    }

    fun getLyricsFile(title: String): File =
        File(lyricsDir, "${sanitizeFileName(title)}.lrc")

    private fun uniqueDisplayName(directory: DocumentFile, requestedName: String): String {
        if (directory.findFile(requestedName) == null) return requestedName
        val extension = requestedName.substringAfterLast('.', "")
        val baseName = if (extension.isBlank()) requestedName else requestedName.dropLast(extension.length + 1)
        var index = 2
        while (true) {
            val suffix = " ($index)" + if (extension.isBlank()) "" else ".$extension"
            val candidate = baseName + suffix
            if (directory.findFile(candidate) == null) return candidate
            index++
        }
    }

    private fun resolvePrimaryStorageFile(treeUri: Uri, relativeFilePath: String): File? {
        if (treeUri.authority != EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY) return null
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(treeUri) }.getOrNull()
            ?: return null
        val volume = documentId.substringBefore(':', "")
        val directory = documentId.substringAfter(':', "")
        if (volume.isBlank()) return null

        val volumeRoot = if (volume.equals("primary", ignoreCase = true)) {
            Environment.getExternalStorageDirectory()
        } else {
            File("/storage", volume)
        }
        return File(File(volumeRoot, directory), relativeFilePath)
    }

    private fun mimeTypeFor(file: File): String =
        when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "ogg" -> "audio/ogg"
            "opus" -> "audio/opus"
            "webm" -> "audio/webm"
            else -> "audio/*"
        }

    private companion object {
        const val EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents"
    }
}
