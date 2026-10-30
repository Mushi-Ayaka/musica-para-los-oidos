package com.musica.paralosoidos.ui

import android.widget.Toast
import java.util.Locale

import com.musica.paralosoidos.ui.theme.MusicaStyle
import com.musica.paralosoidos.ui.components.AddTracksToPlaylistDialog
import com.musica.paralosoidos.ui.library.LibraryViewModel
import com.musica.paralosoidos.ui.library.SortOrder
import com.musica.paralosoidos.ui.components.EmptyLibrary
import com.musica.paralosoidos.ui.components.PlaylistDetailHeader
import com.musica.paralosoidos.ui.components.PlaylistGalleryCard
import com.musica.paralosoidos.ui.components.PlaylistImportConflictDialog
import com.musica.paralosoidos.ui.components.PlaylistTrackRow
import com.musica.paralosoidos.ui.components.SectionTitle
import com.musica.paralosoidos.ui.components.ThemeSettingsSection
import com.musica.paralosoidos.ui.components.TrackRow
import com.musica.paralosoidos.ui.equalizer.EqualizerScreenContent
import com.musica.paralosoidos.ui.player.FullPlayer
import com.musica.paralosoidos.ui.player.PlaybackViewModel
import com.musica.paralosoidos.ui.player.MiniPlayer

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.AppContainer
import com.musica.paralosoidos.data.AudioOutputProfileIds
import com.musica.paralosoidos.data.PlaylistTextCodec
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Checkbox
import com.musica.paralosoidos.data.PlaylistImportPlan
import com.musica.paralosoidos.data.AmbiguousTrackConflict
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.playback.AudioEffectsManager
import com.musica.paralosoidos.playback.EqualizerBandMapper
import com.musica.paralosoidos.playback.MAX_GAIN_DB
import com.musica.paralosoidos.playback.MIN_GAIN_DB
import com.musica.paralosoidos.playback.LyricsFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AppTab(val label: String) {
    Home("Inicio"),
    Library("Biblioteca"),
    Equalizer("Ecualizador"),
    Settings("Ajustes");

    companion object {
        val visibleTabs = listOf(Home, Library, Equalizer, Settings)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicaRoot(
    container: AppContainer,
    controller: MediaController?,
    hasAudioPermission: () -> Boolean,
    requestAudioPermission: () -> Unit
) {
    val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory(container.libraryRepository))
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val themeSettings by libraryViewModel.themeSettings.collectAsStateWithLifecycle()
    val customEqPresets by libraryViewModel.customEqPresets.collectAsStateWithLifecycle()
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    val searchQuery by libraryViewModel.searchQuery.collectAsStateWithLifecycle()
    val librarySubTab by libraryViewModel.librarySubTab.collectAsStateWithLifecycle()
    val currentSortOrder by libraryViewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedLetterFilter by libraryViewModel.letterFilter.collectAsStateWithLifecycle()
    val scanMessage by libraryViewModel.scanMessage.collectAsStateWithLifecycle()
    val filteredTracks by libraryViewModel.filteredTracks.collectAsStateWithLifecycle()
    val albums by libraryViewModel.albums.collectAsStateWithLifecycle()

    val playbackViewModel: PlaybackViewModel = viewModel(factory = PlaybackViewModel.Factory(container.libraryRepository))
    val nowPlaying by playbackViewModel.nowPlaying.collectAsStateWithLifecycle()
    val isPlaying by playbackViewModel.isPlaying.collectAsStateWithLifecycle()
    val position by playbackViewModel.position.collectAsStateWithLifecycle()
    val duration by playbackViewModel.duration.collectAsStateWithLifecycle()
    val playbackSpeed by playbackViewModel.playbackSpeed.collectAsStateWithLifecycle()
    val shuffleEnabled by playbackViewModel.shuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by playbackViewModel.repeatMode.collectAsStateWithLifecycle()
    val playbackError by playbackViewModel.playbackError.collectAsStateWithLifecycle()
    val playbackQueue by playbackViewModel.queue.collectAsStateWithLifecycle()

    var selectedTabName by rememberSaveable { mutableStateOf(AppTab.Home.name) }
    val selectedTab = runCatching { AppTab.valueOf(selectedTabName) }.getOrDefault(AppTab.Home)
    var selectedPlaylistId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedPlaylistForDetail = playlists.firstOrNull { it.id == selectedPlaylistId }
    var showAddTracksDialog by rememberSaveable { mutableStateOf(false) }
    var pendingImportPlan by remember { mutableStateOf<PlaylistImportPlan?>(null) }
    var audioOutputProfileMessage by remember { mutableStateOf<String?>(null) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }
    var themeMessage by remember { mutableStateOf<String?>(null) }

    var showNewPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var newPlaylistName by rememberSaveable { mutableStateOf("") }
    var playlistMessage by remember { mutableStateOf<String?>(null) }
    val autoBackupMessage by container.autoBackup.lastMessage.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(autoBackupMessage) {
        autoBackupMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            container.autoBackup.clearMessage()
        }
    }
    val exportTrackNames = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)
                            ?.use { it.write(PlaylistTextCodec.exportNamesAsText(tracks)) }
                            ?: error("No se pudo crear el archivo de texto.")
                    }
                }.onSuccess { playlistMessage = "Se exportaron los nombres de las canciones." }
                    .onFailure { playlistMessage = "No se pudo exportar: ${it.localizedMessage ?: "error desconocido"}" }
            }
        }
    }
    val importPlaylistJson = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                            ?: error("No se pudo leer el archivo seleccionado.")
                    }
                    val plan = PlaylistTextCodec.analyzePlaylistJson(content, tracks)
                    if (plan.conflicts.isEmpty()) {
                        val imported = PlaylistTextCodec.createPlaylist(plan.playlistName, plan.autoResolvedTrackIds)
                        libraryViewModel.upsertPlaylist(imported) { success, message ->
                            playlistMessage = if (success) {
                                "Lista '${plan.playlistName}' importada correctamente (${plan.autoResolvedTrackIds.size} pistas)."
                            } else {
                                message
                            }
                        }
                    } else {
                        pendingImportPlan = plan
                    }
                }.onFailure {
                    playlistMessage = "No se pudo importar la lista: ${it.localizedMessage ?: "error desconocido"}"
                }
            }
        }
    }
    val storedOutputProfileId by container.audioOutputProfileSettings.profileId.collectAsStateWithLifecycle(
        initialValue = AudioOutputProfileIds.STUDIO_MONITOR
    )
    val tracksByIdLong = remember(tracks) { tracks.associateBy { it.id } }
    val audioGranted = hasAudioPermission()

    LaunchedEffect(customEqPresets) {
        customEqPresets.forEach(AudioEffectsManager::registerCustomPreset)
    }

    LaunchedEffect(storedOutputProfileId) {
        AudioEffectsManager.setOutputProfile(storedOutputProfileId)
    }

    fun saveOutputProfile(profileId: String) {
        AudioEffectsManager.setOutputProfile(profileId)
        audioOutputProfileMessage = null
        coroutineScope.launch {
            runCatching {
                container.audioOutputProfileSettings.save(profileId)
            }.onFailure {
                audioOutputProfileMessage =
                    "No se pudo guardar el perfil: ${it.localizedMessage ?: "error desconocido"}"
            }
        }
    }

    LaunchedEffect(controller, audioGranted) {
        if (audioGranted) libraryViewModel.refreshLibrary()
    }

    DisposableEffect(controller) {
        if (controller != null) playbackViewModel.attachController(controller)
        onDispose { playbackViewModel.detachController() }
    }

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
    Box(Modifier.fillMaxSize()) {
        if (themeSettings.wallpaperUri.isNotBlank()) {
            AsyncImage(
                model = themeSettings.wallpaperUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(themeSettings.blurDp.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = themeSettings.dimAmount))
            )
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MUSICA PARA LOS OIDOS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column {
                nowPlaying?.let { track ->
                    MiniPlayer(
                        track = track,
                        playing = isPlaying,
                        onOpen = { showPlayer = true },
                        onPlayPause = { playbackViewModel.togglePlayPause() },
                        onNext = { playbackViewModel.next() },
                        progress = { (position / duration).coerceIn(0f, 1f) }
                    )
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 0.dp
                ) {
                    AppTab.visibleTabs.forEach { tab ->
                        val icon = when (tab) {
                            AppTab.Home -> Icons.Default.MusicNote
                            AppTab.Library -> Icons.AutoMirrored.Filled.QueueMusic
                            AppTab.Equalizer -> Icons.Default.Tune
                            AppTab.Settings -> Icons.Default.Palette
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTabName = tab.name
                                showPlayer = false
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = {
                                Text(
                                    tab.label,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                AppTab.Home -> {
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 4.dp)
                        ) {
                            Text(
                                "MUSICA PARA LOS OIDOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.1.sp
                            )
                            Text(
                                "Inicio",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    val featuredTrack = nowPlaying ?: tracks.firstOrNull()
                    if (featuredTrack != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { playbackViewModel.playTrack(featuredTrack, tracks) },
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

                                        // Degradado transparente
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
                                onScan = {
                                    if (!hasAudioPermission()) requestAudioPermission()
                                    else libraryViewModel.refreshLibrary(force = true)
                                }
                            )
                        }
                    } else {
                        items(tracks.take(6), key = { "home-${it.id}" }) { track ->
                            TrackRow(
                                track = track,
                                onClick = { playbackViewModel.playTrack(track, tracks) }
                            )
                        }
                    }

                    playbackError?.let { error ->
                        item { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                }

                AppTab.Library -> {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SectionTitle("Biblioteca", "${filteredTracks.size} de ${tracks.size} pistas")
                            Button(
                                onClick = {
                                    if (!hasAudioPermission()) requestAudioPermission()
                                    else libraryViewModel.refreshLibrary(force = true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Actualizar", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Pistas", "Álbumes", "Listas").forEach { section ->
                                FilterChip(
                                    selected = librarySubTab == section,
                                    onClick = { libraryViewModel.setLibrarySubTab(section) },
                                    label = { Text(section) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    if (librarySubTab == "Álbumes") {
                        if (albums.isEmpty()) {
                            item { Text("No hay álbumes disponibles en la biblioteca.") }
                        } else {
                            items(albums, key = { "album-${it.first}" }) { (albumName, albumTracks) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        playbackViewModel.playTrack(albumTracks.first(), albumTracks)
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

                    if (librarySubTab == "Listas") {
                        val activePlaylist = selectedPlaylistForDetail
                        if (activePlaylist != null) {
                            item {
                                PlaylistDetailHeader(
                                    playlist = activePlaylist,
                                    allTracksById = tracksByIdLong,
                                    onBack = { selectedPlaylistId = null },
                                    onPlayAll = { playlistTracks ->
                                        if (playlistTracks.isNotEmpty()) playbackViewModel.playTrack(playlistTracks.first(), playlistTracks)
                                    },
                                    onShuffle = { playlistTracks ->
                                        if (playlistTracks.isNotEmpty()) {
                                            val shuffled = playlistTracks.shuffled()
                                            playbackViewModel.playTrack(shuffled.first(), shuffled)
                                        }
                                    },
                                    onAddSongsClick = { showAddTracksDialog = true },
                                    onDeletePlaylist = {
                                        libraryViewModel.deletePlaylist(activePlaylist.id) { message ->
                                            selectedPlaylistId = null
                                            playlistMessage = message
                                        }
                                    }
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
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                        onPlay = { playbackViewModel.playTrack(track, playlistTracks) },
                                        onMoveUp = {
                                            libraryViewModel.movePlaylistTrack(
                                                activePlaylist.id,
                                                track.id,
                                                -1
                                            ) { playlistMessage = it }
                                        },
                                        onMoveDown = {
                                            libraryViewModel.movePlaylistTrack(
                                                activePlaylist.id,
                                                track.id,
                                                1
                                            ) { playlistMessage = it }
                                        },
                                        onRemove = {
                                            libraryViewModel.removePlaylistTrack(
                                                activePlaylist.id,
                                                track.id
                                            ) { playlistMessage = it }
                                        }
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
                                    Button(onClick = { showNewPlaylistDialog = true }) {
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
                                        onClick = { selectedPlaylistId = playlist.id },
                                        onPlay = { plTracks ->
                                            if (plTracks.isNotEmpty()) playbackViewModel.playTrack(plTracks.first(), plTracks)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (librarySubTab == "Pistas") {
                    // Buscador en tiempo real
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { libraryViewModel.setSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar pistas, artistas o albumes...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { libraryViewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar busqueda")
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

                    // Barra de orden alfabetico
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Orden:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            SortOrder.entries.forEach { order ->
                                FilterChip(
                                    selected = currentSortOrder == order,
                                    onClick = { libraryViewModel.setSortOrder(order) },
                                    label = { Text(order.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    // Jump bar alfabetico horizontal
                    item {
                        LazyRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                SuggestionChip(
                                    onClick = { libraryViewModel.setLetterFilter(null) },
                                    label = { Text("Todos", fontSize = 11.sp) },
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
                                    onClick = {
                                        libraryViewModel.setLetterFilter(if (isSelected) null else char)
                                    },
                                    label = { Text(char.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                        item { Text(scanMessage!!, color = MaterialTheme.colorScheme.secondary) }
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
                            TrackRow(
                                track = track,
                                onClick = { playbackViewModel.playTrack(track, filteredTracks) }
                            )
                        }
                    }
                    }

                    playbackError?.let { error ->
                        item { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                }

                AppTab.Equalizer -> {
                    item {
                        EqualizerScreenContent(
                            controller = controller,
                            onOutputProfileSelected = ::saveOutputProfile,
                            outputProfileMessage = audioOutputProfileMessage,
                            onSaveCustomPreset = { preset ->
                                libraryViewModel.saveCustomEqPreset(preset) { themeMessage = it }
                            }
                        )
                    }
                }

                AppTab.Settings -> {
                    item {
                        ThemeSettingsSection(
                            savedSettings = themeSettings,
                            onSave = { settings ->
                                libraryViewModel.saveThemeSettings(settings) { themeMessage = it }
                            }
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle("Biblioteca", "Exporta nombres o crea una lista desde un archivo JSON.")
                            OutlinedButton(
                                onClick = { exportTrackNames.launch("canciones.txt") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Exportar nombres de canciones (.txt)")
                            }
                            OutlinedButton(
                                onClick = { importPlaylistJson.launch(arrayOf("application/json", "text/*")) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Importar lista desde JSON")
                            }
                            Text(
                                """Formato JSON: {"name":"Favoritas","songs":["Cancion 1","Cancion 2"]}""",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            playlistMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    }
                    themeMessage?.let { message ->
                        item { Text(message, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    }

    // Modal FullPlayer en raiz
    if (showPlayer && nowPlaying != null) {
        val playerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPlayer = false },
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = playerSheetState
        ) {
            FullPlayer(
                track = nowPlaying!!,
                queue = playbackQueue,
                allTracks = tracks,
                controller = controller,
                playing = isPlaying,
                position = { position },
                duration = { duration },
                shuffleEnabled = shuffleEnabled,
                repeatMode = repeatMode,
                playbackSpeed = playbackSpeed,
                onSpeedChange = { speed -> playbackViewModel.setSpeed(speed) },
                error = playbackError,
                onSelectQueueItem = { index -> playbackViewModel.seekTo(index) },
                onMoveQueueItem = { from, to -> playbackViewModel.moveQueueItem(from, to) },
                onRemoveQueueItem = { index -> playbackViewModel.removeQueueItem(index) },
                onAddQueueItem = { track -> playbackViewModel.addQueueItem(track) },
                onClose = { showPlayer = false }
            )
        }
    }

    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = { Text("Crear lista") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newPlaylistName.isNotBlank(),
                    onClick = {
                        val playlist = com.musica.paralosoidos.data.MusicPlaylist(
                            id = java.util.UUID.randomUUID().toString(),
                            name = newPlaylistName.trim(),
                            trackIds = emptyList()
                        )
                        libraryViewModel.upsertPlaylist(playlist) { success, message ->
                            if (success) {
                                playlistMessage = null
                                showNewPlaylistDialog = false
                                newPlaylistName = ""
                            } else {
                                playlistMessage = message
                            }
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddTracksDialog && selectedPlaylistForDetail != null) {
        val activePl = selectedPlaylistForDetail!!
        AddTracksToPlaylistDialog(
            playlistName = activePl.name,
            existingTrackIds = activePl.trackIds,
            allTracks = tracks,
            onDismiss = { showAddTracksDialog = false },
            onAddTracks = { addedTracks ->
                val updatedIds = (activePl.trackIds + addedTracks.map { it.id }).distinct()
                val updatedPlaylist = activePl.copy(trackIds = updatedIds)
                libraryViewModel.upsertPlaylist(updatedPlaylist) { success, message ->
                    if (success) {
                        selectedPlaylistId = updatedPlaylist.id
                        showAddTracksDialog = false
                        playlistMessage = "Se añadieron ${addedTracks.size} canciones a '${activePl.name}'."
                    } else {
                        playlistMessage = message
                    }
                }
            }
        )
    }

    pendingImportPlan?.let { plan ->
        PlaylistImportConflictDialog(
            plan = plan,
            onDismiss = { pendingImportPlan = null },
            onConfirm = { resolvedTrackIds ->
                val newPlaylist = PlaylistTextCodec.createPlaylist(plan.playlistName, resolvedTrackIds)
                libraryViewModel.upsertPlaylist(newPlaylist) { success, message ->
                    if (success) {
                        playlistMessage = "Lista '${plan.playlistName}' importada con ${resolvedTrackIds.size} canciones."
                        pendingImportPlan = null
                    } else {
                        playlistMessage = message
                    }
                }
            }
        )
    }
    }
}
