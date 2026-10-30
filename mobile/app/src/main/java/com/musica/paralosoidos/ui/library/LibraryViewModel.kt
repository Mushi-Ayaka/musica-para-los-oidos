package com.musica.paralosoidos.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musica.paralosoidos.data.LibraryRepository
import com.musica.paralosoidos.data.MusicPlaylist
import com.musica.paralosoidos.data.SavedEqPreset
import com.musica.paralosoidos.data.ThemeSettingsState
import com.musica.paralosoidos.data.TrackEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class LibraryViewModel(private val repository: LibraryRepository) : ViewModel() {
    val tracks: StateFlow<List<TrackEntity>> = repository.tracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<MusicPlaylist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val themeSettings: StateFlow<ThemeSettingsState> = repository.themeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeSettingsState())

    val customEqPresets: StateFlow<List<SavedEqPreset>> = repository.customEqPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _librarySubTab = MutableStateFlow("Pistas")
    val librarySubTab: StateFlow<String> = _librarySubTab.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.AZ)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _letterFilter = MutableStateFlow<Char?>(null)
    val letterFilter: StateFlow<Char?> = _letterFilter.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    private val indexedTracks = tracks.map(TrackFilter::index)

    val filteredTracks: StateFlow<List<TrackEntity>> = combine(
        indexedTracks,
        _searchQuery,
        _sortOrder,
        _letterFilter
    ) { source, query, order, letter ->
        TrackFilter.apply(source, query, order, letter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<Pair<String, List<TrackEntity>>>> = tracks.map { list ->
        list.filter { it.album.isNotBlank() }
            .groupBy { it.album }
            .toList()
            .sortedBy { it.first.lowercase(Locale.ROOT) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }

    fun setLibrarySubTab(value: String) {
        _librarySubTab.value = value
    }

    fun setSortOrder(value: SortOrder) {
        _sortOrder.value = value
    }

    fun setLetterFilter(value: Char?) {
        _letterFilter.value = value
    }

    fun refreshLibrary(force: Boolean = false) {
        viewModelScope.launch {
            _scanMessage.value = "Buscando audio..."
            runCatching { repository.refresh(force = force) }
                .onSuccess { _scanMessage.value = null }
                .onFailure {
                    _scanMessage.value = it.localizedMessage ?: "No se pudo leer el audio."
                }
        }
    }

    fun upsertPlaylist(playlist: MusicPlaylist, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val updated = playlists.value.filterNot { it.id == playlist.id } + playlist
            runCatching { repository.savePlaylists(updated) }
                .onSuccess { onResult(true, null) }
                .onFailure {
                    onResult(false, "No se pudo guardar la lista: ${it.localizedMessage ?: "error desconocido"}")
                }
        }
    }

    fun replacePlaylists(playlists: List<MusicPlaylist>, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.savePlaylists(playlists) }
                .onSuccess { onResult(true, null) }
                .onFailure {
                    onResult(false, "No se pudo restaurar la copia: ${it.localizedMessage ?: "error desconocido"}")
                }
        }
    }

    fun deletePlaylist(playlistId: String, onMessage: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                repository.savePlaylists(playlists.value.filterNot { it.id == playlistId })
            }.onSuccess { onMessage("Lista eliminada.") }
                .onFailure {
                    onMessage("No se pudo eliminar la lista: ${it.localizedMessage ?: "error desconocido"}")
                }
        }
    }

    fun movePlaylistTrack(
        playlistId: String,
        trackId: Long,
        direction: Int,
        onMessage: (String?) -> Unit = {}
    ) {
        val playlist = playlists.value.firstOrNull { it.id == playlistId } ?: return
        val updatedIds = playlist.trackIds.toMutableList()
        val current = updatedIds.indexOf(trackId)
        val target = current + direction
        if (current < 0 || target !in updatedIds.indices) return
        val moved = updatedIds.removeAt(current)
        updatedIds.add(target, moved)
        viewModelScope.launch {
            runCatching {
                repository.savePlaylists(
                    playlists.value.map { if (it.id == playlistId) it.copy(trackIds = updatedIds) else it }
                )
            }.onFailure {
                onMessage("No se pudo mover la canción: ${it.localizedMessage ?: "error desconocido"}")
            }
        }
    }

    fun removePlaylistTrack(
        playlistId: String,
        trackId: Long,
        onMessage: (String?) -> Unit = {}
    ) {
        val playlist = playlists.value.firstOrNull { it.id == playlistId } ?: return
        viewModelScope.launch {
            runCatching {
                repository.savePlaylists(
                    playlists.value.map {
                        if (it.id == playlistId) {
                            it.copy(trackIds = it.trackIds.filterNot { id -> id == trackId })
                        } else {
                            it
                        }
                    }
                )
            }.onFailure {
                onMessage("No se pudo quitar la canción: ${it.localizedMessage ?: "error desconocido"}")
            }
        }
    }

    fun saveThemeSettings(settings: ThemeSettingsState, onMessage: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.saveThemeSettings(settings) }
                .onSuccess { onMessage(null) }
                .onFailure {
                    onMessage("No se pudo guardar el estilo: ${it.localizedMessage ?: "error desconocido"}")
                }
        }
    }

    fun saveCustomEqPreset(preset: SavedEqPreset, onMessage: (String?) -> Unit) {
        viewModelScope.launch {
            val updated = customEqPresets.value.filterNot { it.name == preset.name } + preset
            runCatching { repository.saveCustomEqPresets(updated) }
                .onFailure {
                    onMessage("No se pudo guardar el preset EQ: ${it.localizedMessage ?: "error desconocido"}")
                }
        }
    }

    class Factory(private val repository: LibraryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                return LibraryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
