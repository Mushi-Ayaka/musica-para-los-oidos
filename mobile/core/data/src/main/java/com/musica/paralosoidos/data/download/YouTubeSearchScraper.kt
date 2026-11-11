package com.musica.paralosoidos.data.download

import com.musica.paralosoidos.data.DownloadSearchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object YouTubeSearchScraper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(7, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val YT_INITIAL_DATA_PATTERN = Pattern.compile("var ytInitialData\\s*=\\s*(\\{.+?\\});</script>|ytInitialData\\s*=\\s*(\\{.+?\\});")

    suspend fun search(query: String, maxResults: Int = 10): List<DownloadSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            val url = "https://www.youtube.com/results?search_query=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "es-419,es;q=0.9,en;q=0.8")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return@withContext emptyList()
            }
            val html = response.use { it.body?.string() } ?: return@withContext emptyList()
            val matcher = YT_INITIAL_DATA_PATTERN.matcher(html)
            val jsonString = if (matcher.find()) {
                matcher.group(1) ?: matcher.group(2)
            } else null ?: return@withContext emptyList()

            val rootJson = JSONObject(jsonString)
            val results = mutableListOf<DownloadSearchResult>()

            val contents = rootJson.optJSONObject("contents")
                ?.optJSONObject("twoColumnSearchResultsRenderer")
                ?.optJSONObject("primaryContents")
                ?.optJSONObject("sectionListRenderer")
                ?.optJSONArray("contents") ?: return@withContext emptyList()

            for (i in 0 until contents.length()) {
                val section = contents.optJSONObject(i)?.optJSONObject("itemSectionRenderer") ?: continue
                val items = section.optJSONArray("contents") ?: continue
                for (j in 0 until items.length()) {
                    val v = items.optJSONObject(j)?.optJSONObject("videoRenderer") ?: continue
                    val id = v.optString("videoId", "")
                    if (id.isBlank()) continue

                    val title = v.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Sin título") ?: "Sin título"
                    val channel = v.optJSONObject("ownerText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
                        ?: v.optJSONObject("shortBylineText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Desconocido")
                        ?: "Desconocido"
                    val durationStr = v.optJSONObject("lengthText")?.optString("simpleText", "")
                    val durationSec = parseDurationSeconds(durationStr)
                    val thumbnail = "https://img.youtube.com/vi/$id/hqdefault.jpg"

                    results.add(
                        DownloadSearchResult(
                            id = id,
                            title = title,
                            channel = channel,
                            durationSeconds = durationSec,
                            thumbnailUrl = thumbnail,
                            viewCount = 0L
                        )
                    )
                    if (results.size >= maxResults) break
                }
                if (results.size >= maxResults) break
            }
            results
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            android.util.Log.w("YouTubeSearchScraper", "Error en scraping web: ${e.message}")
            emptyList()
        }
    }

    fun parseDurationSeconds(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        val parts = text.split(':')
        return when (parts.size) {
            3 -> (parts[0].toIntOrNull() ?: 0) * 3600 + (parts[1].toIntOrNull() ?: 0) * 60 + (parts[2].toIntOrNull() ?: 0)
            2 -> (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
            1 -> parts[0].toIntOrNull() ?: 0
            else -> 0
        }
    }
}
