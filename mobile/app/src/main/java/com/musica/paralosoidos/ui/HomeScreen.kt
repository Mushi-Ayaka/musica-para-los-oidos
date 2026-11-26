package com.musica.paralosoidos.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.MusicPlaylist
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.tracker.TrackTrackerManager
import com.musica.paralosoidos.ui.components.EmptyLibrary
import com.musica.paralosoidos.ui.components.PlaylistGalleryCard
import com.musica.paralosoidos.ui.components.TrackRow
import com.musica.paralosoidos.ui.theme.MusicaStyle

@Composable
fun HomeScreen(
    tracks: List<TrackEntity>,
    tracksByIdLong: Map<Long, TrackEntity>,
    playlists: List<MusicPlaylist>,
    playlistsById: Map<String, MusicPlaylist>,
    recentTrackIds: List<Long>,
    recentPlaylistIds: List<String>,
    nowPlaying: TrackEntity?,
    catalogMap: Map<String, CatalogTrackEntity>,
    trackerCircleColor: Color,
    trackTrackerManager: TrackTrackerManager,
    hasAudioPermission: () -> Boolean,
    scanMessage: String?,
    playbackError: String?,
    onRequestPermissionOrRefresh: () -> Unit,
    onPlayTrack: (TrackEntity, List<TrackEntity>) -> Unit,
    onOpenPlaylist: (MusicPlaylist) -> Unit,
    onPlayPlaylist: (MusicPlaylist, List<TrackEntity>) -> Unit,
    onOpenTrackLinks: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val featuredTrack = nowPlaying ?: tracks.firstOrNull()
    val recentTracks = recentTrackIds.mapNotNull { id -> tracksByIdLong[id] }
    val displayTracks = if (recentTracks.isNotEmpty()) recentTracks.take(3) else tracks.take(3)
    val recentPlaylists = recentPlaylistIds.mapNotNull { id -> playlistsById[id] }
    val displayPlaylists = if (recentPlaylists.isNotEmpty()) recentPlaylists.take(3) else playlists.take(3)

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
            ) {
                Text(
                    "Inicio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (featuredTrack != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTrack(featuredTrack, tracks) },
                    shape = MusicaStyle.Shape.large,
                    border = BorderStroke(1.dp, MusicaStyle.Colors.overlay),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(MusicaStyle.Shape.row)
                        ) {
                            if (featuredTrack.artworkUri != null) {
                                AsyncImage(
                                    model = featuredTrack.artworkUri,
                                    contentDescription = "Portada de ${featuredTrack.album}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        modifier = Modifier.size(54.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, MusicaStyle.Colors.scrim)
                                        )
                                    )
                            )
                        }

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    featuredTrack.title,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                            Text(
                                featuredTrack.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Pistas Recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        if (tracks.isEmpty()) {
            item {
                EmptyLibrary(
                    hasPermission = hasAudioPermission(),
                    message = scanMessage,
                    onScan = onRequestPermissionOrRefresh
                )
            }
        } else {
            items(displayTracks, key = { "home-track-${it.id}" }) { track ->
                val cat = catalogMap[track.uri]
                val isLinked = !cat?.youtubeUrl.isNullOrBlank() ||
                    (cat?.lrcSynced == true) ||
                    !cat?.lrcPath.isNullOrBlank() ||
                    trackTrackerManager.hasLocalLrcFile(track)
                TrackRow(
                    track = track,
                    isCurrent = nowPlaying?.id == track.id,
                    onClick = { onPlayTrack(track, tracks) },
                    isLinked = isLinked,
                    trackerColor = trackerCircleColor,
                    onTrackerClick = { onOpenTrackLinks(track) }
                )
            }
        }

        if (displayPlaylists.isNotEmpty()) {
            item {
                Text(
                    "Listas Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }

            items(displayPlaylists, key = { "home-playlist-${it.id}" }) { playlist ->
                PlaylistGalleryCard(
                    playlist = playlist,
                    allTracksById = tracksByIdLong,
                    onClick = { onOpenPlaylist(playlist) },
                    onPlay = { plTracks ->
                        if (plTracks.isNotEmpty()) {
                            onPlayPlaylist(playlist, plTracks)
                        }
                    }
                )
            }
        }

        playbackError?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
    }
}
