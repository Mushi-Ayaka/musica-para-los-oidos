package com.musica.paralosoidos.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.FolderItem
import com.musica.paralosoidos.data.MusicPlaylist
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.tracker.TrackTrackerManager
import com.musica.paralosoidos.ui.components.FolderRow
import com.musica.paralosoidos.ui.components.PlaylistDetailHeader
import com.musica.paralosoidos.ui.components.PlaylistGalleryCard
import com.musica.paralosoidos.ui.components.PlaylistTrackRow
import com.musica.paralosoidos.ui.components.TrackRow
import com.musica.paralosoidos.ui.design.SectionTitle
import com.musica.paralosoidos.ui.library.SortOrder
import com.musica.paralosoidos.ui.theme.MusicaStyle

@Composable
fun LibraryScreen(
    tracks: List<TrackEntity>,
    filteredTracks: List<TrackEntity>,
    tracksByIdLong: Map<Long, TrackEntity>,
    albums: List<Pair<String, List<TrackEntity>>>,
    playlists: List<MusicPlaylist>,
    folderItems: List<FolderItem>,
    selectedFolderPath: String?,
    selectedFolderTracks: List<TrackEntity>,
    selectedPlaylistForDetail: MusicPlaylist?,
    librarySubTab: String,
    searchQuery: String,
    currentSortOrder: SortOrder,
    selectedLetterFilter: Char?,
    nowPlaying: TrackEntity?,
    catalogMap: Map<String, CatalogTrackEntity>,
    trackerCircleColor: Color,
    trackTrackerManager: TrackTrackerManager,
    scanMessage: String?,
    playlistMessage: String?,
    playbackError: String?,
    onRefreshLibrary: () -> Unit,
    onSelectSubTab: (String) -> Unit,
    onSelectFolderPath: (String?) -> Unit,
    onToggleFolderHidden: (String, Boolean) -> Unit,
    onSelectPlaylist: (MusicPlaylist?) -> Unit,
    onOpenNewPlaylistDialog: () -> Unit,
    onOpenAddTracksDialog: () -> Unit,
    onDeletePlaylist: (String) -> Unit,
    onPickPlaylistCover: () -> Unit,
    onClearPlaylistCover: (MusicPlaylist) -> Unit,
    onMovePlaylistTrack: (String, Long, Int) -> Unit,
    onRemovePlaylistTrack: (String, Long) -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onPlayPlaylist: (MusicPlaylist, TrackEntity, List<TrackEntity>) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onLetterFilterChange: (Char?) -> Unit,
    onOpenTrackLinks: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionTitle("Biblioteca", "${filteredTracks.size} de ${tracks.size} pistas")
                IconButton(onClick = onRefreshLibrary) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar biblioteca", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Pistas", "Álbumes", "Listas", "Carpetas").forEach { section ->
                    FilterChip(
                        selected = librarySubTab == section,
                        onClick = {
                            onSelectSubTab(section)
                            if (section == "Carpetas") {
                                onSelectFolderPath(null)
                            }
                        },
                        label = { Text(section, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        when (librarySubTab) {
            "Álbumes" -> {
                if (albums.isEmpty()) {
                    item { Text("No hay álbumes disponibles en la biblioteca.") }
                } else {
                    items(albums, key = { "album-${it.first}" }) { (albumName, albumTracks) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onPlayTrack(albumTracks.first(), albumTracks)
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(albumName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${albumTracks.size} pistas · ${albumTracks.first().artist}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir álbum")
                            }
                        }
                    }
                }
            }

            "Carpetas" -> {
                if (selectedFolderPath == null) {
                    item {
                        Text(
                            "Fuentes y Carpetas de Audio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    if (folderItems.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No se detectaron fuentes de audio.", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = onRefreshLibrary) {
                                        Text("Escanear almacenamiento")
                                    }
                                }
                            }
                        }
                    } else {
                        items(folderItems, key = { "folder-${it.path}" }) { folder ->
                            FolderRow(
                                folder = folder,
                                onClick = { onSelectFolderPath(folder.path) },
                                onToggleHidden = { hidden -> onToggleFolderHidden(folder.path, hidden) }
                            )
                        }
                    }
                } else {
                    val currentFolder = folderItems.firstOrNull { it.path == selectedFolderPath }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MusicaStyle.Shape.row,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                var detailFolderMenuExpanded by remember { mutableStateOf(false) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onSelectFolderPath(null) }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            currentFolder?.name ?: "Carpeta",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${selectedFolderTracks.size} canciones • $selectedFolderPath",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Box {
                                        IconButton(onClick = { detailFolderMenuExpanded = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones de carpeta", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        DropdownMenu(
                                            expanded = detailFolderMenuExpanded,
                                            onDismissRequest = { detailFolderMenuExpanded = false },
                                            modifier = Modifier.background(MusicaStyle.Colors.menuBackgroundAlt)
                                        ) {
                                            val isHidden = currentFolder?.isHidden == true
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (isHidden) "Mostrar carpeta" else "Ocultar carpeta",
                                                        color = if (isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                        contentDescription = null,
                                                        tint = if (isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                    )
                                                },
                                                onClick = {
                                                    detailFolderMenuExpanded = false
                                                    if (currentFolder != null) {
                                                        onToggleFolderHidden(currentFolder.path, !isHidden)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (selectedFolderTracks.isNotEmpty()) onPlayTrack(selectedFolderTracks.first(), selectedFolderTracks)
                                    },
                                    enabled = selectedFolderTracks.isNotEmpty(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reproducir carpeta")
                                }
                            }
                        }
                    }
                    items(selectedFolderTracks, key = { "fld-trk-${it.id}" }) { track ->
                        val cat = catalogMap[track.uri]
                        val isLinked = !cat?.youtubeUrl.isNullOrBlank() ||
                            (cat?.lrcSynced == true) ||
                            !cat?.lrcPath.isNullOrBlank() ||
                            trackTrackerManager.hasLocalLrcFile(track)
                        TrackRow(
                            track = track,
                            isCurrent = nowPlaying?.id == track.id,
                            onClick = { onPlayTrack(track, selectedFolderTracks) },
                            isLinked = isLinked,
                            trackerColor = trackerCircleColor,
                            onTrackerClick = { onOpenTrackLinks(track) }
                        )
                    }
                }
            }

            "Listas" -> {
                val activePlaylist = selectedPlaylistForDetail
                if (activePlaylist != null) {
                    item {
                        PlaylistDetailHeader(
                            playlist = activePlaylist,
                            allTracksById = tracksByIdLong,
                            onBack = { onSelectPlaylist(null) },
                            onPlayAll = { playlistTracks ->
                                if (playlistTracks.isNotEmpty()) onPlayPlaylist(activePlaylist, playlistTracks.first(), playlistTracks)
                            },
                            onShuffle = { playlistTracks ->
                                if (playlistTracks.isNotEmpty()) {
                                    val shuffled = playlistTracks.shuffled()
                                    onPlayPlaylist(activePlaylist, shuffled.first(), shuffled)
                                }
                            },
                            onAddSongsClick = onOpenAddTracksDialog,
                            onDeletePlaylist = { onDeletePlaylist(activePlaylist.id) },
                            onPickCover = onPickPlaylistCover,
                            onClearCover = { onClearPlaylistCover(activePlaylist) }
                        )
                    }
                    val playlistTracks = activePlaylist.trackIds.mapNotNull(tracksByIdLong::get)
                    val playlistTrackPositions = activePlaylist.trackIds.withIndex().associate { it.value to it.index }
                    if (playlistTracks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Esta lista no tiene canciones aún.\nToca 'Añadir canciones' para incluir música.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(playlistTracks, key = { "pl-trk-${it.id}" }) { track ->
                            val trackIndex = playlistTrackPositions[track.id] ?: 0
                            PlaylistTrackRow(
                                index = trackIndex + 1,
                                track = track,
                                isCurrent = nowPlaying?.id == track.id,
                                canMoveUp = trackIndex > 0,
                                canMoveDown = trackIndex >= 0 && trackIndex < activePlaylist.trackIds.lastIndex,
                                onPlay = { onPlayPlaylist(activePlaylist, track, playlistTracks) },
                                onMoveUp = { onMovePlaylistTrack(activePlaylist.id, track.id, -1) },
                                onMoveDown = { onMovePlaylistTrack(activePlaylist.id, track.id, 1) },
                                onRemove = { onRemovePlaylistTrack(activePlaylist.id, track.id) }
                            )
                        }
                    }
                } else {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tus listas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${playlists.size} listas creadas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(onClick = onOpenNewPlaylistDialog) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Nueva lista")
                            }
                        }
                    }
                    playlistMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
                    if (playlists.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Crea una lista para organizar tus pistas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(playlists, key = { "playlist-${it.id}" }) { playlist ->
                            PlaylistGalleryCard(
                                playlist = playlist,
                                allTracksById = tracksByIdLong,
                                onClick = { onSelectPlaylist(playlist) },
                                onPlay = { plTracks ->
                                    if (plTracks.isNotEmpty()) onPlayPlaylist(playlist, plTracks.first(), plTracks)
                                }
                            )
                        }
                    }
                }
            }

            else -> {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar pistas, artistas o álbumes...", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MusicaStyle.Shape.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MusicaStyle.Colors.overlay,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                item {
                    LazyRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text("Orden:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        items(SortOrder.entries, key = { it.name }) { order ->
                            FilterChip(
                                selected = currentSortOrder == order,
                                onClick = { onSortOrderChange(order) },
                                label = { Text(order.label, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                item {
                    LazyRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            SuggestionChip(
                                onClick = { onLetterFilterChange(null) },
                                label = { Text("Todos", style = MaterialTheme.typography.labelSmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedLetterFilter == null) MaterialTheme.colorScheme.primary else MusicaStyle.Colors.borderStrong,
                                    labelColor = if (selectedLetterFilter == null) Color.Black else Color.White
                                ),
                                border = null
                            )
                        }
                        items(('A'..'Z').toList() + '#') { char ->
                            val isSelected = selectedLetterFilter == char
                            SuggestionChip(
                                onClick = { onLetterFilterChange(if (isSelected) null else char) },
                                label = { Text(char.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MusicaStyle.Colors.border,
                                    labelColor = if (isSelected) Color.Black else Color.White
                                ),
                                border = null
                            )
                        }
                    }
                }

                if (scanMessage != null) {
                    item { Text(scanMessage, color = MaterialTheme.colorScheme.secondary) }
                }

                if (filteredTracks.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No se encontraron pistas con el filtro actual.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(filteredTracks, key = { "lib-${it.id}" }) { track ->
                        val cat = catalogMap[track.uri]
                        val isLinked = !cat?.youtubeUrl.isNullOrBlank() ||
                            (cat?.lrcSynced == true) ||
                            !cat?.lrcPath.isNullOrBlank() ||
                            trackTrackerManager.hasLocalLrcFile(track)
                        TrackRow(
                            track = track,
                            isCurrent = nowPlaying?.id == track.id,
                            onClick = { onPlayTrack(track, filteredTracks) },
                            isLinked = isLinked,
                            trackerColor = trackerCircleColor,
                            onTrackerClick = { onOpenTrackLinks(track) }
                        )
                    }
                }
            }
        }

        playbackError?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
    }
}
