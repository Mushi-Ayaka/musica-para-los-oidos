package com.musica.paralosoidos.data.lyrics

import com.musica.paralosoidos.data.download.MetadataCleaner
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder

class LyricsDownloadEngine(private val okHttpClient: OkHttpClient) {

    fun fetchLyrics(title: String, channel: String, durationSeconds: Int): String? {
        val cleanMeta = MetadataCleaner.clean(title, channel)
        val cleanTitle = cleanMeta.title
        val cleanArtist = cleanMeta.artist

        // Attempt 1: Exact match via /api/get
        val exactLyrics = tryFetchExact(cleanTitle, cleanArtist, durationSeconds)
        if (!exactLyrics.isNullOrBlank()) return exactLyrics

        // Attempt 2: Search query via /api/search
        return trySearchLyrics(cleanTitle, cleanArtist)
    }

    private fun tryFetchExact(title: String, artist: String, durationSeconds: Int): String? {
        return try {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val durationParam = if (durationSeconds > 0) "&duration=$durationSeconds" else ""
            val url = "https://lrclib.net/api/get?track_name=$encodedTitle&artist_name=$encodedArtist$durationParam"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MusicaParaLosOidos/1.3.0 (Android; https://github.com/Mushi-Ayaka)")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val synced = json.optString("syncedLyrics", "")
            synced.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }

    private fun trySearchLyrics(title: String, artist: String): String? {
        return try {
            val query = if (artist.isNotBlank() && artist != "Desconocido") "$artist $title" else title
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://lrclib.net/api/search?q=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MusicaParaLosOidos/1.3.0 (Android; https://github.com/Mushi-Ayaka)")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val array = JSONArray(body)
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val synced = item.optString("syncedLyrics", "")
                if (synced.isNotBlank()) {
                    return synced
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun writeLrcFile(file: File, lrcContent: String) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(lrcContent, Charsets.UTF_8)
        } catch (_: Exception) {
        }
    }
}
