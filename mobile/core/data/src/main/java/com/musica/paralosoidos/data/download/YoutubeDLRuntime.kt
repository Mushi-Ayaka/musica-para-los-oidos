package com.musica.paralosoidos.data.download

import android.content.Context
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface YoutubeDLState {
    data object Preparing : YoutubeDLState
    data object Updating : YoutubeDLState
    data object Ready : YoutubeDLState
    data class Failed(val message: String) : YoutubeDLState
}

class YoutubeDLRuntime(context: Context) {
    private val appContext = context.applicationContext
    private val initializationMutex = Mutex()
    private val _state = MutableStateFlow<YoutubeDLState>(YoutubeDLState.Preparing)

    val state: StateFlow<YoutubeDLState> = _state.asStateFlow()

    private var lastUpdateCheckMillis: Long = 0L

    suspend fun ensureReady(forceUpdate: Boolean = false) {
        initializationMutex.withLock {
            val now = System.currentTimeMillis()
            val currentVersion = runCatching { YoutubeDL.getInstance().versionName(appContext) }.getOrNull()
            val isOldVersion = currentVersion == null || currentVersion.startsWith("2025.") || currentVersion.contains("2025")

            if (_state.value == YoutubeDLState.Ready && !isOldVersion && (!forceUpdate || now - lastUpdateCheckMillis < 60_000L)) {
                return
            }

            _state.value = YoutubeDLState.Updating
            try {
                withContext(Dispatchers.IO) {
                    FFmpeg.getInstance().init(appContext)
                    YoutubeDL.getInstance().init(appContext)
                    val registeredVersion = YoutubeDL.getInstance().versionName(appContext)
                    Log.i(TAG, "Iniciando comprobación yt-dlp; versión actual=$registeredVersion")

                    // 1. Intentar primero actualización directa OTA (sin depender de GitHub API rate limit)
                    val directSuccess = runCatching { directOtaUpdate(appContext) }.getOrDefault(false)
                    if (!directSuccess) {
                        // 2. Fallback a updater de la librería
                        runCatching {
                            val updateStatus = YoutubeDL.getInstance().updateYoutubeDL(
                                appContext,
                                YoutubeDL.UpdateChannel.STABLE
                            )
                            Log.i(TAG, "yt-dlp updateStatus=$updateStatus versión=${YoutubeDL.getInstance().versionName(appContext)}")
                        }.onFailure { err ->
                            Log.w(TAG, "No se pudo actualizar yt-dlp online, versión actual=${YoutubeDL.getInstance().versionName(appContext)}", err)
                        }
                    }
                    lastUpdateCheckMillis = System.currentTimeMillis()
                    Log.i(TAG, "yt-dlp listo con versión final=${YoutubeDL.getInstance().versionName(appContext)}")
                }
                _state.value = YoutubeDLState.Ready
            } catch (cancelled: CancellationException) {
                _state.value = YoutubeDLState.Preparing
                throw cancelled
            } catch (error: Exception) {
                markFailed(error)
                throw error
            } catch (error: LinkageError) {
                markFailed(error)
                throw error
            }
        }
    }

    private fun directOtaUpdate(context: Context): Boolean {
        return try {
            // Conectar al endpoint web de última release para obtener el tag de versión redirigido
            val checkUrl = URL("https://github.com/yt-dlp/yt-dlp/releases/latest")
            val conn = (checkUrl.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                connectTimeout = 8000
                readTimeout = 8000
            }
            conn.connect()
            val redirectUrl = conn.getHeaderField("Location")
            conn.disconnect()

            val latestTag = if (!redirectUrl.isNullOrBlank()) {
                redirectUrl.substringAfterLast("/").trim()
            } else {
                null
            }

            val currentVersion = YoutubeDL.getInstance().versionName(context)
            Log.i(TAG, "Direct OTA: registrada=$currentVersion, latest=$latestTag")

            if (latestTag != null && currentVersion == latestTag) {
                Log.i(TAG, "yt-dlp ya está en la versión más reciente ($latestTag)")
                return true
            }

            // Descargar el binario directamente desde el asset de release de GitHub
            var currentDownloadUrl = URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp")
            var dlConn: HttpURLConnection
            var redirects = 0
            while (true) {
                dlConn = currentDownloadUrl.openConnection() as HttpURLConnection
                dlConn.instanceFollowRedirects = false
                dlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                dlConn.connectTimeout = 15000
                dlConn.readTimeout = 30000
                val status = dlConn.responseCode
                if (status in 300..399) {
                    val newLocation = dlConn.getHeaderField("Location") ?: break
                    dlConn.disconnect()
                    currentDownloadUrl = URL(newLocation)
                    redirects++
                    if (redirects > 5) break
                } else {
                    break
                }
            }

            val tempFile = File.createTempFile("ytdlp_ota", ".tmp", context.cacheDir)
            try {
                dlConn.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } finally {
                dlConn.disconnect()
            }

            if (tempFile.length() < 500_000L) {
                Log.w(TAG, "Archivo yt-dlp descargado incompleto (${tempFile.length()} bytes)")
                tempFile.delete()
                return false
            }

            // El directorio nativo donde youtubedl-android guarda y busca yt-dlp:
            // Context.getNoBackupFilesDir()/youtubedl-android/yt-dlp/yt-dlp
            val ytdlpDir = File(File(context.noBackupFilesDir, "youtubedl-android"), "yt-dlp").apply { mkdirs() }
            val targetFile = File(ytdlpDir, "yt-dlp")
            tempFile.copyTo(targetFile, overwrite = true)
            targetFile.setExecutable(true, false)
            tempFile.delete()

            val versionToSave = latestTag ?: "2026.latest"
            context.getSharedPreferences("youtubedl-android", Context.MODE_PRIVATE)
                .edit()
                .putString("dlpVersion", versionToSave)
                .putString("dlpVersionName", versionToSave)
                .apply()

            Log.i(TAG, "yt-dlp actualizado exitosamente via OTA directa a versión $versionToSave")
            true
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo completar actualización directa OTA de yt-dlp", e)
            false
        }
    }

    private fun markFailed(error: Throwable) {
        val message = error.localizedMessage ?: error.javaClass.simpleName
        _state.value = YoutubeDLState.Failed(message)
        Log.e(TAG, "No se pudo preparar o actualizar yt-dlp", error)
    }

    private companion object {
        const val TAG = "YoutubeDLRuntime"
    }
}
