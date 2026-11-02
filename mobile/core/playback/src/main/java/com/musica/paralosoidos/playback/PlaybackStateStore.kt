package com.musica.paralosoidos.playback

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class StoredMediaItem(
    val mediaId: String,
    val uri: String,
    val title: String?,
    val artist: String?,
    val album: String?
)

internal data class StoredPlaybackState(
    val items: List<StoredMediaItem>,
    val currentIndex: Int,
    val positionMs: Long,
    val playbackSpeed: Float,
    val repeatMode: Int
)

internal class PlaybackStateStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): StoredPlaybackState? {
        val raw = preferences.getString(STATE_KEY, null) ?: return null
        return runCatching {
            val root = JSONObject(raw)
            if (root.optInt("version", 0) != VERSION) return@runCatching null
            val array = root.optJSONArray("items") ?: return@runCatching null
            val items = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val uri = item.optString("uri").takeIf(String::isNotBlank) ?: continue
                    add(
                        StoredMediaItem(
                            mediaId = item.optString("mediaId"),
                            uri = uri,
                            title = item.optString("title").takeIf(String::isNotBlank),
                            artist = item.optString("artist").takeIf(String::isNotBlank),
                            album = item.optString("album").takeIf(String::isNotBlank)
                        )
                    )
                }
            }
            if (items.isEmpty()) {
                null
            } else {
                StoredPlaybackState(
                    items = items,
                    currentIndex = root.optInt("currentIndex", 0),
                    positionMs = root.optLong("positionMs", 0L),
                    playbackSpeed = root.optDouble("playbackSpeed", 1.0).toFloat(),
                    repeatMode = root.optInt("repeatMode", 0)
                )
            }
        }.getOrNull()
    }

    fun save(state: StoredPlaybackState, synchronous: Boolean = false) {
        val items = JSONArray()
        state.items.forEach { item ->
            items.put(
                JSONObject()
                    .put("mediaId", item.mediaId)
                    .put("uri", item.uri)
                    .put("title", item.title.orEmpty())
                    .put("artist", item.artist.orEmpty())
                    .put("album", item.album.orEmpty())
            )
        }
        val root = JSONObject()
            .put("version", VERSION)
            .put("items", items)
            .put("currentIndex", state.currentIndex)
            .put("positionMs", state.positionMs)
            .put("playbackSpeed", state.playbackSpeed.toDouble())
            .put("repeatMode", state.repeatMode)
        val editor = preferences.edit().putString(STATE_KEY, root.toString())
        if (synchronous) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    fun clear(synchronous: Boolean = false) {
        val editor = preferences.edit().remove(STATE_KEY)
        if (synchronous) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "playback_state"
        const val STATE_KEY = "state"
        const val VERSION = 1
    }
}
