package com.musica.paralosoidos.ui

import com.musica.paralosoidos.data.backup.PlaybackBackupState
import com.musica.paralosoidos.ui.components.TrackLinksBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import java.io.File
import kotlinx.coroutines.flow.first

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import com.musica.paralosoidos.ui.downloader.DownloaderScreen
import com.musica.paralosoidos.ui.downloader.DownloaderViewModel
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.AppContainer
import com.musica.paralosoidos.BuildConfig
import com.musica.paralosoidos.data.AudioOutputProfileIds
import com.musica.paralosoidos.data.AutoBackupCodec
import com.musica.paralosoidos.data.DownloadSearchResult
import com.musica.paralosoidos.data.PlaylistImportPlan
import com.musica.paralosoidos.data.PlaylistTextCodec
import com.musica.paralosoidos.data.tracker.TrackTrackerManager
import com.musica.paralosoidos.playback.AudioEffectsManager
import com.musica.paralosoidos.ui.LyricsSettingsSection
import com.musica.paralosoidos.ui.ThemeSettingsSection
import com.musica.paralosoidos.ui.components.AddTracksToPlaylistDialog
import com.musica.paralosoidos.ui.components.EmptyLibrary
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.musica.paralosoidos.ui.components.FolderRow
import com.musica.paralosoidos.ui.components.PlaylistDetailHeader
import com.musica.paralosoidos.ui.components.PlaylistGalleryCard
import com.musica.paralosoidos.ui.components.PlaylistImportConflictDialog
import com.musica.paralosoidos.ui.components.PlaylistTrackRow
import com.musica.paralosoidos.ui.components.TrackRow
import com.musica.paralosoidos.ui.design.SectionTitle
import com.musica.paralosoidos.ui.equalizer.EqualizerScreenContent
import com.musica.paralosoidos.ui.library.LibraryViewModel
import com.musica.paralosoidos.ui.library.SortOrder
import com.musica.paralosoidos.ui.player.FullPlayer
import com.musica.paralosoidos.ui.player.MiniPlayer
import com.musica.paralosoidos.ui.player.PlaybackViewModel
import com.musica.paralosoidos.ui.theme.MusicaStyle
import com.musica.paralosoidos.ui.update.UpdateCircleButton
import com.musica.paralosoidos.ui.update.UpdateDialog
import com.musica.paralosoidos.ui.update.UpdateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AppTab(val label: String) {
    Home("Inicio"),
    Library("Biblioteca"),
    Download("Descargar"),
    Equalizer("Ecualizador"),
    Settings("Ajustes");

    companion object {
        val visibleTabs = listOf(Home, Library, Download, Equalizer, Settings)
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
    val downloaderViewModel: DownloaderViewModel = viewModel(
        factory = DownloaderViewModel.Factory(
            androidx.compose.ui.platform.LocalContext.current.applicationContext,
            container.downloadSettings,
            container.networkMonitor,
            container.downloadQueueManager,
            container.downloadHistoryDao,
            container.youtubeDLRuntime
        )
    )
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val themeSettings by libraryViewModel.themeSettings.collectAsStateWithLifecycle()
    val customEqPresets by libraryViewModel.customEqPresets.collectAsStateWithLifecycle()
    val lyricsScrollSpeed by libraryViewModel.lyricsScrollSpeed.collectAsStateWithLifecycle()
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    val searchQuery by libraryViewModel.searchQuery.collectAsStateWithLifecycle()
    val librarySubTab by libraryViewModel.librarySubTab.collectAsStateWithLifecycle()
    val currentSortOrder by libraryViewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedLetterFilter by libraryViewModel.letterFilter.collectAsStateWithLifecycle()
    val scanMessage by libraryViewModel.scanMessage.collectAsStateWithLifecycle()
    val filteredTracks by libraryViewModel.filteredTracks.collectAsStateWithLifecycle()
    val albums by libraryViewModel.albums.collectAsStateWithLifecycle()
    val visibleTracks by libraryViewModel.visibleTracks.collectAsStateWithLifecycle()
    val folderItems by libraryViewModel.folderItems.collectAsStateWithLifecycle()
    var selectedFolderPath by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedFolderTracks = remember(tracks, selectedFolderPath) { tracks.filter { it.folderPath == selectedFolderPath } }
    val eqEnabled by container.preferences.eqEnabled.collectAsStateWithLifecycle(initialValue = false)
    val activeEqPreset by container.preferences.activeEqPreset.collectAsStateWithLifecycle(initialValue = "Plano (Fidelidad de Estudio)")
    val eqBands by container.preferences.eqBands.collectAsStateWithLifecycle(initialValue = List(10) { 0 })
    val bassBoost by container.preferences.bassBoost.collectAsStateWithLifecycle(initialValue = 0)
    val virtualizer by container.preferences.virtualizer.collectAsStateWithLifecycle(initialValue = 0)
    val speakerBoost by container.preferences.speakerBoost.collectAsStateWithLifecycle(initialValue = 0)
    val mediaVolume by container.preferences.mediaVolume.collectAsStateWithLifecycle(initialValue = 100f)
    val recentTrackIds by container.preferences.recentTrackIds.collectAsStateWithLifecycle(
        initialValue = emptyList(),
        lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    )

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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var playlistMessage by remember { mutableStateOf<String?>(null) }

    var missingTracksFromBackup by remember { mutableStateOf<List<com.musica.paralosoidos.data.backup.TrackMetadataBackup>?>(null) }
    var trackerSyncProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var trackerJob by remember { mutableStateOf<Job?>(null) }
    val isTrackerSyncing = trackerJob != null
    var selectedTrackForLinks by remember { mutableStateOf<com.musica.paralosoidos.data.TrackEntity?>(null) }

    val catalogTracks by container.catalogTracks.observeAll().collectAsStateWithLifecycle(emptyList())
    val catalogMap = remember(catalogTracks) { catalogTracks.associateBy { it.localUri.orEmpty() } }
    val trackerCircleColorHex by container.preferences.trackerCircleColor.collectAsStateWithLifecycle("#FFFFFF")
    val trackerCircleColor = remember(trackerCircleColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(trackerCircleColorHex)) }.getOrDefault(Color.White)
    }

    val trackerFrequency by container.preferences.trackerFrequency.collectAsStateWithLifecycle("ON_NEW_TRACKS_ONLY")
    val trackerIntensity by container.preferences.trackerIntensity.collectAsStateWithLifecycle("MEDIUM")
    val downloadConcurrency by container.preferences.downloadConcurrency.collectAsStateWithLifecycle(1)
    val downloadRetryCount by container.preferences.downloadRetryCount.collectAsStateWithLifecycle(1)
    val autoBackupInterval by container.preferences.autoBackupInterval.collectAsStateWithLifecycle("DAILY")

    fun startTrackerSync(tracksToSync: List<com.musica.paralosoidos.data.TrackEntity>) {
        if (tracksToSync.isEmpty() || trackerJob != null) return
        trackerJob = coroutineScope.launch {
            trackerSyncProgress = 0 to tracksToSync.size
            try {
                container.trackTrackerManager.runAutoTracking(tracksToSync) { current, total ->
                    trackerSyncProgress = current to total
                }
                playlistMessage = "Sincronización de Trackers finalizada."
            } catch (cancelled: CancellationException) {
                playlistMessage = "Sincronización cancelada."
                throw cancelled
            } catch (error: Exception) {
                playlistMessage = "Falló la sincronización: ${error.localizedMessage ?: "error desconocido"}"
            } finally {
                trackerSyncProgress = null
                trackerJob = null
            }
        }
    }

    fun updateRecentTracks(track: com.musica.paralosoidos.data.TrackEntity) {
        coroutineScope.launch {
            // Obtener IDs actuales directamente desde las preferencias
            kotlin.runCatching {
                val recentIds = container.preferences.recentTrackIds.first()
                val currentIds = recentIds.toMutableList()
                // Remover si ya existe
                currentIds.remove(track.id)
                // Agregar al inicio
                currentIds.add(0, track.id)
                // Mantener solo las últimas 3
                container.preferences.saveRecentTrackIds(currentIds.take(3))
            }
        }
    }

    fun updateRecentPlaylists(playlistId: String) {
        coroutineScope.launch {
            // Obtener IDs actuales directamente desde las preferencias
            kotlin.runCatching {
                val recentIds = container.preferences.recentPlaylistIds.first()
                val currentIds = recentIds.toMutableList()
                // Remover si ya existe
                currentIds.remove(playlistId)
                // Agregar al inicio
                currentIds.add(0, playlistId)
                // Mantener solo las últimas 3
                container.preferences.saveRecentPlaylistIds(currentIds.take(3))
            }
        }
    }

    fun playTrackAndUpdateRecent(track: com.musica.paralosoidos.data.TrackEntity, trackList: List<com.musica.paralosoidos.data.TrackEntity>) {
        playbackViewModel.playTrack(track, trackList)
        updateRecentTracks(track)
    }

    LaunchedEffect(visibleTracks, trackerFrequency) {
        if ((trackerFrequency == "ON_APP_OPEN" || trackerFrequency == "ON_NEW_TRACKS_ONLY") &&
            visibleTracks.isNotEmpty()
        ) {
            startTrackerSync(visibleTracks)
        }
    }

    val exportCentralBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val currentPlayback = PlaybackBackupState(
                        queueUris = playbackQueue.map { it.uri },
                        queueTrackTitles = playbackQueue.map { it.title },
                        currentTrackUri = nowPlaying?.uri,
                        currentTrackTitle = nowPlaying?.title,
                        currentPositionMs = position.toLong()
                    )
                    val jsonStr = container.centralBackupManager.exportBundle(currentPlayback)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                            it.write(jsonStr)
                        } ?: error("No se pudo escribir en el archivo.")
                    }
                }.onSuccess { playlistMessage = "Copia completa exportada con exito." }
                    .onFailure { playlistMessage = "Error al exportar copia: ${it.localizedMessage ?: "error desconocido"}" }
            }
        }
    }

    val importCentralBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val content = withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: error("No se pudo leer el archivo.")
                        stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    }
                    val result = container.centralBackupManager.importBundle(content)
                    playlistMessage = "Copia restaurada: ${result.playlistCount} listas y ${result.trackMetadataCount} pistas procesadas."
                    if (result.missingLinkedTracks.isNotEmpty()) {
                        missingTracksFromBackup = result.missingLinkedTracks
                    }
                }.onFailure {
                    playlistMessage = "Error al importar copia: ${it.localizedMessage ?: "formato invalido"}"
                }
            }
        }
    }

    BackHandler(enabled = showPlayer) {
        showPlayer = false
    }

    BackHandler(enabled = selectedFolderPath != null) {
        selectedFolderPath = null
    }

    var showNewPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var newPlaylistName by rememberSaveable { mutableStateOf("") }
    val autoBackupMessage by container.autoBackup.lastMessage.collectAsStateWithLifecycle()
    val updateViewModel: UpdateViewModel = viewModel(
        factory = UpdateViewModel.Factory(
            context = context,
            updateRepository = container.updateRepository,
            currentVersion = BuildConfig.VERSION_NAME
        )
    )

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
    val restoreBackupFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                            ?: error("No se pudo leer la copia seleccionada.")
                    }
                    val backup = AutoBackupCodec.decode(content)
                    val currentTracks = libraryViewModel.tracks.value
                    val currentPlaylists = libraryViewModel.playlists.value
                    val restored = AutoBackupCodec.toPlaylists(backup, currentTracks)
                    val merged = AutoBackupCodec.merge(currentPlaylists, restored)
                    val added = merged.size - currentPlaylists.size
                    if (added <= 0) {
                        playlistMessage = "La copia no añadió listas nuevas."
                    } else {
                        libraryViewModel.replacePlaylists(merged) { success, message ->
                            playlistMessage = if (success) {
                                "Se restauraron $added listas desde la copia."
                            } else {
                                message
                            }
                        }
                    }
                }.onFailure {
                    playlistMessage = "No se pudo restaurar la copia: ${it.localizedMessage ?: "error desconocido"}"
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

    var eqRestored by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(controller, eqEnabled, activeEqPreset, eqBands, bassBoost, virtualizer, speakerBoost) {
        if (!eqRestored && controller != null) {
            AudioEffectsManager.restoreState(
                enabled = eqEnabled,
                preset = activeEqPreset,
                bands = eqBands,
                bassStrength = bassBoost,
                virtualizerStrength = virtualizer,
                speakerBoost = speakerBoost
            )
            controller.volume = (mediaVolume / 100f).coerceIn(0f, 1f)
            eqRestored = true
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && audioGranted) {
                libraryViewModel.verifyAndSyncAudioMismatch()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                actions = {
                    if (selectedTab == AppTab.Home) {
                        UpdateCircleButton(viewModel = updateViewModel)
                    }
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
                            AppTab.Download -> Icons.Default.Download
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
                                    .clickable { playTrackAndUpdateRecent(featuredTrack, tracks) },
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

                    // Calcular pistas recientes directamente
                    val tracksById = tracks.associateBy { it.id }
                    val recentTracks = recentTrackIds.mapNotNull { id -> tracksById[id] }

                    if (recentTracks.isEmpty() && tracks.isEmpty()) {
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
                    } else if (recentTracks.isNotEmpty()) {
                        items(recentTracks, key = { "recent-${it.id}" }) { track ->
                            val cat = catalogMap[track.uri]
                            val isLinked = !cat?.youtubeUrl.isNullOrBlank() || (cat?.lrcSynced == true) || !cat?.lrcPath.isNullOrBlank() || container.trackTrackerManager.hasLocalLrcFile(track)
                            TrackRow(
                                track = track,
                                onClick = { playTrackAndUpdateRecent(track, tracks) },
                                isLinked = isLinked,
                                trackerColor = trackerCircleColor,
                                onTrackerClick = { selectedTrackForLinks = track }
                            )
                        }
                    } else {
                        // Mostrar primeras pistas de la biblioteca si no hay recientes
                        items(tracks.take(6), key = { "home-${it.id}" }) { track ->
                            val cat = catalogMap[track.uri]
                            val isLinked = !cat?.youtubeUrl.isNullOrBlank() || (cat?.lrcSynced == true) || !cat?.lrcPath.isNullOrBlank() || container.trackTrackerManager.hasLocalLrcFile(track)
                            TrackRow(
                                track = track,
                                onClick = { playTrackAndUpdateRecent(track, tracks) },
                                isLinked = isLinked,
                                trackerColor = trackerCircleColor,
                                onTrackerClick = { selectedTrackForLinks = track }
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
                            IconButton(
                                onClick = {
                                    if (!hasAudioPermission()) requestAudioPermission()
                                    else libraryViewModel.refreshLibrary(force = true)
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Actualizar biblioteca", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Pistas", "Álbumes", "Listas", "Carpetas").forEach { section ->
                                FilterChip(
                                    selected = librarySubTab == section,
                                    onClick = {
                                        libraryViewModel.setLibrarySubTab(section)
                                        if (section == "Carpetas") {
                                            selectedFolderPath = null
                                        }
                                    },
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
                                        playTrackAndUpdateRecent(albumTracks.first(), albumTracks)
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

                    if (librarySubTab == "Carpetas") {
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
                                            Button(onClick = { coroutineScope.launch { libraryViewModel.refreshLibrary(force = true) } }) {
                                                Text("Escanear almacenamiento")
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(folderItems, key = { "folder-${it.path}" }) { folder ->
                                    FolderRow(
                                        folder = folder,
                                        onClick = { selectedFolderPath = folder.path },
                                        onToggleHidden = { hidden ->
                                            libraryViewModel.setFolderHidden(folder.path, hidden)
                                        }
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
                                            IconButton(onClick = { selectedFolderPath = null }) {
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
                                                                libraryViewModel.setFolderHidden(currentFolder.path, !isHidden)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                if (selectedFolderTracks.isNotEmpty()) playTrackAndUpdateRecent(selectedFolderTracks.first(), selectedFolderTracks)
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
                                val isLinked = !cat?.youtubeUrl.isNullOrBlank() || (cat?.lrcSynced == true) || !cat?.lrcPath.isNullOrBlank() || container.trackTrackerManager.hasLocalLrcFile(track)
                                TrackRow(
                                    track = track,
                                    onClick = { playTrackAndUpdateRecent(track, selectedFolderTracks) },
                                    isLinked = isLinked,
                                    trackerColor = trackerCircleColor,
                                    onTrackerClick = { selectedTrackForLinks = track }
                                )
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
                            val cat = catalogMap[track.uri]
                            val isLinked = !cat?.youtubeUrl.isNullOrBlank() || (cat?.lrcSynced == true) || !cat?.lrcPath.isNullOrBlank() || container.trackTrackerManager.hasLocalLrcFile(track)
                            TrackRow(
                                track = track,
                                onClick = { playbackViewModel.playTrack(track, filteredTracks) },
                                isLinked = isLinked,
                                trackerColor = trackerCircleColor,
                                onTrackerClick = { selectedTrackForLinks = track }
                            )
                        }
                    }
                    }

                    playbackError?.let { error ->
                        item { Text(error, color = MaterialTheme.colorScheme.error) }
                    }
                }

                AppTab.Download -> {
                    item {
                        DownloaderScreen(viewModel = downloaderViewModel)
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
                            },
                            initialMediaVolume = mediaVolume,
                            onMediaVolumeChange = { vol ->
                                coroutineScope.launch { container.preferences.saveMediaVolume(vol) }
                            },
                            onPersistEqSettings = {
                                coroutineScope.launch {
                                    container.preferences.saveEqEnabled(AudioEffectsManager.isEnabled.value)
                                    container.preferences.saveActiveEqPreset(AudioEffectsManager.selectedPreset.value)
                                    container.preferences.saveEqBands(AudioEffectsManager.bands.value)
                                    container.preferences.saveBassBoost(AudioEffectsManager.bassStrength.value)
                                    container.preferences.saveVirtualizer(AudioEffectsManager.virtualizerStrength.value)
                                    container.preferences.saveSpeakerBoost(AudioEffectsManager.speakerBoost.value)
                                }
                            }
                        )
                    }
                }

                AppTab.Settings -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Ajustes",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Apariencia, letras y copias de seguridad.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    item {
                        ThemeSettingsSection(
                            savedSettings = themeSettings,
                            onSave = { settings ->
                                libraryViewModel.saveThemeSettings(settings) { themeMessage = it }
                            }
                        )
                    }
                    item {
                        LyricsSettingsSection(
                            scrollSpeed = lyricsScrollSpeed,
                            onScrollSpeedChange = { speed ->
                                libraryViewModel.saveLyricsScrollSpeed(speed) { themeMessage = it }
                            }
                        )
                    }
                    // 1. Configuracion de Trackers y Descargas
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle("Trackers y Descargas", "Ajusta la intensidad, frecuencia y apariencia de vinculación y descargas.")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MusicaStyle.Shape.row,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Frecuencia de Tracker
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Frecuencia de Tracker", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(when(trackerFrequency) {
                                                "ON_APP_OPEN" -> "Al abrir la app"
                                                "EVERY_6_HOURS" -> "Cada 6 horas"
                                                "DAILY" -> "Diario"
                                                "MANUAL_ONLY" -> "Solo manual"
                                                else -> "Al añadir canciones"
                                            }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        var freqExpanded by remember { mutableStateOf(false) }
                                        Box {
                                            OutlinedButton(onClick = { freqExpanded = true }) { Text("Cambiar") }
                                            DropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                                                listOf(
                                                    "ON_NEW_TRACKS_ONLY" to "Al añadir canciones",
                                                    "ON_APP_OPEN" to "Al abrir la app",
                                                    "EVERY_6_HOURS" to "Cada 6 horas",
                                                    "DAILY" to "Diario",
                                                    "MANUAL_ONLY" to "Solo manual"
                                                ).forEach { (key, label) ->
                                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                                        freqExpanded = false
                                                        coroutineScope.launch { container.preferences.saveTrackerFrequency(key) }
                                                    })
                                                }
                                            }
                                        }
                                    }

                                    // Intensidad de Tracker
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Intensidad de Tracker", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(when(trackerIntensity) {
                                                "LOW" -> "Baja (ahorro batería/red)"
                                                "HIGH" -> "Alta (rápida)"
                                                else -> "Media (3 en lote)"
                                            }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        var intExpanded by remember { mutableStateOf(false) }
                                        Box {
                                            OutlinedButton(onClick = { intExpanded = true }) { Text("Cambiar") }
                                            DropdownMenu(expanded = intExpanded, onDismissRequest = { intExpanded = false }) {
                                                listOf(
                                                    "LOW" to "Baja (ahorro batería/red)",
                                                    "MEDIUM" to "Media (3 en lote)",
                                                    "HIGH" to "Alta (rápida)"
                                                ).forEach { (key, label) ->
                                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                                        intExpanded = false
                                                        coroutineScope.launch { container.preferences.saveTrackerIntensity(key) }
                                                    })
                                                }
                                            }
                                        }
                                    }

                                    // Color del circulo del Tracker
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Color del círculo del Tracker", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            listOf(
                                                "#FFFFFF" to Color.White,
                                                "#00E5FF" to Color(0xFF00E5FF),
                                                "#00E676" to Color(0xFF00E676),
                                                "#FFEA00" to Color(0xFFFFEA00),
                                                "#E040FB" to Color(0xFFE040FB)
                                            ).forEach { (hex, col) ->
                                                val isSelected = trackerCircleColorHex.equals(hex, ignoreCase = true)
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(col)
                                                        .border(
                                                            width = if (isSelected) 3.dp else 1.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                                            shape = CircleShape
                                                        )
                                                        .clickable {
                                                            coroutineScope.launch { container.preferences.saveTrackerCircleColor(hex) }
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    // Concurrencia de descargas
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Descargas simultáneas: $downloadConcurrency", style = MaterialTheme.typography.bodyMedium)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf(1, 2, 3).forEach { c ->
                                                val sel = downloadConcurrency == c
                                                Button(
                                                    onClick = { coroutineScope.launch { container.preferences.saveDownloadConcurrency(c) } },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                        contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    modifier = Modifier.size(38.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("$c", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    // Selector de carpeta de descarga
                                    val downloadSettingsState by container.downloadSettings.state.collectAsStateWithLifecycle(
                                        initialValue = com.musica.paralosoidos.data.DownloadSettingsState(),
                                        lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
                                    )
                                    val downloadFolderLabel = downloadSettingsState.outputFolderUri?.let { rawUri ->
                                        android.net.Uri.parse(rawUri).lastPathSegment.orEmpty()
                                            .substringAfter(':')
                                            .substringAfterLast('/')
                                    }
                                    val downloadFolderPicker = rememberLauncherForActivityResult(
                                        ActivityResultContracts.OpenDocumentTree()
                                    ) { uri ->
                                        if (uri != null) {
                                            try {
                                                context.contentResolver.takePersistableUriPermission(
                                                    uri,
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                )
                                                coroutineScope.launch {
                                                    container.downloadSettings.saveOutputFolderUri(uri.toString())
                                                }
                                            } catch (error: SecurityException) {
                                                playlistMessage = "No se pudo acceder a la carpeta: ${error.localizedMessage ?: "permiso denegado"}"
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Carpeta de descarga",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                downloadFolderLabel?.let { "Actual: $it" } ?: "Sin configurar",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (downloadFolderLabel != null)
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                else
                                                    MaterialTheme.colorScheme.error
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { downloadFolderPicker.launch(null) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (downloadFolderLabel != null) "Cambiar" else "Elegir")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Copia de Seguridad Centralizada
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle("Copia de Seguridad Centralizada", "Exporta, restaura o sube a la nube todo el estado de la app.")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MusicaStyle.Shape.row,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { exportCentralBackup.launch("musica-para-los-oidos-backup.json") },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Exportar copia completa (.json)")
                                    }

                                    OutlinedButton(
                                        onClick = { importCentralBackup.launch(arrayOf("application/json", "text/*")) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Restaurar copia completa (.json)")
                                    }

                                    OutlinedButton(
                                        onClick = { importPlaylistJson.launch(arrayOf("application/json", "text/*")) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Importar lista de reproducción (.json)")
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                runCatching {
                                                    val cacheFile = File(context.cacheDir, "musica-para-los-oidos-backup.json")
                                                    val jsonStr = container.centralBackupManager.exportBundle()
                                                    cacheFile.writeText(jsonStr, Charsets.UTF_8)
                                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        cacheFile
                                                    )
                                                    val driveIntent = container.centralBackupManager.createGoogleDriveIntent(uri)
                                                    context.startActivity(driveIntent)
                                                }.onFailure {
                                                    playlistMessage = "No se pudo abrir Drive: ${it.localizedMessage ?: "error"}"
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Subir copia a Google Drive (App a App)")
                                    }

                                    // Intervalo de auto backup
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Respaldo periódico automático", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            Text(when(autoBackupInterval) {
                                                "HOURLY" -> "Cada 1 hora"
                                                "SIX_HOURS" -> "Cada 6 horas"
                                                "OFF" -> "Desactivado"
                                                else -> "Diario (24h)"
                                            }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        var autoExpanded by remember { mutableStateOf(false) }
                                        Box {
                                            OutlinedButton(onClick = { autoExpanded = true }) { Text("Cambiar") }
                                            DropdownMenu(expanded = autoExpanded, onDismissRequest = { autoExpanded = false }) {
                                                listOf(
                                                    "DAILY" to "Diario (24h)",
                                                    "SIX_HOURS" to "Cada 6 horas",
                                                    "HOURLY" to "Cada 1 hora",
                                                    "OFF" to "Desactivado"
                                                ).forEach { (key, label) ->
                                                    DropdownMenuItem(text = { Text(label) }, onClick = {
                                                        autoExpanded = false
                                                        coroutineScope.launch { container.preferences.saveAutoBackupInterval(key) }
                                                    })
                                                }
                                            }
                                        }
                                    }

                                    playlistMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }

                    // 3. Estadisticas de Trackers
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle("Estadísticas de Trackers", "Resumen de canciones y letras vinculadas.")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MusicaStyle.Shape.row,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Solo contar entradas que tienen una pista local correspondiente en la biblioteca actual
                                    val localUris = tracks.map { it.uri }.toSet()
                                    val activeCatalog = catalogTracks.filter { it.localUri != null && it.localUri in localUris }
                                    val ytCount = activeCatalog.count { !it.youtubeUrl.isNullOrBlank() }
                                    val lrcCount = activeCatalog.count { it.lrcSynced || !it.lrcPath.isNullOrBlank() }

                                    Text("Canciones en biblioteca: ${tracks.size}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Vinculadas a YouTube: $ytCount / ${tracks.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    Text("Con letra LRC: $lrcCount / ${tracks.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

                                    if (trackerSyncProgress != null) {
                                        val (cur, tot) = trackerSyncProgress!!
                                        Text("Sincronizando: $cur / $tot...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }

                                    Button(
                                        onClick = {
                                            startTrackerSync(visibleTracks)
                                        },
                                        enabled = !isTrackerSyncing && visibleTracks.isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isTrackerSyncing) "Sincronizando..." else "Sincronizar Trackers ahora")
                                    }

                                    if (isTrackerSyncing) {
                                        OutlinedButton(
                                            onClick = { trackerJob?.cancel() },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(Icons.Default.Stop, contentDescription = null)
                                            Spacer(Modifier.width(6.dp))
                                            Text("Detener Tracker")
                                        }
                                    }
                                }
                            }
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

    // Reproductor a pantalla completa sobre la raiz
    if (showPlayer) {
        nowPlaying?.let { currentTrack ->
        FullPlayer(
            track = currentTrack,
            catalogEntry = catalogMap[currentTrack.uri],
            queue = playbackQueue,
            allTracks = tracks,
            controller = controller,
            playing = isPlaying,
            position = { position },
            duration = { duration },
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode,
            playbackSpeed = playbackSpeed,
            lyricsScrollSpeed = lyricsScrollSpeed,
            onSpeedChange = { speed -> playbackViewModel.setSpeed(speed) },
            error = playbackError,
            onSelectQueueItem = { index -> playbackViewModel.seekTo(index) },
            onMoveQueueItem = { from, to -> playbackViewModel.moveQueueItem(from, to) },
            onRemoveQueueItem = { index -> playbackViewModel.removeQueueItem(index) },
            onAddQueueItems = { selected -> playbackViewModel.addQueueItems(selected) },
            onOpenLyricsSettings = {
                showPlayer = false
                selectedTabName = AppTab.Settings.name
            },
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

    UpdateDialog(viewModel = updateViewModel)

    // BottomSheet para ver / editar vinculos de la pista (YouTube & LRC Tracker)
    @OptIn(ExperimentalMaterial3Api::class)
    val linksSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (selectedTrackForLinks != null) {
        val currentTrack = selectedTrackForLinks!!
        val catalogEntry = catalogMap[currentTrack.uri]
        TrackLinksBottomSheet(
            track = currentTrack,
            catalogEntry = catalogEntry,
            sheetState = linksSheetState,
            onDismiss = { selectedTrackForLinks = null },
            onSave = { ytUrl, lrcPath, lrcSynced ->
                coroutineScope.launch {
                    container.saveTrackLinks(currentTrack, ytUrl, lrcPath, lrcSynced)
                }
            },
            onAutoSearchYouTube = {
                container.trackTrackerManager.searchYouTubeCandidate(
                    currentTrack,
                    allowSuspiciousTitle = true
                )
            },
            onAutoSearchLrc = {
                container.trackTrackerManager.searchLrcMatch(currentTrack)
            },
            onDownloadLrc = { lrcContent ->
                container.trackTrackerManager.downloadAndSaveLrc(currentTrack, lrcContent) != null
            },
            onUnlink = {
                coroutineScope.launch {
                    container.saveTrackLinks(currentTrack, null, null, false)
                }
            }
        )
    }

    // Dialogo para restaurar pistas faltantes del respaldo en este dispositivo
    if (missingTracksFromBackup != null) {
        val missing = missingTracksFromBackup!!
        AlertDialog(
            onDismissRequest = { missingTracksFromBackup = null },
            title = { Text("Restaurar canciones del respaldo") },
            text = {
                Text(
                    "Se encontraron ${missing.size} pistas vinculadas a YouTube que no existen en este dispositivo.\n" +
                        "Deseas descargarlas a tu carpeta de musica?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        container.centralBackupManager.enqueueMissingTracks(missing)
                        playlistMessage = "Se agregaron ${missing.size} pistas a la cola de descargas."
                        missingTracksFromBackup = null
                    }
                ) {
                    Text("Descargar todas (${missing.size})")
                }
            },
            dismissButton = {
                TextButton(onClick = { missingTracksFromBackup = null }) {
                    Text("Omitir")
                }
            }
        )
    }
    }
}
