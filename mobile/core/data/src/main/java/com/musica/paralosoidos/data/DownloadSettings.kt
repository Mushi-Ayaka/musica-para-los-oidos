package com.musica.paralosoidos.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.downloadSettingsDataStore by preferencesDataStore(name = "download_settings")

const val PUBLIC_MUSIC_FOLDER = "Music/Musica para los Oídos"

data class DownloadSettingsState(
    val format: AudioOutputFormat? = null,
    val mp3Bitrate: Int? = null,
    val syncLyrics: Boolean = true
) {
    val isConfigured: Boolean
        get() = format == AudioOutputFormat.BEST ||
            (format == AudioOutputFormat.MP3 && mp3Bitrate != null && mp3Bitrate in MP3_BITRATES)

    companion object {
        val MP3_BITRATES = listOf(128, 192, 256, 320)
    }
}

class DownloadSettings(context: Context) {
    private val dataStore = context.applicationContext.downloadSettingsDataStore

    val state: Flow<DownloadSettingsState> = dataStore.data.map { preferences ->
        DownloadSettingsState(
            format = AudioOutputFormat.entries.firstOrNull {
                it.key == preferences[FORMAT_KEY]
            },
            mp3Bitrate = preferences[MP3_BITRATE_KEY],
            syncLyrics = preferences[SYNC_LYRICS_KEY] ?: true
        )
    }

    suspend fun save(format: AudioOutputFormat?, mp3Bitrate: Int?, syncLyrics: Boolean? = null) {
        require(mp3Bitrate == null || mp3Bitrate in DownloadSettingsState.MP3_BITRATES) {
            "La calidad de MP3 seleccionada no es válida."
        }
        dataStore.edit { preferences ->
            if (format == null) {
                preferences.remove(FORMAT_KEY)
            } else {
                preferences[FORMAT_KEY] = format.key
            }
            if (mp3Bitrate == null) {
                preferences.remove(MP3_BITRATE_KEY)
            } else {
                preferences[MP3_BITRATE_KEY] = mp3Bitrate
            }
            if (syncLyrics != null) {
                preferences[SYNC_LYRICS_KEY] = syncLyrics
            }
        }
    }

    suspend fun saveSyncLyrics(syncLyrics: Boolean) {
        dataStore.edit { preferences ->
            preferences[SYNC_LYRICS_KEY] = syncLyrics
        }
    }

    suspend fun restore(formatKey: String?, mp3Bitrate: Int?, syncLyrics: Boolean? = true) {
        val format = AudioOutputFormat.entries.firstOrNull { it.key == formatKey }
        val bitrate = mp3Bitrate?.takeIf { it in DownloadSettingsState.MP3_BITRATES }
        save(format, bitrate, syncLyrics)
    }

    private companion object {
        val FORMAT_KEY = stringPreferencesKey("audio_format")
        val MP3_BITRATE_KEY = intPreferencesKey("mp3_bitrate")
        val SYNC_LYRICS_KEY = booleanPreferencesKey("sync_lyrics")
    }
}
