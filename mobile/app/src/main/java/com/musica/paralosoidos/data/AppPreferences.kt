package com.musica.paralosoidos.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

@Serializable
data class ThemeSettingsState(
    val presetId: String = "amoled",
    val backgroundColor: String = "#000000",
    val surfaceColor: String = "#0a0a0c",
    val elevatedColor: String = "#131317",
    val playerBackgroundColor: String = "#0f0f14",
    val playerAccentColor: String = "#06b6d4",
    val primaryAccentColor: String = "#06b6d4",
    val secondaryAccentColor: String = "#8b5cf6",
    val textColor: String = "#ffffff",
    val mutedTextColor: String = "#a1a1aa",
    val cornerRadiusDp: Int = 20,
    val fontFamily: String = "jakarta",
    val wallpaperUri: String = "",
    val blurDp: Int = 16,
    val dimAmount: Float = 0.8f,
    val customPresets: List<SavedThemePreset> = emptyList()
)

@Serializable
data class SavedThemePreset(
    val id: String,
    val name: String,
    val settings: ThemeSettingsState
)

@Serializable
data class SavedEqPreset(
    val id: String,
    val name: String,
    val gainsDb: List<Int>,
    val bassBoostDb: Int,
    val virtualizerPercent: Int
)

@Serializable
data class MusicPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<Long>
)

class AppPreferences(context: Context) {
    private val dataStore = context.applicationContext.appPreferencesDataStore
    private val json = Json { ignoreUnknownKeys = true }

    val themeSettings: Flow<ThemeSettingsState> = dataStore.data.map { preferences ->
        preferences[THEME_KEY]?.let(json::decodeFromString) ?: ThemeSettingsState()
    }

    val customEqPresets: Flow<List<SavedEqPreset>> = dataStore.data.map { preferences ->
        preferences[EQ_PRESETS_KEY]?.let(json::decodeFromString) ?: emptyList()
    }

    val playlists: Flow<List<MusicPlaylist>> = dataStore.data.map { preferences ->
        preferences[PLAYLISTS_KEY]?.let(json::decodeFromString) ?: emptyList()
    }

    val lyricsFolderUri: Flow<String?> = dataStore.data.map { preferences ->
        preferences[LYRICS_FOLDER_KEY]
    }

    val autoRestoreDone: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_RESTORE_DONE_KEY] ?: false
    }

    suspend fun saveThemeSettings(settings: ThemeSettingsState) {
        require(settings.cornerRadiusDp in 8..32) { "El radio de esquina debe estar entre 8 y 32 dp." }
        require(settings.blurDp in 0..40) { "El desenfoque debe estar entre 0 y 40 dp." }
        require(settings.dimAmount in 0.1f..0.95f) { "La atenuación debe estar entre 10% y 95%." }
        val colors = listOf(
            settings.backgroundColor,
            settings.surfaceColor,
            settings.elevatedColor,
            settings.playerBackgroundColor,
            settings.playerAccentColor,
            settings.primaryAccentColor,
            settings.secondaryAccentColor,
            settings.textColor,
            settings.mutedTextColor
        )
        require(colors.all { it.matches(HEX_COLOR_PATTERN) }) {
            "Los colores del tema deben usar el formato #RRGGBB o #AARRGGBB."
        }
        require(settings.fontFamily in SUPPORTED_FONT_FAMILIES) {
            "La familia tipográfica seleccionada no está disponible."
        }
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = json.encodeToString(settings)
        }
    }

    suspend fun saveCustomEqPresets(presets: List<SavedEqPreset>) {
        require(presets.all { it.gainsDb.size == 10 && it.gainsDb.all { gain -> gain in -16..16 } }) {
            "Los presets deben contener diez bandas entre -16 dB y +16 dB."
        }
        dataStore.edit { preferences ->
            preferences[EQ_PRESETS_KEY] = json.encodeToString(presets)
        }
    }

    suspend fun savePlaylists(playlists: List<MusicPlaylist>) {
        require(playlists.all { it.name.isNotBlank() && it.trackIds.distinct().size == it.trackIds.size }) {
            "Las listas deben tener nombre y no repetir pistas."
        }
        dataStore.edit { preferences ->
            preferences[PLAYLISTS_KEY] = json.encodeToString(playlists)
        }
    }

    suspend fun markAutoRestoreDone() {
        dataStore.edit { preferences ->
            preferences[AUTO_RESTORE_DONE_KEY] = true
        }
    }

    suspend fun saveLyricsFolder(uri: String?) {
        dataStore.edit { preferences ->
            if (uri.isNullOrBlank()) {
                preferences.remove(LYRICS_FOLDER_KEY)
            } else {
                preferences[LYRICS_FOLDER_KEY] = uri
            }
        }
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme_settings")
        val EQ_PRESETS_KEY = stringPreferencesKey("custom_eq_presets")
        val PLAYLISTS_KEY = stringPreferencesKey("playlists")
        val LYRICS_FOLDER_KEY = stringPreferencesKey("lyrics_folder_uri")
        val AUTO_RESTORE_DONE_KEY = booleanPreferencesKey("auto_restore_done")
        val HEX_COLOR_PATTERN = Regex("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        val SUPPORTED_FONT_FAMILIES = setOf("jakarta", "system", "serif", "mono")
    }
}
