package com.musica.paralosoidos.data.lyrics

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException

class LyricsDownloadEngine(private val okHttpClient: OkHttpClient) {

    fun fetchLyrics(title: String, channel: String, durationSeconds: Int): String? {
        return try {
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val encodedArtist = java.net.URLEncoder.encode(channel, "UTF-8")
            val url = "https://lrclib.net/api/get?track_name=$encodedTitle&artist_name=$encodedArtist&duration=$durationSeconds"
            val request = Request.Builder().url(url).get().build()
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

    fun writeLrcFile(file: File, lrcContent: String) {
        file.parentFile?.mkdirs()
        file.writeText(lrcContent, Charsets.UTF_8)
    }
}
