package com.musica.paralosoidos.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musica.paralosoidos.data.update.AvailableRelease
import com.musica.paralosoidos.data.update.UpdateCheckResult
import com.musica.paralosoidos.data.update.UpdateRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object NoConnection : UpdateUiState
    data class Available(val release: AvailableRelease) : UpdateUiState
    data class Downloading(val release: AvailableRelease, val progress: Float) : UpdateUiState
    data class ReadyToInstall(val release: AvailableRelease, val file: File) : UpdateUiState
    data class Error(val message: String, val release: AvailableRelease? = null) : UpdateUiState
}

class UpdateViewModel(
    private val appContext: Context,
    private val updateRepository: UpdateRepository,
    private val currentVersion: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    init {
        // Chequeo inicial automático (respetando throttle de 6 h)
        checkForUpdates(force = false)
    }

    fun openDialog() {
        _showDialog.value = true
    }

    fun closeDialog() {
        _showDialog.value = false
    }

    fun checkForUpdates(
        force: Boolean = false,
        onFeedback: ((String) -> Unit)? = null
    ) {
        if (_isChecking.value) return

        viewModelScope.launch {
            _isChecking.value = true
            try {
                when (val result = updateRepository.check(currentVersion, force)) {
                    is UpdateCheckResult.Available -> {
                        _uiState.value = UpdateUiState.Available(result.release)
                        if (force) {
                            _showDialog.value = true
                        }
                    }
                    is UpdateCheckResult.UpToDate -> {
                        if (_uiState.value !is UpdateUiState.Available &&
                            _uiState.value !is UpdateUiState.Downloading &&
                            _uiState.value !is UpdateUiState.ReadyToInstall
                        ) {
                            _uiState.value = UpdateUiState.Idle
                        }
                        if (force) {
                            onFeedback?.invoke("Ya tienes la versión más reciente (v$currentVersion).")
                        }
                    }
                    is UpdateCheckResult.NoConnection -> {
                        if (_uiState.value !is UpdateUiState.Available &&
                            _uiState.value !is UpdateUiState.Downloading &&
                            _uiState.value !is UpdateUiState.ReadyToInstall
                        ) {
                            _uiState.value = UpdateUiState.NoConnection
                        }
                        if (force) {
                            onFeedback?.invoke("Sin conexión a internet.")
                        }
                    }
                    is UpdateCheckResult.Error -> {
                        if (force) {
                            onFeedback?.invoke("No se pudo comprobar: ${result.message}")
                        }
                    }
                }
            } finally {
                _isChecking.value = false
            }
        }
    }

    fun download() {
        val current = _uiState.value
        val release = when (current) {
            is UpdateUiState.Available -> current.release
            is UpdateUiState.Error -> current.release
            is UpdateUiState.Downloading -> current.release
            is UpdateUiState.ReadyToInstall -> current.release
            else -> return
        } ?: return

        viewModelScope.launch {
            _uiState.value = UpdateUiState.Downloading(release, 0f)
            _showDialog.value = true

            val targetFile = getUpdateFile(appContext)
            var success = false

            try {
                withContext(Dispatchers.IO) {
                    if (targetFile.exists()) targetFile.delete()

                    val url = URL(release.downloadUrl)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15_000
                        readTimeout = 20_000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", UpdateRepository.USER_AGENT)
                    }

                    try {
                        val responseCode = conn.responseCode
                        if (responseCode !in 200..299) {
                            throw IllegalStateException("HTTP $responseCode al descargar el archivo.")
                        }

                        val totalBytes = conn.contentLengthLong.takeIf { it > 0 } ?: release.sizeBytes
                        val digest = MessageDigest.getInstance("SHA-256")
                        var downloadedBytes = 0L

                        conn.inputStream.use { input ->
                            FileOutputStream(targetFile).use { output ->
                                val buffer = ByteArray(8192)
                                var bytesRead: Int
                                var lastUpdateMs = 0L

                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    digest.update(buffer, 0, bytesRead)
                                    downloadedBytes += bytesRead

                                    val now = System.currentTimeMillis()
                                    if (now - lastUpdateMs > 100 || downloadedBytes == totalBytes) {
                                        lastUpdateMs = now
                                        val progress = if (totalBytes > 0) {
                                            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                                        } else {
                                            0f
                                        }
                                        _uiState.value = UpdateUiState.Downloading(release, progress)
                                    }
                                }
                            }
                        }

                        // Verificación opcional de SHA-256 si la API proveyó digest
                        release.digest?.let { expectedDigest ->
                            if (expectedDigest.startsWith("sha256:", ignoreCase = true)) {
                                val expectedHex = expectedDigest.substringAfter(":").trim()
                                val actualHex = digest.digest().joinToString("") { "%02x".format(it) }
                                if (!actualHex.equals(expectedHex, ignoreCase = true)) {
                                    targetFile.delete()
                                    throw SecurityException("La suma de comprobación SHA-256 no coincide.")
                                }
                            }
                        }

                        success = true
                    } finally {
                        conn.disconnect()
                    }
                }

                if (success) {
                    _uiState.value = UpdateUiState.ReadyToInstall(release, targetFile)
                }
            } catch (e: CancellationException) {
                if (targetFile.exists()) targetFile.delete()
                _uiState.value = UpdateUiState.Available(release)
                throw e
            } catch (e: Exception) {
                if (targetFile.exists()) targetFile.delete()
                _uiState.value = UpdateUiState.Error(
                    e.localizedMessage ?: "Fallo al descargar la actualización.",
                    release
                )
            }
        }
    }

    fun install(): Boolean {
        val state = _uiState.value
        val release = (state as? UpdateUiState.ReadyToInstall)?.release
            ?: (state as? UpdateUiState.Available)?.release
            ?: (state as? UpdateUiState.Error)?.release

        val file = (state as? UpdateUiState.ReadyToInstall)?.file
            ?: getUpdateFile(appContext).takeIf { it.exists() && it.length() > 0 }

        if (file == null) {
            _uiState.value = UpdateUiState.Error("No se encontró el archivo APK descargado.", release)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!appContext.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${appContext.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(intent)
                _uiState.value = UpdateUiState.Error(
                    "Activa 'Instalar aplicaciones desconocidas' para Música para los Oídos y pulsa Instalar de nuevo.",
                    release
                )
                return false
            }
        }

        return try {
            val uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(installIntent)
            true
        } catch (e: Exception) {
            _uiState.value = UpdateUiState.Error(
                "Error al abrir el instalador: ${e.localizedMessage ?: "desconocido"}",
                release
            )
            false
        }
    }

    private fun getUpdateFile(context: Context): File {
        val dir = File(context.cacheDir, "updates")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "update.apk")
    }

    class Factory(
        private val context: Context,
        private val updateRepository: UpdateRepository,
        private val currentVersion: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdateViewModel(context.applicationContext, updateRepository, currentVersion) as T
        }
    }
}
