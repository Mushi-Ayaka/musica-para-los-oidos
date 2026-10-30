package com.musica.paralosoidos.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class AutoBackupManager(
    context: Context,
    private val preferences: AppPreferences,
    private val tracks: TrackDao
) {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val restoreMutex = Mutex()
    private val _lastMessage = MutableStateFlow<String?>(null)

    val lastMessage: StateFlow<String?> = _lastMessage

    @Volatile
    private var restoreCompleted = false

    fun start() {
        scope.launch {
            combine(
                preferences.playlists,
                preferences.themeSettings,
                preferences.customEqPresets
            ) { _, _, _ -> Unit }
                .drop(1)
                .collectLatest {
                    if (!restoreCompleted) return@collectLatest
                    delay(1_200)
                    runCatching { exportNow() }
                }
        }
    }

    suspend fun restoreIfNeeded() = restoreMutex.withLock {
        if (restoreCompleted) return@withLock
        if (preferences.autoRestoreDone.first()) {
            restoreCompleted = true
            return@withLock
        }

        val local = preferences.playlists.first()
        val backup = readBackup()
        var restoredCount = 0
        if (backup != null) {
            val library = tracks.observeAll().first()
            val restored = AutoBackupCodec.toPlaylists(backup, library)
            val merged = if (local.isEmpty()) restored else AutoBackupCodec.merge(local, restored)
            restoredCount = (merged.size - local.size).coerceAtLeast(0)
            if (merged != local) {
                runCatching { preferences.savePlaylists(merged) }
            }
            if (local.isEmpty()) {
                backup.theme?.let { theme -> runCatching { preferences.saveThemeSettings(theme) } }
                if (backup.eqPresets.isNotEmpty()) {
                    runCatching { preferences.saveCustomEqPresets(backup.eqPresets) }
                }
            }
        }

        preferences.markAutoRestoreDone()
        restoreCompleted = true
        if (restoredCount > 0) {
            _lastMessage.value = "Se restauraron $restoredCount listas desde la copia automática."
        }
        runCatching { exportNow() }
    }

    fun clearMessage() {
        _lastMessage.value = null
    }

    suspend fun exportNow() {
        val library = tracks.observeAll().first()
        val text = AutoBackupCodec.encode(
            playlists = preferences.playlists.first(),
            theme = preferences.themeSettings.first(),
            eqPresets = preferences.customEqPresets.first(),
            library = library,
            exportedAt = System.currentTimeMillis()
        )
        writeBackup(text)
    }

    private suspend fun readBackup(): AutoBackupFile? {
        val current = readFile(CURRENT_FILE)
        current?.let { text ->
            runCatching { AutoBackupCodec.decode(text) }.getOrNull()?.let { return it }
        }
        val previous = readFile(PREVIOUS_FILE) ?: return null
        return runCatching { AutoBackupCodec.decode(previous) }.getOrNull()
    }

    private suspend fun writeBackup(text: String) {
        val current = readFile(CURRENT_FILE)
        if (current != null && runCatching { AutoBackupCodec.decode(current) }.isSuccess) {
            runCatching { writeFile(PREVIOUS_FILE, current) }
        }
        writeFile(CURRENT_FILE, text)
    }

    private suspend fun readFile(name: String): String? = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            readFromMediaStore(name)
        } else {
            readFromLegacyStorage(name)
        }
    }

    private suspend fun writeFile(name: String, text: String) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToMediaStore(name, text)
        } else {
            writeToLegacyStorage(name, text)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun readFromMediaStore(name: String): String? {
        val uri = findMediaStoreUri(name) ?: return null
        return runCatching {
            resolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        }.getOrNull()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeToMediaStore(name: String, text: String) {
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val existing = findMediaStoreUri(name)
        val uri = existing ?: resolver.insert(
            collection,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$FOLDER_PATH/")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        ) ?: error("No se pudo crear la copia automática en Descargas.")

        if (existing != null) {
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 1) },
                null,
                null
            )
        }

        var written = false
        try {
            resolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(text.toByteArray(Charsets.UTF_8))
                output.flush()
            } ?: error("No se pudo escribir la copia automática.")
            written = true
        } finally {
            if (written || existing != null) {
                resolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null
                )
            } else {
                resolver.delete(uri, null, null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun findMediaStoreUri(name: String): Uri? {
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        resolver.query(collection, projection, selection, arrayOf(name), null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return ContentUris.withAppendedId(collection, cursor.getLong(0))
            }
        }
        return null
    }

    @Suppress("DEPRECATION")
    private fun readFromLegacyStorage(name: String): String? {
        val candidates = listOfNotNull(publicBackupDir(), fallbackBackupDir())
        return candidates.asSequence()
            .map { File(it, name) }
            .firstOrNull { it.exists() }
            ?.let { runCatching { it.readText(Charsets.UTF_8) }.getOrNull() }
    }

    @Suppress("DEPRECATION")
    private fun writeToLegacyStorage(name: String, text: String) {
        val directory = publicBackupDir() ?: fallbackBackupDir().also { it.mkdirs() }
        File(directory, name).writeText(text, Charsets.UTF_8)
    }

    @Suppress("DEPRECATION")
    private fun publicBackupDir(): File? {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val directory = File(downloads, FOLDER)
        return directory.takeIf { it.exists() || it.mkdirs() }
    }

    private fun fallbackBackupDir(): File =
        File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: appContext.filesDir, FOLDER)

    private companion object {
        const val FOLDER = "MusicaParaLosOidos"
        val FOLDER_PATH = "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER"
        const val CURRENT_FILE = "musica-para-los-oidos-backup.json"
        const val PREVIOUS_FILE = "musica-para-los-oidos-backup-anterior.json"
    }
}
