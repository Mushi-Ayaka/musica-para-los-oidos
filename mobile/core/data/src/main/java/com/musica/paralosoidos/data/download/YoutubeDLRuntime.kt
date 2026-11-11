package com.musica.paralosoidos.data.download

import android.content.Context
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
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

    suspend fun ensureReady() {
        initializationMutex.withLock {
            if (_state.value == YoutubeDLState.Ready) return

            _state.value = YoutubeDLState.Updating
            try {
                withContext(Dispatchers.IO) {
                    FFmpeg.getInstance().init(appContext)
                    YoutubeDL.getInstance().init(appContext)
                    val bundledVersion = YoutubeDL.getInstance().versionName(appContext)
                    Log.i(TAG, "Comprobando yt-dlp actualizado; versión registrada=$bundledVersion")
                    val updateStatus = YoutubeDL.getInstance().updateYoutubeDL(
                        appContext,
                        YoutubeDL.UpdateChannel.STABLE
                    )
                    Log.i(
                        TAG,
                        "yt-dlp listo: status=$updateStatus version=${YoutubeDL.getInstance().versionName(appContext)}"
                    )
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

    private fun markFailed(error: Throwable) {
        val message = error.localizedMessage ?: error.javaClass.simpleName
        _state.value = YoutubeDLState.Failed(message)
        Log.e(TAG, "No se pudo preparar o actualizar yt-dlp", error)
    }

    private companion object {
        const val TAG = "YoutubeDLRuntime"
    }
}
