package com.musica.paralosoidos.ui.components
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.musica.paralosoidos.data.FolderItem
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.PlaylistImportPlan
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.ui.theme.MusicaStyle

@Composable
fun EmptyLibrary(
    hasPermission: Boolean,
    message: String?,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                if (hasPermission) "No hay pistas locales todavía." else "Se necesita permiso para leer tu biblioteca musical.",
                style = MaterialTheme.typography.bodyMedium
            )
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onScan) {
                Text(if (hasPermission) "Buscar música" else "Conceder acceso")
            }
        }
    }
}

@Composable
fun TrackRow(
    track: TrackEntity,
    onClick: () -> Unit,
    isLinked: Boolean = false,
    trackerColor: Color = Color.White,
    onTrackerClick: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (track.artworkUri != null) {
            AsyncImage(
                model = track.artworkUri,
                contentDescription = "Portada de ${track.album}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(MusicaStyle.Shape.small),
                contentScale = ContentScale.Crop
            )
        } else {
            Card(
                modifier = Modifier.size(48.dp),
                shape = MusicaStyle.Shape.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                "${track.artist}${track.album.takeIf(String::isNotBlank)?.let { " · $it" }.orEmpty()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (onTrackerClick != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTrackerClick),
                contentAlignment = Alignment.Center
            ) {
                if (isLinked) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(trackerColor)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .border(1.5.dp, trackerColor.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistGalleryCard(
    playlist: com.musica.paralosoidos.data.MusicPlaylist,
    allTracksById: Map<Long, TrackEntity>,
    onClick: () -> Unit,
    onPlay: (List<TrackEntity>) -> Unit
) {
    val selectedTracks = remember(playlist.trackIds, allTracksById) {
        playlist.trackIds.mapNotNull(allTracksById::get)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MusicaStyle.Shape.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MusicaStyle.Colors.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(MusicaStyle.Shape.medium)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), MusicaStyle.Shape.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "${selectedTracks.size} canciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onPlay(selectedTracks) },
                enabled = selectedTracks.isNotEmpty()
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Reproducir lista",
                    tint = if (selectedTracks.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun PlaylistDetailHeader(
    playlist: com.musica.paralosoidos.data.MusicPlaylist,
    allTracksById: Map<Long, TrackEntity>,
    onBack: () -> Unit,
    onPlayAll: (List<TrackEntity>) -> Unit,
    onShuffle: (List<TrackEntity>) -> Unit,
    onAddSongsClick: () -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val selectedTracks = remember(playlist.trackIds, allTracksById) {
        playlist.trackIds.mapNotNull(allTracksById::get)
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a listas")
            }
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar lista", tint = MaterialTheme.colorScheme.error)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MusicaStyle.Shape.card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MusicaStyle.Shape.row)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    val totalDurationMs = remember(selectedTracks) { selectedTracks.sumOf { it.durationMs } }
                    val formattedDuration = remember(totalDurationMs) {
                        val totalSec = totalDurationMs / 1000
                        val hours = totalSec / 3600
                        val minutes = (totalSec % 3600) / 60
                        val seconds = totalSec % 60
                        if (hours > 0) String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds)
                        else String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
                    }
                    Text(playlist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${selectedTracks.size} canciones en total • $formattedDuration duración total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onPlayAll(selectedTracks) },
                enabled = selectedTracks.isNotEmpty(),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reproducir", maxLines = 1, softWrap = false)
            }
            OutlinedButton(
                onClick = { onShuffle(selectedTracks) },
                enabled = selectedTracks.isNotEmpty(),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Aleatorio", maxLines = 1, softWrap = false)
            }
            Button(
                onClick = onAddSongsClick,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir", maxLines = 1, softWrap = false)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar lista") },
            text = { Text("¿Deseas eliminar '${playlist.name}'? No se borrarán los archivos de audio del dispositivo.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePlaylist()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PlaylistTrackRow(
    index: Int,
    track: TrackEntity,
    isCurrent: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MusicaStyle.Shape.tiny)
            .clickable(onClick = onPlay)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index",
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(24.dp)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MusicaStyle.Shape.micro)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!track.artworkUri.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
                val currentCanMoveUp = rememberUpdatedState(canMoveUp)
        val currentCanMoveDown = rememberUpdatedState(canMoveDown)
        val currentOnMoveUp = rememberUpdatedState(onMoveUp)
        val currentOnMoveDown = rememberUpdatedState(onMoveDown)
        val haptic = LocalHapticFeedback.current
        var isDragging by remember { mutableStateOf(false) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Arrastrar para reordenar",
                tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(24.dp)
                    .pointerInput(track.id) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()
                            isDragging = true
                            var totalDragY = 0f
                            val threshold = 36.dp.toPx()
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        break
                                    }
                                    val dragY = change.position.y - change.previousPosition.y
                                    totalDragY += dragY
                                    change.consume()
                                    if (totalDragY <= -threshold && currentCanMoveUp.value) {
                                        currentOnMoveUp.value()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        totalDragY = 0f
                                    } else if (totalDragY >= threshold && currentCanMoveDown.value) {
                                        currentOnMoveDown.value()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        totalDragY = 0f
                                    }
                                }
                            } finally {
                                isDragging = false
                            }
                        }
                    }
            )
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MusicaStyle.Colors.menuBackgroundAlt)
                ) {
                    if (canMoveUp) {
                        DropdownMenuItem(
                            text = { Text("Subir posición") },
                            leadingIcon = {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onMoveUp()
                            }
                        )
                    }
                    if (canMoveDown) {
                        DropdownMenuItem(
                            text = { Text("Bajar posición") },
                            leadingIcon = {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onMoveDown()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Quitar de la lista", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            menuExpanded = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderRow(
    folder: FolderItem,
    onClick: () -> Unit,
    onToggleHidden: (Boolean) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val alpha = if (folder.isHidden) 0.45f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MusicaStyle.Shape.row,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        ),
        border = BorderStroke(
            1.dp,
            if (folder.isHidden) MusicaStyle.Colors.border.copy(alpha = 0.3f) else MusicaStyle.Colors.border
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(MusicaStyle.Shape.medium)
                    .background(
                        if (folder.isHidden) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (folder.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        folder.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                        maxLines = 1
                    )
                    if (folder.isHidden) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "(Oculta)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
                Text(
                    folder.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.8f),
                    maxLines = 1
                )
                Text(
                    "${folder.trackCount} canciones",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opciones de carpeta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(MusicaStyle.Colors.menuBackgroundAlt)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (folder.isHidden) "Mostrar carpeta" else "Ocultar carpeta",
                                color = if (folder.isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (folder.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (folder.isHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleHidden(!folder.isHidden)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddTracksToPlaylistDialog(
    playlistName: String,
    existingTrackIds: List<Long>,
    allTracks: List<TrackEntity>,
    onDismiss: () -> Unit,
    onAddTracks: (List<TrackEntity>) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val availableTracks = remember(allTracks, existingTrackIds) {
        allTracks.filterNot { it.id in existingTrackIds }
    }
    val filteredTracks = remember(availableTracks, search) {
        if (search.isBlank()) availableTracks
        else availableTracks.filter {
            it.title.contains(search, ignoreCase = true) ||
            it.artist.contains(search, ignoreCase = true) ||
            it.album.contains(search, ignoreCase = true)
        }
    }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a '$playlistName'", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Buscar canción o artista") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filteredTracks.size} pistas disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            selectedIds = if (selectedIds.size == filteredTracks.size) {
                                emptySet()
                            } else {
                                filteredTracks.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(if (selectedIds.size == filteredTracks.size) "Deseleccionar todas" else "Seleccionar todas")
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredTracks, key = { "add-${it.id}" }) { track ->
                        val isChecked = track.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MusicaStyle.Shape.tiny)
                                .clickable {
                                    selectedIds = if (isChecked) selectedIds - track.id else selectedIds + track.id
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + track.id else selectedIds - track.id
                                }
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(Modifier.weight(1f)) {
                                Text(track.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                Text("${track.artist} • ${track.album}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedIds.isNotEmpty(),
                onClick = {
                    val tracksToAdd = availableTracks.filter { it.id in selectedIds }
                    onAddTracks(tracksToAdd)
                }
            ) {
                Text("Añadir (${selectedIds.size})")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun PlaylistImportConflictDialog(
    plan: PlaylistImportPlan,
    onDismiss: () -> Unit,
    onConfirm: (List<Long>) -> Unit
) {
    var chosenSelections by remember {
        mutableStateOf<Map<String, Long?>>(
            plan.conflicts.associate { conflict ->
                conflict.songTitle to conflict.candidates.firstOrNull()?.id
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Resolver duplicados en '${plan.playlistName}'", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Se encontraron canciones con versiones múltiples en tu biblioteca. Elige qué pista asociar o si deseas omitirla:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plan.conflicts, key = { "conflict-${it.songTitle}" }) { conflict ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = MusicaStyle.Shape.small
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(conflict.songTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                conflict.candidates.forEach { candidate ->
                                    val isSelected = chosenSelections[conflict.songTitle] == candidate.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MusicaStyle.Shape.micro)
                                            .clickable {
                                                chosenSelections = chosenSelections + (conflict.songTitle to candidate.id)
                                            }
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                chosenSelections = chosenSelections + (conflict.songTitle to candidate.id)
                                            }
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(candidate.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text("${candidate.artist} • ${candidate.album}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            chosenSelections = chosenSelections + (conflict.songTitle to null)
                                        }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = chosenSelections[conflict.songTitle] == null,
                                        onClick = {
                                            chosenSelections = chosenSelections + (conflict.songTitle to null)
                                        }
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Omitir esta canción", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
                if (plan.autoResolvedTrackIds.isNotEmpty()) {
                    Text(
                        "${plan.autoResolvedTrackIds.size} pistas se importarán automáticamente sin conflicto.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val resolvedConflicts = chosenSelections.values.filterNotNull()
                    val finalIds = (plan.autoResolvedTrackIds + resolvedConflicts).distinct()
                    onConfirm(finalIds)
                }
            ) {
                Text("Importar lista")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
