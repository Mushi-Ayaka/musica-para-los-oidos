package com.musica.paralosoidos.data.update

import kotlinx.serialization.Serializable

@Serializable
data class AvailableRelease(
    val tagName: String,
    val versionName: String,
    val assetName: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val digest: String? = null
)

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class Available(val release: AvailableRelease) : UpdateCheckResult
    data object NoConnection : UpdateCheckResult
    data class Error(val message: String) : UpdateCheckResult
}

object VersionComparator {
    /**
     * Devuelve true si [remoteVersion] es estrictamente mayor que [currentVersion].
     * Soporta prefijos como 'v' o 'V', y sufijos tipo '-debug' o '-beta'.
     */
    fun isNewer(remoteVersion: String, currentVersion: String): Boolean {
        val remoteParts = normalize(remoteVersion)
        val currentParts = normalize(currentVersion)
        val maxLen = maxOf(remoteParts.size, currentParts.size)

        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun normalize(v: String): List<Int> {
        val cleaned = v.trim().removePrefix("v").removePrefix("V").substringBefore("-")
        return cleaned.split(".")
            .mapNotNull { it.toIntOrNull() }
            .ifEmpty { listOf(0) }
    }
}
