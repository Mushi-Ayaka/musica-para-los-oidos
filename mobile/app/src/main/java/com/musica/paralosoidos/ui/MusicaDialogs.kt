package com.musica.paralosoidos.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.MusicPlaylist
import com.musica.paralosoidos.data.PlaylistImportPlan
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.backup.TrackMetadataBackup
import com.musica.paralosoidos.data.lyrics.LyricsFetchResult
import com.musica.paralosoidos.ui.components.AddTracksToPlaylistDialog
import com.musica.paralosoidos.ui.components.PlaylistImportConflictDialog
import com.musica.paralosoidos.ui.components.TrackLinksBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicaDialogs(
    showNewPlaylistDialog: Boolean,
    newPlaylistName: String,
    onNewPlaylistNameChange: (String) -> Unit,
    onDismissNewPlaylist: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    showAddTracksDialog: Boolean,
    selectedPlaylistForDetail: MusicPlaylist?,
    allTracks: List<TrackEntity>,
    onDismissAddTracks: () -> Unit,
    onAddTracksToPlaylist: (MusicPlaylist, List<TrackEntity>) -> Unit,
    pendingImportPlan: PlaylistImportPlan?,
    onDismissImportPlan: () -> Unit,
    onConfirmImportPlan: (PlaylistImportPlan, List<Long>) -> Unit,
    selectedTrackForLinks: TrackEntity?,
    catalogMap: Map<String, CatalogTrackEntity>,
    onDismissTrackLinks: () -> Unit,
    onSaveTrackLinks: (TrackEntity, String?, String?, Boolean, String?) -> Unit,
    onAutoSearchYouTube: suspend (TrackEntity) -> String?,
    onAutoSearchLrcDetailed: suspend (TrackEntity) -> LyricsFetchResult?,
    onDownloadLrc: suspend (TrackEntity, String, String?) -> Boolean,
    onUnlinkTrack: (TrackEntity) -> Unit,
    missingTracksFromBackup: List<TrackMetadataBackup>?,
    onDismissMissingTracks: () -> Unit,
    onConfirmDownloadMissingTracks: (List<TrackMetadataBackup>) -> Unit
) {
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = onDismissNewPlaylist,
            title = { Text("Crear lista") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = onNewPlaylistNameChange,
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newPlaylistName.isNotBlank(),
                    onClick = { onCreatePlaylist(newPlaylistName.trim()) }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissNewPlaylist) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddTracksDialog && selectedPlaylistForDetail != null) {
        AddTracksToPlaylistDialog(
            playlistName = selectedPlaylistForDetail.name,
            existingTrackIds = selectedPlaylistForDetail.trackIds,
            allTracks = allTracks,
            onDismiss = onDismissAddTracks,
            onAddTracks = { addedTracks ->
                onAddTracksToPlaylist(selectedPlaylistForDetail, addedTracks)
            }
        )
    }

    pendingImportPlan?.let { plan ->
        PlaylistImportConflictDialog(
            plan = plan,
            onDismiss = onDismissImportPlan,
            onConfirm = { resolvedTrackIds ->
                onConfirmImportPlan(plan, resolvedTrackIds)
            }
        )
    }

    val linksSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (selectedTrackForLinks != null) {
        TrackLinksBottomSheet(
            track = selectedTrackForLinks,
            catalogEntry = catalogMap[selectedTrackForLinks.uri],
            sheetState = linksSheetState,
            onDismiss = onDismissTrackLinks,
            onSave = { ytUrl, lrcPath, lrcSynced, lrclibUrl ->
                onSaveTrackLinks(selectedTrackForLinks, ytUrl, lrcPath, lrcSynced, lrclibUrl)
            },
            onAutoSearchYouTube = { onAutoSearchYouTube(selectedTrackForLinks) },
            onAutoSearchLrcDetailed = { onAutoSearchLrcDetailed(selectedTrackForLinks) },
            onDownloadLrc = { lrcContent, lrclibUrl ->
                onDownloadLrc(selectedTrackForLinks, lrcContent, lrclibUrl)
            },
            onUnlink = { onUnlinkTrack(selectedTrackForLinks) }
        )
    }

    if (missingTracksFromBackup != null) {
        AlertDialog(
            onDismissRequest = onDismissMissingTracks,
            title = { Text("Restaurar canciones del respaldo") },
            text = {
                Text(
                    "Se encontraron ${missingTracksFromBackup.size} pistas vinculadas a YouTube que no existen en este dispositivo.\n" +
                        "¿Deseas descargarlas a tu carpeta de música?"
                )
            },
            confirmButton = {
                Button(onClick = { onConfirmDownloadMissingTracks(missingTracksFromBackup) }) {
                    Text("Descargar todas (${missingTracksFromBackup.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissMissingTracks) {
                    Text("Omitir")
                }
            }
        )
    }
}
