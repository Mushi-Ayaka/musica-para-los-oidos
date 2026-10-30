package com.musica.paralosoidos.ui

import java.util.Locale

import com.musica.paralosoidos.ui.theme.MusicaStyle
import com.musica.paralosoidos.ui.components.AddTracksToPlaylistDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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

private enum class SortOrder(val label: String) {
    AZ("A-Z"),
    ZA("Z-A"),
    Recent("Recientes")
}

private data class SearchableTrack(
    val track: TrackEntity,
    val title: String,
    val artist: String,
    val album: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicaRoot(
    container: AppContainer,
    controller: MediaController?,
    hasAudioPermission: () -> Boolean,
    requestAudioPermission: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    val tracksFlow = remember(container) { container.tracks.observeAll() }
    val tracks by tracksFlow.collectAsState(initial = emptyList())
    val themeSettings by container.preferences.themeSettings.collectAsState(
        initial = com.musica.paralosoidos.data.ThemeSettingsState()
    )
    val customEqPresets by container.preferences.customEqPresets.collectAsState(initial = emptyList())
    val playlists by container.preferences.playlists.collectAsState(initial = emptyList())

    var nowPlaying by remember { mutableStateOf<TrackEntity?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(1f) }
    var queueRevision by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var selectedPlaylistForDetail by remember { mutableStateOf<com.musica.paralosoidos.data.MusicPlaylist?>(null) }
    var showAddTracksDialog by remember { mutableStateOf(false) }
    var pendingImportPlan by remember { mutableStateOf<PlaylistImportPlan?>(null) }
    var audioOutputProfileMessage by remember { mutableStateOf<String?>(null) }

    var scanMessage by remember { mutableStateOf<String?>(null) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }

    var themeMessage by remember { mutableStateOf<String?>(null) }

    // Dialogos y modales

    // Filtros de biblioteca
    var searchQuery by remember { mutableStateOf("") }
    var librarySubTab by remember { mutableStateOf("Pistas") }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var playlistMessage by remember { mutableStateOf<String?>(null) }
    var currentSortOrder by remember { mutableStateOf(SortOrder.AZ) }
    var selectedLetterFilter by remember { mutableStateOf<Char?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
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
                        container.preferences.savePlaylists(playlists + imported)
                        playlistMessage = "Lista '${plan.playlistName}' importada correctamente (${plan.autoResolvedTrackIds.size} pistas)."
                    } else {
                        pendingImportPlan = plan
                    }
                }.onFailure {
                    playlistMessage = "No se pudo importar la lista: ${it.localizedMessage ?: "error desconocido"}"
                }
            }
        }
    }
    val storedOutputProfileId by container.audioOutputProfileSettings.profileId.collectAsState(
        initial = AudioOutputProfileIds.STUDIO_MONITOR
    )
    val tracksById = remember(tracks) { tracks.associateBy { it.id.toString() } }
    val tracksByIdLong = remember(tracks) { tracks.associateBy { it.id } }
    val latestTracksById by rememberUpdatedState(tracksById)
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
        if (audioGranted) {
            runCatching { container.refreshLocalAudio() }
                .onFailure { scanMessage = it.localizedMessage ?: "No se pudo leer el audio." }
        }
    }

    DisposableEffect(controller) {
        if (controller == null) return@DisposableEffect onDispose { }
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_TIMELINE_CHANGED) ||
                    events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                ) {
                    queueRevision++
                    nowPlaying = latestTracksById[player.currentMediaItem?.mediaId]
                }
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    isPlaying = player.isPlaying
                }
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                    duration = player.duration.takeIf { it > 0L }?.toFloat() ?: 1f
                }
                if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                    position = player.currentPosition.coerceAtLeast(0L).toFloat()
                }
                if (events.contains(Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED)) {
                    shuffleEnabled = player.shuffleModeEnabled
                }
                if (events.contains(Player.EVENT_REPEAT_MODE_CHANGED)) {
                    repeatMode = player.repeatMode
                }
                if (events.contains(Player.EVENT_PLAYBACK_PARAMETERS_CHANGED)) {
                    playbackSpeed = player.playbackParameters.speed
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playbackError = error.localizedMessage ?: "No se pudo reproducir este archivo."
                isPlaying = false
            }
        }
        controller.addListener(listener)
        nowPlaying = latestTracksById[controller.currentMediaItem?.mediaId]
        isPlaying = controller.isPlaying
        position = controller.currentPosition.coerceAtLeast(0L).toFloat()
        duration = controller.duration.takeIf { it > 0L }?.toFloat() ?: 1f
        shuffleEnabled = controller.shuffleModeEnabled
        repeatMode = controller.repeatMode
        playbackSpeed = controller.playbackParameters.speed
        onDispose { controller.removeListener(listener) }
    }

    LaunchedEffect(controller, isPlaying) {
        while (true) {
            if (isPlaying) {
                position = controller?.currentPosition?.coerceAtLeast(0L)?.toFloat() ?: 0f
                duration = controller?.duration?.takeIf { it > 0L }?.toFloat() ?: 1f
            }
            delay(500)
        }
    }

    fun playTrack(track: TrackEntity, customQueue: List<TrackEntity> = tracks) {
        if (controller == null) {
            scanMessage = "El reproductor aun se esta iniciando. Intentalo de nuevo."
            return
        }
        playbackError = null
        val queue = customQueue.map(::mediaItemFor)
        val index = customQueue.indexOf(track).coerceAtLeast(0)
        controller.setMediaItems(queue, index, 0L)
        controller.prepare()
        controller.play()
    }

    val playbackQueue = remember(tracksById, controller, queueRevision) {
        controller?.let { player ->
            (0 until player.mediaItemCount).mapNotNull { index ->
                tracksById[player.getMediaItemAt(index).mediaId]
            }
        }.orEmpty()
    }

    // Filtrar y ordenar pistas de biblioteca con texto ya normalizado.
    val searchableTracks = remember(tracks) {
        tracks.map {
            SearchableTrack(
                track = it,
                title = it.title.lowercase(Locale.ROOT),
                artist = it.artist.lowercase(Locale.ROOT),
                album = it.album.lowercase(Locale.ROOT)
            )
        }
    }
    val filteredTracks by remember(tracks) {
        derivedStateOf {
            var result = searchableTracks

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase(Locale.ROOT)
                result = result.filter {
                    it.title.contains(q) || it.artist.contains(q) || it.album.contains(q)
                }
            }

            if (selectedLetterFilter != null) {
                val letter = selectedLetterFilter!!
                result = if (letter == '#') {
                    result.filter { it.track.title.firstOrNull()?.isLetter() != true }
                } else {
                    result.filter { it.track.title.startsWith(letter, ignoreCase = true) }
                }
            }

            when (currentSortOrder) {
                SortOrder.AZ -> result.sortedBy { it.title }
                SortOrder.ZA -> result.sortedByDescending { it.title }
                SortOrder.Recent -> result
            }.map { it.track }
        }
    }

    val albums = remember(tracks) {
        tracks.filter { it.album.isNotBlank() }.groupBy { it.album }
            .toList()
            .sortedBy { it.first.lowercase() }
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
                        onPlayPause = {
                            if (controller?.isPlaying == true) controller.pause() else controller?.play()
                        },
                        onNext = { controller?.seekToNextMediaItem() },
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
                                selectedTab = tab
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
                                    .clickable { playTrack(featuredTrack) },
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
                                    else {
                                        scanMessage = "Buscando audio..."
                                        coroutineScope.launch {
                                            runCatching { container.refreshLocalAudio(force = true) }
                                                .onSuccess { scanMessage = null }
                                                .onFailure { scanMessage = it.localizedMessage ?: "No se pudo leer el audio." }
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        items(tracks.take(6), key = { "home-${it.id}" }) { track ->
                            TrackRow(
                                track = track,
                                onClick = { playTrack(track) }
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
                                    else {
                                        scanMessage = "Buscando audio..."
                                        coroutineScope.launch {
                                            runCatching { container.refreshLocalAudio(force = true) }
                                                .onSuccess { scanMessage = null }
                                                .onFailure { scanMessage = it.localizedMessage ?: "No se pudo leer el audio." }
                                        }
                                    }
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
                                    onClick = { librarySubTab = section },
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
                                        playTrack(albumTracks.first(), albumTracks)
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
                        val activePlaylist = playlists.firstOrNull { it.id == selectedPlaylistForDetail?.id } ?: selectedPlaylistForDetail
                        if (activePlaylist != null) {
                            item {
                                PlaylistDetailHeader(
                                    playlist = activePlaylist,
                                    allTracksById = tracksByIdLong,
                                    onBack = { selectedPlaylistForDetail = null },
                                    onPlayAll = { playlistTracks ->
                                        if (playlistTracks.isNotEmpty()) playTrack(playlistTracks.first(), playlistTracks)
                                    },
                                    onShuffle = { playlistTracks ->
                                        if (playlistTracks.isNotEmpty()) {
                                            val shuffled = playlistTracks.shuffled()
                                            playTrack(shuffled.first(), shuffled)
                                        }
                                    },
                                    onAddSongsClick = { showAddTracksDialog = true },
                                    onDeletePlaylist = {
                                        coroutineScope.launch {
                                            container.preferences.savePlaylists(playlists.filterNot { it.id == activePlaylist.id })
                                            selectedPlaylistForDetail = null
                                            playlistMessage = "Lista eliminada."
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
                                        onPlay = { playTrack(track, playlistTracks) },
                                        onMoveUp = {
                                            coroutineScope.launch {
                                                val updatedIds = activePlaylist.trackIds.toMutableList()
                                                val curr = updatedIds.indexOf(track.id)
                                                if (curr > 0) {
                                                    val moved = updatedIds.removeAt(curr)
                                                    updatedIds.add(curr - 1, moved)
                                                    container.preferences.savePlaylists(
                                                        playlists.map { if (it.id == activePlaylist.id) it.copy(trackIds = updatedIds) else it }
                                                    )
                                                }
                                            }
                                        },
                                        onMoveDown = {
                                            coroutineScope.launch {
                                                val updatedIds = activePlaylist.trackIds.toMutableList()
                                                val curr = updatedIds.indexOf(track.id)
                                                if (curr >= 0 && curr < updatedIds.lastIndex) {
                                                    val moved = updatedIds.removeAt(curr)
                                                    updatedIds.add(curr + 1, moved)
                                                    container.preferences.savePlaylists(
                                                        playlists.map { if (it.id == activePlaylist.id) it.copy(trackIds = updatedIds) else it }
                                                    )
                                                }
                                            }
                                        },
                                        onRemove = {
                                            coroutineScope.launch {
                                                val updatedIds = activePlaylist.trackIds.filterNot { it == track.id }
                                                container.preferences.savePlaylists(
                                                    playlists.map { if (it.id == activePlaylist.id) it.copy(trackIds = updatedIds) else it }
                                                )
                                            }
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
                                        onClick = { selectedPlaylistForDetail = playlist },
                                        onPlay = { plTracks ->
                                            if (plTracks.isNotEmpty()) playTrack(plTracks.first(), plTracks)
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
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar pistas, artistas o albumes...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
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
                                    onClick = { currentSortOrder = order },
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
                                    onClick = { selectedLetterFilter = null },
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
                                        selectedLetterFilter = if (isSelected) null else char
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
                                onClick = { playTrack(track, filteredTracks) }
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
                                coroutineScope.launch {
                                    runCatching {
                                        container.preferences.saveCustomEqPresets(
                                            (customEqPresets.filterNot { it.name == preset.name } + preset)
                                        )
                                    }.onFailure {
                                        themeMessage = "No se pudo guardar el preset EQ: ${it.localizedMessage ?: "error desconocido"}"
                                    }
                                }
                            }
                        )
                    }
                }

                AppTab.Settings -> {
                    item {
                        ThemeSettingsSection(
                            savedSettings = themeSettings,
                            onSave = { container.preferences.saveThemeSettings(it) },
                            onMessage = { themeMessage = it }
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
                onSpeedChange = { speed ->
                    playbackSpeed = speed
                    controller?.setPlaybackSpeed(speed)
                },
                error = playbackError,
                onSelectQueueItem = { index ->
                    controller?.seekTo(index, 0L)
                    controller?.play()
                },
                onMoveQueueItem = { from, to -> controller?.moveMediaItem(from, to) },
                onRemoveQueueItem = { index -> controller?.removeMediaItem(index) },
                onAddQueueItem = { track -> controller?.addMediaItem(mediaItemFor(track)) },
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
                        coroutineScope.launch {
                            runCatching { container.preferences.savePlaylists(playlists + playlist) }
                                .onSuccess {
                                    playlistMessage = null
                                    showNewPlaylistDialog = false
                                    newPlaylistName = ""
                                }
                                .onFailure {
                                    playlistMessage = "No se pudo crear la lista: ${it.localizedMessage ?: "error desconocido"}"
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
                coroutineScope.launch {
                    val updatedIds = (activePl.trackIds + addedTracks.map { it.id }).distinct()
                    container.preferences.savePlaylists(
                        playlists.map { if (it.id == activePl.id) it.copy(trackIds = updatedIds) else it }
                    )
                    selectedPlaylistForDetail = activePl.copy(trackIds = updatedIds)
                    showAddTracksDialog = false
                    playlistMessage = "Se añadieron ${addedTracks.size} canciones a '${activePl.name}'."
                }
            }
        )
    }

    pendingImportPlan?.let { plan ->
        PlaylistImportConflictDialog(
            plan = plan,
            onDismiss = { pendingImportPlan = null },
            onConfirm = { resolvedTrackIds ->
                coroutineScope.launch {
                    val newPlaylist = PlaylistTextCodec.createPlaylist(plan.playlistName, resolvedTrackIds)
                    container.preferences.savePlaylists(playlists + newPlaylist)
                    playlistMessage = "Lista '${plan.playlistName}' importada con ${resolvedTrackIds.size} canciones."
                    pendingImportPlan = null
                }
            }
        )
    }
    }
}

private fun mediaItemFor(track: TrackEntity): MediaItem =
    MediaItem.Builder()
        .setMediaId(track.id.toString())
        .setUri(track.uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .build()
        )
        .build()
