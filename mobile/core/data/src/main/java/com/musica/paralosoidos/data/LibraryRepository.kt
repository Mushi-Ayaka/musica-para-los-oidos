package com.musica.paralosoidos.data

import kotlinx.coroutines.flow.Flow

class LibraryRepository(private val container: AppContainer) {
    val tracks: Flow<List<TrackEntity>> = container.tracks.observeAll()
    val playlists: Flow<List<MusicPlaylist>> = container.preferences.playlists
    val themeSettings: Flow<ThemeSettingsState> = container.preferences.themeSettings
    val customEqPresets: Flow<List<SavedEqPreset>> = container.preferences.customEqPresets

    suspend fun refresh(force: Boolean) {
        container.refreshLocalAudio(force)
    }

    suspend fun savePlaylists(playlists: List<MusicPlaylist>) {
        container.preferences.savePlaylists(playlists)
    }

    suspend fun saveThemeSettings(settings: ThemeSettingsState) {
        container.preferences.saveThemeSettings(settings)
    }

    suspend fun saveCustomEqPresets(presets: List<SavedEqPreset>) {
        container.preferences.saveCustomEqPresets(presets)
    }
}
