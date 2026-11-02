package com.musica.paralosoidos.data.update

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.musica.paralosoidos.data.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
internal data class GitHubReleaseDto(
    val tag_name: String = "",
    val assets: List<GitHubAssetDto> = emptyList()
)

@Serializable
internal data class GitHubAssetDto(
    val name: String = "",
    val size: Long = 0L,
    val browser_download_url: String = "",
    val digest: String? = null
)

object ReleaseParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonStr: String): AvailableRelease? {
        val dto = runCatching { json.decodeFromString<GitHubReleaseDto>(jsonStr) }.getOrNull() ?: return null
        if (dto.tag_name.isBlank()) return null

        val chosenAsset = dto.assets.firstOrNull { it.name.endsWith("-arm64-release.apk") }
            ?: dto.assets.firstOrNull { it.name.endsWith(".apk") }
            ?: return null

        if (chosenAsset.browser_download_url.isBlank()) return null

        return AvailableRelease(
            tagName = dto.tag_name,
            versionName = dto.tag_name.removePrefix("v").removePrefix("V"),
            assetName = chosenAsset.name,
            downloadUrl = chosenAsset.browser_download_url,
            sizeBytes = chosenAsset.size,
            digest = chosenAsset.digest?.takeIf { it.isNotBlank() }
        )
    }
}

class UpdateRepository(
    private val context: Context,
    private val preferences: AppPreferences
) {
    companion object {
        const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/Mushi-Ayaka/musica-para-los-oidos/releases/latest"
        const val CHECK_THROTTLE_MS = 6 * 60 * 60 * 1000L // 6 horas
        const val USER_AGENT = "MusicaParaLosOidos"
    }

    private val appContext = context.applicationContext

    fun isOnline(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun check(currentVersion: String, force: Boolean = false): UpdateCheckResult {
        if (!isOnline()) return UpdateCheckResult.NoConnection

        val now = System.currentTimeMillis()
        if (!force) {
            val lastCheck = runCatching { preferences.lastUpdateCheckAt.first() }.getOrDefault(0L)
            if (now - lastCheck < CHECK_THROTTLE_MS && lastCheck > 0L) {
                return UpdateCheckResult.UpToDate
            }
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val url = URL(GITHUB_LATEST_RELEASE_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                }

                try {
                    val code = conn.responseCode
                    if (code != 200) {
                        return@runCatching UpdateCheckResult.Error("HTTP $code al consultar actualizaciones.")
                    }

                    val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val release = ReleaseParser.parse(body)
                        ?: return@runCatching UpdateCheckResult.Error("Respuesta de versión no válida.")

                    runCatching { preferences.saveLastUpdateCheckAt(now) }

                    if (VersionComparator.isNewer(release.versionName, currentVersion)) {
                        UpdateCheckResult.Available(release)
                    } else {
                        UpdateCheckResult.UpToDate
                    }
                } finally {
                    conn.disconnect()
                }
            }.getOrElse { e ->
                UpdateCheckResult.Error(e.localizedMessage ?: "Error de conexión")
            }
        }
    }
}
