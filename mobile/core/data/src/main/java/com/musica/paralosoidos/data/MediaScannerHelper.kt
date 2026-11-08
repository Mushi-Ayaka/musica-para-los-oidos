package com.musica.paralosoidos.data

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

object MediaScannerHelper {

    fun mimeTypeFor(file: File): String {
        return when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/x-wav"
            "flac" -> "audio/flac"
            "opus" -> "audio/opus"
            "aac" -> "audio/aac"
            "webm" -> "audio/*"
            else -> "audio/*"
        }
    }

    fun scanFile(context: Context, file: File, onScanned: (Uri?) -> Unit) {
        if (!file.exists()) {
            onScanned(null)
            return
        }
        MediaScannerConnection.scanFile(
            context.applicationContext,
            arrayOf(file.absolutePath),
            arrayOf(mimeTypeFor(file))
        ) { _, uri ->
            onScanned(uri)
        }
    }

    suspend fun scanAndAwait(context: Context, file: File): Uri? = suspendCancellableCoroutine { continuation ->
        if (!file.exists()) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        MediaScannerConnection.scanFile(
            context.applicationContext,
            arrayOf(file.absolutePath),
            arrayOf(mimeTypeFor(file))
        ) { _, uri ->
            if (continuation.isActive) {
                continuation.resume(uri)
            }
        }
    }
}
