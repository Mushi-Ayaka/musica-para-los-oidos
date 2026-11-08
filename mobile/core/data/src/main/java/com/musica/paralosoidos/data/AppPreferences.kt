package com.musica.paralosoidos.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

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

    val lyricsScrollSpeed: Flow<Float> = dataStore.data.map { preferences ->
        (preferences[LYRICS_SCROLL_SPEED_KEY] ?: 1.0f).coerceIn(0.5f, 2.0f)
    }

    val lastUpdateCheckAt: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_UPDATE_CHECK_AT_KEY] ?: 0L
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

    suspend fun saveLyricsScrollSpeed(speed: Float) {
        require(speed in 0.5f..2.0f) { "La velocidad de letras debe estar entre 0.5x y 2.0x." }
        dataStore.edit { preferences ->
            preferences[LYRICS_SCROLL_SPEED_KEY] = speed
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

    suspend fun saveLastUpdateCheckAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_UPDATE_CHECK_AT_KEY] = timestamp
        }
    }

    val hiddenFolders: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[HIDDEN_FOLDERS_KEY] ?: emptySet()
    }

    suspend fun saveHiddenFolders(folders: Set<String>) {
        dataStore.edit { preferences ->
            preferences[HIDDEN_FOLDERS_KEY] = folders
        }
    }

    val eqEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[EQ_ENABLED_KEY] ?: false
    }

    suspend fun saveEqEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EQ_ENABLED_KEY] = enabled
        }
    }

    val activeEqPreset: Flow<String> = dataStore.data.map { preferences ->
        preferences[ACTIVE_EQ_PRESET_KEY] ?: "Plano"
    }

    suspend fun saveActiveEqPreset(preset: String) {
        dataStore.edit { preferences ->
            preferences[ACTIVE_EQ_PRESET_KEY] = preset
        }
    }

    val eqBands: Flow<List<Int>> = dataStore.data.map { preferences ->
        preferences[EQ_BANDS_KEY]?.let { runCatching { json.decodeFromString<List<Int>>(it) }.getOrNull() } ?: List(10) { 0 }
    }

    suspend fun saveEqBands(bands: List<Int>) {
        dataStore.edit { preferences ->
            preferences[EQ_BANDS_KEY] = json.encodeToString(bands)
        }
    }

    val bassBoost: Flow<Int> = dataStore.data.map { preferences ->
        preferences[BASS_BOOST_KEY] ?: 0
    }

    suspend fun saveBassBoost(strength: Int) {
        dataStore.edit { preferences ->
            preferences[BASS_BOOST_KEY] = strength
        }
    }

    val virtualizer: Flow<Int> = dataStore.data.map { preferences ->
        preferences[VIRTUALIZER_KEY] ?: 0
    }

    suspend fun saveVirtualizer(strength: Int) {
        dataStore.edit { preferences ->
            preferences[VIRTUALIZER_KEY] = strength
        }
    }

    val speakerBoost: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SPEAKER_BOOST_KEY] ?: 0
    }

    suspend fun saveSpeakerBoost(strength: Int) {
        dataStore.edit { preferences ->
            preferences[SPEAKER_BOOST_KEY] = strength
        }
    }

    val mediaVolume: Flow<Float> = dataStore.data.map { preferences ->
        preferences[MEDIA_VOLUME_KEY] ?: 100f
    }

    suspend fun saveMediaVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[MEDIA_VOLUME_KEY] = volume.coerceIn(0f, 100f)
        }
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("theme_settings")
        val EQ_PRESETS_KEY = stringPreferencesKey("custom_eq_presets")
        val PLAYLISTS_KEY = stringPreferencesKey("playlists")
        val LYRICS_FOLDER_KEY = stringPreferencesKey("lyrics_folder_uri")
        val AUTO_RESTORE_DONE_KEY = booleanPreferencesKey("auto_restore_done")
        val LYRICS_SCROLL_SPEED_KEY = floatPreferencesKey("lyrics_scroll_speed")
        val LAST_UPDATE_CHECK_AT_KEY = longPreferencesKey("last_update_check_at")
        val HIDDEN_FOLDERS_KEY = stringSetPreferencesKey("hidden_folders")
        val EQ_ENABLED_KEY = booleanPreferencesKey("eq_enabled")
        val ACTIVE_EQ_PRESET_KEY = stringPreferencesKey("active_eq_preset")
        val EQ_BANDS_KEY = stringPreferencesKey("eq_bands")
        val BASS_BOOST_KEY = intPreferencesKey("bass_boost")
        val VIRTUALIZER_KEY = intPreferencesKey("virtualizer")
        val SPEAKER_BOOST_KEY = intPreferencesKey("speaker_boost")
        val MEDIA_VOLUME_KEY = floatPreferencesKey("media_volume")
        val HEX_COLOR_PATTERN = Regex("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
        val SUPPORTED_FONT_FAMILIES = setOf("jakarta", "system", "serif", "mono")
    }
}
