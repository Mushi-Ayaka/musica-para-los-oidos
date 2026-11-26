package com.musica.paralosoidos.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.musica.paralosoidos.BuildConfig
import com.musica.paralosoidos.data.AppContainer
import com.musica.paralosoidos.data.AudioOutputProfileIds
import com.musica.paralosoidos.data.DownloadSettingsState
import com.musica.paralosoidos.data.MusicPlaylist
import com.musica.paralosoidos.data.PlaylistImportPlan
import com.musica.paralosoidos.data.PlaylistTextCodec
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.backup.PlaybackBackupState
import com.musica.paralosoidos.data.backup.TrackMetadataBackup
import com.musica.paralosoidos.playback.AudioEffectsManager
import com.musica.paralosoidos.ui.downloader.DownloaderScreen
import com.musica.paralosoidos.ui.downloader.DownloaderViewModel
import com.musica.paralosoidos.ui.equalizer.EqualizerScreenContent
import com.musica.paralosoidos.ui.library.LibraryViewModel
import com.musica.paralosoidos.ui.player.FullPlayer
import com.musica.paralosoidos.ui.player.MiniPlayer
import com.musica.paralosoidos.ui.player.PlaybackViewModel
import com.musica.paralosoidos.ui.update.UpdateCircleButton
import com.musica.paralosoidos.ui.update.UpdateDialog
import com.musica.paralosoidos.ui.update.UpdateViewModel
import com.musica.paralosoidos.widget.MusicWidgetProvider
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory(container.libraryRepository))
    val downloaderViewModel: DownloaderViewModel = viewModel(
        factory = DownloaderViewModel.Factory(
            context.applicationContext,
            container.downloadSettings,
            container.networkMonitor,
            container.downloadQueueManager,
            container.downloadHistoryDao,
            container.youtubeDLRuntime
        )
    )
    val playbackViewModel: PlaybackViewModel = viewModel(factory = PlaybackViewModel.Factory(container.libraryRepository))
    val updateViewModel: UpdateViewModel = viewModel(
        factory = UpdateViewModel.Factory(
            context = context,
            updateRepository = container.updateRepository,
            currentVersion = BuildConfig.VERSION_NAME
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

    val crossfadeSeconds by container.preferences.crossfadeSeconds.collectAsStateWithLifecycle(initialValue = 0)
    val eqEnabled by container.preferences.eqEnabled.collectAsStateWithLifecycle(initialValue = false)
    val activeEqPreset by container.preferences.activeEqPreset.collectAsStateWithLifecycle(initialValue = "Plano (Fidelidad de Estudio)")
    val eqBands by container.preferences.eqBands.collectAsStateWithLifecycle(initialValue = List(10) { 0 })
    val bassBoost by container.preferences.bassBoost.collectAsStateWithLifecycle(initialValue = 0)
    val virtualizer by container.preferences.virtualizer.collectAsStateWithLifecycle(initialValue = 0)
    val speakerBoost by container.preferences.speakerBoost.collectAsStateWithLifecycle(initialValue = 0)
    val mediaVolume by container.preferences.mediaVolume.collectAsStateWithLifecycle(initialValue = 100f)
    val recentTrackIds by container.preferences.recentTrackIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentPlaylistIds by container.preferences.recentPlaylistIds.collectAsStateWithLifecycle(initialValue = emptyList())
    val storedOutputProfileId by container.audioOutputProfileSettings.profileId.collectAsStateWithLifecycle(
        initialValue = AudioOutputProfileIds.STUDIO_MONITOR
    )
    val downloadSettingsState by container.downloadSettings.state.collectAsStateWithLifecycle(
        initialValue = DownloadSettingsState()
    )

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
    var selectedFolderPath by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddTracksDialog by rememberSaveable { mutableStateOf(false) }
    var showNewPlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var newPlaylistName by rememberSaveable { mutableStateOf("") }
    var showPlayer by rememberSaveable { mutableStateOf(false) }

    var pendingImportPlan by remember { mutableStateOf<PlaylistImportPlan?>(null) }
    var audioOutputProfileMessage by remember { mutableStateOf<String?>(null) }
    var themeMessage by remember { mutableStateOf<String?>(null) }
    var playlistMessage by remember { mutableStateOf<String?>(null) }
    var missingTracksFromBackup by remember { mutableStateOf<List<TrackMetadataBackup>?>(null) }
    var trackerSyncProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var trackerJob by remember { mutableStateOf<Job?>(null) }
    val isTrackerSyncing = trackerJob != null
    var selectedTrackForLinks by remember { mutableStateOf<TrackEntity?>(null) }

    val selectedPlaylistForDetail = playlists.firstOrNull { it.id == selectedPlaylistId }
    val selectedFolderTracks = remember(tracks, selectedFolderPath) { tracks.filter { it.folderPath == selectedFolderPath } }
    val tracksByIdLong = remember(tracks) { tracks.associateBy { it.id } }
    val playlistsById = remember(playlists) { playlists.associateBy { it.id } }
    val catalogTracks by container.catalogTracks.observeAll().collectAsStateWithLifecycle(emptyList())
    val catalogMap = remember(catalogTracks) { catalogTracks.associateBy { it.localUri.orEmpty() } }
    val trackerCircleColorHex by container.preferences.trackerCircleColor.collectAsStateWithLifecycle("#FFFFFF")
    val trackerCircleColor = remember(trackerCircleColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(trackerCircleColorHex)) }.getOrDefault(Color.White)
    }

    val trackerFrequency by container.preferences.trackerFrequency.collectAsStateWithLifecycle("ON_NEW_TRACKS_ONLY")
    val trackerIntensity by container.preferences.trackerIntensity.collectAsStateWithLifecycle("MEDIUM")
    val downloadConcurrency by container.preferences.downloadConcurrency.collectAsStateWithLifecycle(1)
    val autoBackupInterval by container.preferences.autoBackupInterval.collectAsStateWithLifecycle("DAILY")
    val autoBackupMessage by container.autoBackup.lastMessage.collectAsStateWithLifecycle()

    val homeListState = rememberLazyListState()
    val libraryListState = rememberLazyListState()
    val settingsListState = rememberLazyListState()

    fun showFeedback(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun startTrackerSync(tracksToSync: List<TrackEntity>) {
        if (tracksToSync.isEmpty() || trackerJob != null) return
        trackerJob = coroutineScope.launch {
            trackerSyncProgress = 0 to tracksToSync.size
            try {
                container.trackTrackerManager.runAutoTracking(tracksToSync) { current, total ->
                    trackerSyncProgress = current to total
                }
                showFeedback("Sincronización de Trackers finalizada.")
            } catch (cancelled: CancellationException) {
                showFeedback("Sincronización cancelada.")
                throw cancelled
            } catch (error: Exception) {
                showFeedback("Falló la sincronización: ${error.localizedMessage ?: "error desconocido"}")
            } finally {
                trackerSyncProgress = null
                trackerJob = null
            }
        }
    }

    fun updateRecentTracks(track: TrackEntity) {
        coroutineScope.launch {
            runCatching {
                val currentIds = container.preferences.recentTrackIds.first().toMutableList()
                currentIds.remove(track.id)
                currentIds.add(0, track.id)
                container.preferences.saveRecentTrackIds(currentIds.take(3))
            }
        }
    }

    fun updateRecentPlaylists(playlistId: String) {
        coroutineScope.launch {
            runCatching {
                val currentIds = container.preferences.recentPlaylistIds.first().toMutableList()
                currentIds.remove(playlistId)
                currentIds.add(0, playlistId)
                container.preferences.saveRecentPlaylistIds(currentIds.take(3))
            }
        }
    }

    fun playTrackAndUpdateRecent(track: TrackEntity, trackList: List<TrackEntity>) {
        playbackViewModel.playTrack(track, trackList)
        updateRecentTracks(track)
        coroutineScope.launch {
            container.preferences.recordTrackPlayed(track.id)
        }
    }

    LaunchedEffect(nowPlaying?.id) {
        val current = nowPlaying ?: return@LaunchedEffect
        updateRecentTracks(current)
    }

    LaunchedEffect(visibleTracks, trackerFrequency) {
        if ((trackerFrequency == "ON_APP_OPEN" || trackerFrequency == "ON_NEW_TRACKS_ONLY") &&
            visibleTracks.isNotEmpty()
        ) {
            startTrackerSync(visibleTracks)
        }
    }

    LaunchedEffect(autoBackupMessage) {
        autoBackupMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            container.autoBackup.clearMessage()
        }
    }

    LaunchedEffect(playlistMessage) {
        playlistMessage?.let {
            showFeedback(it)
            playlistMessage = null
        }
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
                    showFeedback("Carpeta de descarga guardada.")
                }
            } catch (error: SecurityException) {
                showFeedback("No se pudo acceder a la carpeta: ${error.localizedMessage ?: "permiso denegado"}")
            }
        }
    }

    val exportCentralBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
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
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            container.centralBackupManager.exportZipBundle(out, currentPlayback)
                        } ?: error("No se pudo escribir en el archivo.")
                    }
                }.onSuccess { showFeedback("Copia completa exportada con éxito.") }
                    .onFailure { showFeedback("Error al exportar copia: ${it.localizedMessage ?: "error desconocido"}") }
            }
        }
    }

    val importCentralBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val result = withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: error("No se pudo leer el archivo.")
                        stream.use { container.centralBackupManager.importBundleStream(it) }
                    }
                    showFeedback("Copia restaurada: ${result.playlistCount} listas y ${result.trackMetadataCount} pistas procesadas.")
                    if (result.missingLinkedTracks.isNotEmpty()) {
                        missingTracksFromBackup = result.missingLinkedTracks
                    }
                }.onFailure {
                    showFeedback("Error al importar copia: ${it.localizedMessage ?: "formato inválido"}")
                }
            }
        }
    }

    val importThemesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                runCatching {
                    val count = withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: error("No se pudo leer el archivo.")
                        stream.use { container.centralBackupManager.importThemes(it) }
                    }
                    showFeedback(if (count > 0) "Se importaron $count temas con éxito." else "No se encontraron temas nuevos.")
                }.onFailure {
                    showFeedback("Error al importar temas: ${it.localizedMessage ?: "error desconocido"}")
                }
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
                            showFeedback(
                                if (success) "Lista '${plan.playlistName}' importada (${plan.autoResolvedTrackIds.size} pistas)."
                                else (message ?: "No se pudo importar la lista.")
                            )
                        }
                    } else {
                        pendingImportPlan = plan
                    }
                }.onFailure {
                    showFeedback("No se pudo importar la lista: ${it.localizedMessage ?: "error desconocido"}")
                }
            }
        }
    }

    val pickPlaylistCover = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val currentPl = selectedPlaylistForDetail
        if (uri != null && currentPl != null) {
            coroutineScope.launch {
                runCatching {
                    val coversDir = File(context.filesDir, "playlist_covers").apply { mkdirs() }
                    val destFile = File(coversDir, "cover_${currentPl.id}.jpg")
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destFile.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                    val fileUri = Uri.fromFile(destFile).toString()
                    libraryViewModel.upsertPlaylist(currentPl.copy(coverUri = fileUri)) { ok, err ->
                        if (!ok && err != null) showFeedback(err)
                    }
                }.onFailure {
                    showFeedback("Error al guardar portada: ${it.localizedMessage ?: "error"}")
                }
            }
        }
    }

    BackHandler(enabled = showPlayer) {
        showPlayer = false
    }

    BackHandler(enabled = !showPlayer && selectedTab == AppTab.Library && selectedPlaylistId != null && librarySubTab == "Listas") {
        selectedPlaylistId = null
    }

    BackHandler(enabled = !showPlayer && selectedTab == AppTab.Library && selectedFolderPath != null && librarySubTab == "Carpetas") {
        selectedFolderPath = null
    }

    val audioGranted = hasAudioPermission()

    LaunchedEffect(customEqPresets) {
        customEqPresets.forEach(AudioEffectsManager::registerCustomPreset)
    }

    LaunchedEffect(storedOutputProfileId) {
        AudioEffectsManager.setOutputProfile(storedOutputProfileId)
    }

    LaunchedEffect(selectedTabName) {
        if (selectedTabName == AppTab.Library.name) {
            libraryViewModel.refreshLibrary(force = true)
        } else if (selectedTabName == AppTab.Download.name) {
            downloaderViewModel.checkAndUpdateYoutubeDL()
        }
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && audioGranted) {
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

    val downloadFolderLabel = downloadSettingsState.outputFolderUri?.let { rawUri ->
        Uri.parse(rawUri).lastPathSegment.orEmpty()
            .substringAfter(':')
            .substringAfterLast('/')
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
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "MÚSICA PARA LOS OÍDOS",
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when (selectedTab) {
                        AppTab.Home -> HomeScreen(
                            tracks = tracks,
                            tracksByIdLong = tracksByIdLong,
                            playlists = playlists,
                            playlistsById = playlistsById,
                            recentTrackIds = recentTrackIds,
                            recentPlaylistIds = recentPlaylistIds,
                            nowPlaying = nowPlaying,
                            catalogMap = catalogMap,
                            trackerCircleColor = trackerCircleColor,
                            trackTrackerManager = container.trackTrackerManager,
                            hasAudioPermission = hasAudioPermission,
                            scanMessage = scanMessage,
                            playbackError = playbackError,
                            onRequestPermissionOrRefresh = {
                                if (!hasAudioPermission()) requestAudioPermission()
                                else libraryViewModel.refreshLibrary(force = true)
                            },
                            onPlayTrack = ::playTrackAndUpdateRecent,
                            onOpenPlaylist = { playlist ->
                                selectedPlaylistId = playlist.id
                                libraryViewModel.setLibrarySubTab("Listas")
                                selectedTabName = AppTab.Library.name
                                updateRecentPlaylists(playlist.id)
                            },
                            onPlayPlaylist = { playlist, plTracks ->
                                playTrackAndUpdateRecent(plTracks.first(), plTracks)
                                updateRecentPlaylists(playlist.id)
                            },
                            onOpenTrackLinks = { selectedTrackForLinks = it },
                            listState = homeListState
                        )

                        AppTab.Library -> LibraryScreen(
                            tracks = tracks,
                            filteredTracks = filteredTracks,
                            tracksByIdLong = tracksByIdLong,
                            albums = albums,
                            playlists = playlists,
                            folderItems = folderItems,
                            selectedFolderPath = selectedFolderPath,
                            selectedFolderTracks = selectedFolderTracks,
                            selectedPlaylistForDetail = selectedPlaylistForDetail,
                            librarySubTab = librarySubTab,
                            searchQuery = searchQuery,
                            currentSortOrder = currentSortOrder,
                            selectedLetterFilter = selectedLetterFilter,
                            nowPlaying = nowPlaying,
                            catalogMap = catalogMap,
                            trackerCircleColor = trackerCircleColor,
                            trackTrackerManager = container.trackTrackerManager,
                            scanMessage = scanMessage,
                            playlistMessage = playlistMessage,
                            playbackError = playbackError,
                            onRefreshLibrary = {
                                if (!hasAudioPermission()) requestAudioPermission()
                                else libraryViewModel.refreshLibrary(force = true)
                            },
                            onSelectSubTab = libraryViewModel::setLibrarySubTab,
                            onSelectFolderPath = { selectedFolderPath = it },
                            onToggleFolderHidden = libraryViewModel::setFolderHidden,
                            onSelectPlaylist = { pl ->
                                selectedPlaylistId = pl?.id
                                if (pl != null) updateRecentPlaylists(pl.id)
                            },
                            onOpenNewPlaylistDialog = { showNewPlaylistDialog = true },
                            onOpenAddTracksDialog = { showAddTracksDialog = true },
                            onDeletePlaylist = { playlistId ->
                                libraryViewModel.deletePlaylist(playlistId) { message ->
                                    selectedPlaylistId = null
                                    if (message != null) showFeedback(message)
                                }
                            },
                            onPickPlaylistCover = { pickPlaylistCover.launch(arrayOf("image/*")) },
                            onClearPlaylistCover = { pl ->
                                libraryViewModel.upsertPlaylist(pl.copy(coverUri = null)) { _, _ -> }
                            },
                            onMovePlaylistTrack = { plId, trkId, delta ->
                                libraryViewModel.movePlaylistTrack(plId, trkId, delta) { msg ->
                                    if (msg != null) showFeedback(msg)
                                }
                            },
                            onRemovePlaylistTrack = { plId, trkId ->
                                libraryViewModel.removePlaylistTrack(plId, trkId) { msg ->
                                    if (msg != null) showFeedback(msg)
                                }
                            },
                            onPlayTrack = ::playTrackAndUpdateRecent,
                            onPlayPlaylist = { pl, track, list ->
                                playTrackAndUpdateRecent(track, list)
                                updateRecentPlaylists(pl.id)
                            },
                            onSearchQueryChange = libraryViewModel::setSearchQuery,
                            onSortOrderChange = libraryViewModel::setSortOrder,
                            onLetterFilterChange = libraryViewModel::setLetterFilter,
                            onOpenTrackLinks = { selectedTrackForLinks = it },
                            listState = libraryListState
                        )

                        AppTab.Download -> DownloaderScreen(
                            viewModel = downloaderViewModel,
                            onPickOutputFolder = { downloadFolderPicker.launch(null) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        AppTab.Equalizer -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
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
                                initialEqEnabled = eqEnabled,
                                initialPresetName = activeEqPreset,
                                initialEqBands = eqBands,
                                initialBassBoost = bassBoost,
                                initialVirtualizer = virtualizer,
                                initialSpeakerBoost = speakerBoost,
                                onEqStateChange = { enabled, preset, bands, bass, virt, spk ->
                                    coroutineScope.launch {
                                        container.preferences.saveEqState(enabled, preset, bands, bass, virt, spk)
                                    }
                                },
                                customPresets = customEqPresets,
                                savedProfileId = storedOutputProfileId
                            )
                        }

                        AppTab.Settings -> SettingsScreen(
                            themeSettings = themeSettings,
                            lyricsScrollSpeed = lyricsScrollSpeed,
                            crossfadeSeconds = crossfadeSeconds,
                            trackerFrequency = trackerFrequency,
                            trackerIntensity = trackerIntensity,
                            trackerCircleColorHex = trackerCircleColorHex,
                            downloadConcurrency = downloadConcurrency,
                            downloadFolderLabel = downloadFolderLabel,
                            autoBackupInterval = autoBackupInterval,
                            tracks = tracks,
                            visibleTracks = visibleTracks,
                            catalogTracks = catalogTracks,
                            trackerSyncProgress = trackerSyncProgress,
                            isTrackerSyncing = isTrackerSyncing,
                            themeMessage = themeMessage,
                            onSaveThemeSettings = { settings ->
                                libraryViewModel.saveThemeSettings(settings) { themeMessage = it }
                            },
                            onSaveLyricsScrollSpeed = { speed ->
                                libraryViewModel.saveLyricsScrollSpeed(speed) { themeMessage = it }
                            },
                            onSaveCrossfadeSeconds = { sec ->
                                coroutineScope.launch { container.preferences.saveCrossfadeSeconds(sec) }
                            },
                            onSaveTrackerFrequency = { key ->
                                coroutineScope.launch { container.preferences.saveTrackerFrequency(key) }
                            },
                            onSaveTrackerIntensity = { key ->
                                coroutineScope.launch { container.preferences.saveTrackerIntensity(key) }
                            },
                            onSaveTrackerCircleColor = { hex ->
                                coroutineScope.launch { container.preferences.saveTrackerCircleColor(hex) }
                            },
                            onSaveDownloadConcurrency = { c ->
                                coroutineScope.launch { container.preferences.saveDownloadConcurrency(c) }
                            },
                            onPickDownloadFolder = { downloadFolderPicker.launch(null) },
                            onPinWidget = {
                                val pinned = MusicWidgetProvider.requestPin(context)
                                if (!pinned) {
                                    showFeedback("Tu launcher no permite fijar widgets desde la app. Añádelo desde el escritorio.")
                                }
                            },
                            onExportCentralBackup = {
                                exportCentralBackup.launch("musica-para-los-oidos-backup.zip")
                            },
                            onImportCentralBackup = {
                                importCentralBackup.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "*/*"))
                            },
                            onImportThemes = {
                                importThemesLauncher.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "*/*"))
                            },
                            onImportPlaylistJson = {
                                importPlaylistJson.launch(arrayOf("application/json", "text/*"))
                            },
                            onUploadDriveBackup = {
                                coroutineScope.launch {
                                    runCatching {
                                        val cacheFile = File(context.cacheDir, "musica-para-los-oidos-backup.zip")
                                        withContext(Dispatchers.IO) {
                                            cacheFile.outputStream().use { out ->
                                                container.centralBackupManager.exportZipBundle(out)
                                            }
                                        }
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            cacheFile
                                        )
                                        val driveIntent = container.centralBackupManager.createGoogleDriveIntent(uri)
                                        context.startActivity(driveIntent)
                                    }.onFailure {
                                        showFeedback("No se pudo abrir Drive: ${it.localizedMessage ?: "error"}")
                                    }
                                }
                            },
                            onSaveAutoBackupInterval = { key ->
                                coroutineScope.launch { container.preferences.saveAutoBackupInterval(key) }
                            },
                            onStartTrackerSync = { startTrackerSync(visibleTracks) },
                            onStopTrackerSync = { trackerJob?.cancel() },
                            listState = settingsListState
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showPlayer && nowPlaying != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
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
                        onLinkLrcFromLrclib = { trackToLink ->
                            val fetchResult = container.trackTrackerManager.fetchLyricsWithUrl(trackToLink)
                            val lyrics = fetchResult?.syncedLyrics ?: fetchResult?.plainLyrics
                            if (!lyrics.isNullOrBlank()) {
                                val savedFile = container.trackTrackerManager.downloadAndSaveLrc(
                                    track = trackToLink,
                                    lrcContent = lyrics,
                                    lrclibUrl = fetchResult?.lrclibUrl
                                )
                                savedFile != null
                            } else {
                                false
                            }
                        },
                        onClose = { showPlayer = false }
                    )
                }
            }

            UpdateDialog(viewModel = updateViewModel)

            MusicaDialogs(
                showNewPlaylistDialog = showNewPlaylistDialog,
                newPlaylistName = newPlaylistName,
                onNewPlaylistNameChange = { newPlaylistName = it },
                onDismissNewPlaylist = { showNewPlaylistDialog = false },
                onCreatePlaylist = { name ->
                    val playlist = MusicPlaylist(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        trackIds = emptyList()
                    )
                    libraryViewModel.upsertPlaylist(playlist) { success, message ->
                        if (success) {
                            showNewPlaylistDialog = false
                            newPlaylistName = ""
                            updateRecentPlaylists(playlist.id)
                        } else if (message != null) {
                            showFeedback(message)
                        }
                    }
                },
                showAddTracksDialog = showAddTracksDialog,
                selectedPlaylistForDetail = selectedPlaylistForDetail,
                allTracks = tracks,
                onDismissAddTracks = { showAddTracksDialog = false },
                onAddTracksToPlaylist = { activePl, addedTracks ->
                    val updatedIds = (activePl.trackIds + addedTracks.map { it.id }).distinct()
                    val updatedPlaylist = activePl.copy(trackIds = updatedIds)
                    libraryViewModel.upsertPlaylist(updatedPlaylist) { success, message ->
                        if (success) {
                            selectedPlaylistId = updatedPlaylist.id
                            showAddTracksDialog = false
                            showFeedback("Se añadieron ${addedTracks.size} canciones a '${activePl.name}'.")
                        } else if (message != null) {
                            showFeedback(message)
                        }
                    }
                },
                pendingImportPlan = pendingImportPlan,
                onDismissImportPlan = { pendingImportPlan = null },
                onConfirmImportPlan = { plan, resolvedTrackIds ->
                    val newPlaylist = PlaylistTextCodec.createPlaylist(plan.playlistName, resolvedTrackIds)
                    libraryViewModel.upsertPlaylist(newPlaylist) { success, message ->
                        if (success) {
                            showFeedback("Lista '${plan.playlistName}' importada con ${resolvedTrackIds.size} canciones.")
                            pendingImportPlan = null
                            updateRecentPlaylists(newPlaylist.id)
                        } else if (message != null) {
                            showFeedback(message)
                        }
                    }
                },
                selectedTrackForLinks = selectedTrackForLinks,
                catalogMap = catalogMap,
                onDismissTrackLinks = { selectedTrackForLinks = null },
                onSaveTrackLinks = { track, ytUrl, lrcPath, lrcSynced, lrclibUrl ->
                    coroutineScope.launch {
                        container.saveTrackLinks(track, ytUrl, lrcPath, lrcSynced, lrclibUrl)
                    }
                },
                onAutoSearchYouTube = { track ->
                    container.trackTrackerManager.searchYouTubeCandidate(
                        track,
                        allowSuspiciousTitle = true
                    )
                },
                onAutoSearchLrcDetailed = { track ->
                    container.trackTrackerManager.fetchLyricsWithUrl(track)
                },
                onDownloadLrc = { track, lrcContent, lrclibUrl ->
                    container.trackTrackerManager.downloadAndSaveLrc(track, lrcContent, lrclibUrl) != null
                },
                onUnlinkTrack = { track ->
                    coroutineScope.launch {
                        container.saveTrackLinks(track, null, null, false, null)
                    }
                },
                missingTracksFromBackup = missingTracksFromBackup,
                onDismissMissingTracks = { missingTracksFromBackup = null },
                onConfirmDownloadMissingTracks = { missing ->
                    container.centralBackupManager.enqueueMissingTracks(missing)
                    showFeedback("Se agregaron ${missing.size} pistas a la cola de descargas.")
                    missingTracksFromBackup = null
                }
            )
        }
    }
}
