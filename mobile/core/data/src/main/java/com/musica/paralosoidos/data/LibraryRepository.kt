package com.musica.paralosoidos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class LibraryRepository(private val container: AppContainer) {
    val tracks: Flow<List<TrackEntity>> = container.tracks.observeAll()
    val hiddenFolders: Flow<Set<String>> = container.preferences.hiddenFolders
    val playlists: Flow<List<MusicPlaylist>> = container.preferences.playlists
    val themeSettings: Flow<ThemeSettingsState> = container.preferences.themeSettings
    val customEqPresets: Flow<List<SavedEqPreset>> = container.preferences.customEqPresets
    val lyricsScrollSpeed: Flow<Float> = container.preferences.lyricsScrollSpeed

    val eqEnabled: Flow<Boolean> = container.preferences.eqEnabled
    val activeEqPreset: Flow<String> = container.preferences.activeEqPreset
    val eqBands: Flow<List<Int>> = container.preferences.eqBands
    val bassBoost: Flow<Int> = container.preferences.bassBoost
    val virtualizer: Flow<Int> = container.preferences.virtualizer
    val speakerBoost: Flow<Int> = container.preferences.speakerBoost
    val mediaVolume: Flow<Float> = container.preferences.mediaVolume

    val visibleTracks: Flow<List<TrackEntity>> = combine(tracks, hiddenFolders) { trks, hidden ->
        trks.filter { it.folderPath !in hidden }
    }

    val folderItems: Flow<List<FolderItem>> = combine(tracks, hiddenFolders) { trks, hidden ->
        trks.groupBy { it.folderPath }
            .map { (path, group) ->
                val name = group.firstOrNull()?.folderName?.ifBlank { "Música" } ?: "Música"
                FolderItem(
                    path = path,
                    name = name,
                    trackCount = group.size,
                    isHidden = path in hidden
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun refresh(force: Boolean) {
        container.refreshLocalAudio(force)
    }

    suspend fun verifyAndSyncAudioMismatch() {
        container.verifyAndSyncAudioMismatch()
    }

    suspend fun setFolderHidden(folderPath: String, hidden: Boolean) {
        val current = hiddenFolders.first().toMutableSet()
        if (hidden) {
            current.add(folderPath)
        } else {
            current.remove(folderPath)
        }
        container.preferences.saveHiddenFolders(current)
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

    suspend fun saveLyricsScrollSpeed(speed: Float) {
        container.preferences.saveLyricsScrollSpeed(speed)
    }

    suspend fun saveEqEnabled(enabled: Boolean) {
        container.preferences.saveEqEnabled(enabled)
    }

    suspend fun saveActiveEqPreset(preset: String) {
        container.preferences.saveActiveEqPreset(preset)
    }

    suspend fun saveEqBands(bands: List<Int>) {
        container.preferences.saveEqBands(bands)
    }

    suspend fun saveBassBoost(strength: Int) {
        container.preferences.saveBassBoost(strength)
    }

    suspend fun saveVirtualizer(strength: Int) {
        container.preferences.saveVirtualizer(strength)
    }

    suspend fun saveSpeakerBoost(strength: Int) {
        container.preferences.saveSpeakerBoost(strength)
    }

    suspend fun saveMediaVolume(volume: Float) {
        container.preferences.saveMediaVolume(volume)
    }
}
