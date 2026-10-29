package com.musica.paralosoidos.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
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

    // Filtrar y ordenar pistas de biblioteca
    val filteredTracks by remember(tracks) {
        derivedStateOf {
            var result = tracks

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                result = result.filter {
                    it.title.lowercase().contains(q) ||
                    it.artist.lowercase().contains(q) ||
                    it.album.lowercase().contains(q)
                }
            }

            if (selectedLetterFilter != null) {
                val letter = selectedLetterFilter!!
                result = if (letter == '#') {
                    result.filter { it.title.firstOrNull()?.isLetter() != true }
                } else {
                    result.filter { it.title.startsWith(letter, ignoreCase = true) }
                }
            }

            when (currentSortOrder) {
                SortOrder.AZ -> result.sortedBy { it.title.lowercase() }
                SortOrder.ZA -> result.sortedByDescending { it.title.lowercase() }
                SortOrder.Recent -> result
            }
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
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(14.dp))
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
                                                        colors = listOf(Color.Transparent, Color(0xCC000000))
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
                                    allTracks = tracks,
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
                            val playlistTracks = activePlaylist.trackIds.mapNotNull { id -> tracks.firstOrNull { it.id == id } }
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
                                    val trackIndex = activePlaylist.trackIds.indexOf(track.id)
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
                                        allTracks = tracks,
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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color(0x33FFFFFF),
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
                                        containerColor = if (selectedLetterFilter == null) MaterialTheme.colorScheme.primary else Color(0x1AFFFFFF),
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
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0x14FFFFFF),
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

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeSettingsSection(
    savedSettings: com.musica.paralosoidos.data.ThemeSettingsState,
    onSave: suspend (com.musica.paralosoidos.data.ThemeSettingsState) -> Unit,
    onMessage: (String?) -> Unit
) {
    var draft by remember(savedSettings) { mutableStateOf(savedSettings) }
    LaunchedEffect(draft) {
        if (draft != savedSettings) {
            delay(300)
            runCatching { onSave(draft) }
                .onSuccess { onMessage(null) }
                .onFailure {
                    onMessage("No se pudo guardar el estilo: ${it.localizedMessage ?: "error desconocido"}")
                }
        }
    }
    ThemeSettingsScreen(settings = draft, onSettingsChange = { draft = it })
}

@Composable
private fun EmptyLibrary(
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
private fun TrackRow(
    track: TrackEntity,
    onClick: () -> Unit
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
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Card(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
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
    }
}

@Composable
private fun PlaylistGalleryCard(
    playlist: com.musica.paralosoidos.data.MusicPlaylist,
    allTracks: List<TrackEntity>,
    onClick: () -> Unit,
    onPlay: (List<TrackEntity>) -> Unit
) {
    val selectedTracks = remember(playlist.trackIds, allTracks) {
        playlist.trackIds.mapNotNull { id -> allTracks.firstOrNull { it.id == id } }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color(0x14FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
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
private fun PlaylistDetailHeader(
    playlist: com.musica.paralosoidos.data.MusicPlaylist,
    allTracks: List<TrackEntity>,
    onBack: () -> Unit,
    onPlayAll: (List<TrackEntity>) -> Unit,
    onShuffle: (List<TrackEntity>) -> Unit,
    onAddSongsClick: () -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val selectedTracks = remember(playlist.trackIds, allTracks) {
        playlist.trackIds.mapNotNull { id -> allTracks.firstOrNull { it.id == id } }
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
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
                    Text(playlist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${selectedTracks.size} canciones en total",
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
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reproducir")
            }
            OutlinedButton(
                onClick = { onShuffle(selectedTracks) },
                enabled = selectedTracks.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Aleatorio")
            }
            Button(
                onClick = onAddSongsClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir")
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
private fun PlaylistTrackRow(
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onPlay)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index",
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.width(28.dp)
        )
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
        IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "Subir pista", modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar pista", modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Quitar de lista", modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun AddTracksToPlaylistDialog(
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
                                .clip(RoundedCornerShape(8.dp))
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
private fun PlaylistImportConflictDialog(
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
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(conflict.songTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                conflict.candidates.forEach { candidate ->
                                    val isSelected = chosenSelections[conflict.songTitle] == candidate.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
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

@Composable
private fun MiniPlayer(
    track: TrackEntity,
    playing: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    progress: () -> Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Column {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0x1AFFFFFF)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (track.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Card(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).clickable(onClick = onOpen)) {
                    Text(track.title, fontWeight = FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                    Text(track.artist, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reproducir o pausar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
                }
            }
        }
    }
}

@Composable
private fun FullPlayer(
    track: TrackEntity,
    queue: List<TrackEntity>,
    allTracks: List<TrackEntity>,
    controller: MediaController?,
    playing: Boolean,
    position: () -> Float,
    duration: () -> Float,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    playbackSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    error: String?,
    onSelectQueueItem: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onRemoveQueueItem: (Int) -> Unit,
    onAddQueueItem: (TrackEntity) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Cerrar", modifier = Modifier.size(32.dp))
        }

        val pagerState = rememberPagerState(pageCount = { 2 })
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(360.dp)
        ) { page ->
            if (page == 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        if (track.artworkUri != null) {
                            AsyncImage(
                                model = track.artworkUri,
                                contentDescription = "Portada de ${track.album}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text(track.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(track.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LyricsPane(
                    track = track,
                    positionMs = { position().toLong() }
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }) {
                Text("Portada", color = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } }) {
                Text("Letras", color = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        PlayerSeekBar(position = position, duration = duration, controller = controller)

        PlaybackTimeLabels(position = position, duration = duration)

        // Controles de reproduccion principales
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { controller?.shuffleModeEnabled = !shuffleEnabled }) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Aleatorio",
                    tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { controller?.seekToPreviousMediaItem() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(36.dp))
            }
            IconButton(
                onClick = {
                    if (controller?.isPlaying == true) controller.pause() else controller?.play()
                },
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.tertiary, CircleShape)
            ) {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pausar" else "Reproducir",
                    modifier = Modifier.size(36.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { controller?.seekToNextMediaItem() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = {
                controller?.repeatMode = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
            }) {
                Icon(
                    if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeticion",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Selector de velocidad (dropdown)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Velocidad de reproduccion:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            var speedExpanded by remember { mutableStateOf(false) }
            Box {
                Surface(
                    onClick = { speedExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x1AFFFFFF),
                    border = BorderStroke(1.dp, Color(0x26FFFFFF))
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${playbackSpeed}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
                DropdownMenu(
                    expanded = speedExpanded,
                    onDismissRequest = { speedExpanded = false },
                    modifier = Modifier.background(Color(0xFF141024))
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${speed}x" + if (speed == 1.0f) " (Normal)" else "",
                                    fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                    color = if (playbackSpeed == speed) MaterialTheme.colorScheme.primary else Color.White,
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {
                                onSpeedChange(speed)
                                speedExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("En cola (${queue.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Text(
            "Mantén pulsada una pista y arrástrala para cambiar el orden.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        var showAddTracks by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { showAddTracks = !showAddTracks }) {
            Text(if (showAddTracks) "Ocultar pistas" else "Añadir pistas a la cola")
        }
        if (showAddTracks) {
            val queueIds = queue.mapTo(mutableSetOf()) { it.id }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allTracks.filterNot { it.id in queueIds }, key = { "add-${it.id}" }) { candidate ->
                    Surface(
                        onClick = { onAddQueueItem(candidate) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            candidate.title,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 220.dp)) {
            items(queue, key = { "queue-${it.id}" }) { queued ->
                val latestQueue by rememberUpdatedState(queue)
                val latestMove by rememberUpdatedState(onMoveQueueItem)
                val density = LocalDensity.current
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pointerInput(queued.id) {
                            var accumulatedDrag = 0f
                            val moveThreshold = with(density) { 44.dp.toPx() }
                            detectDragGesturesAfterLongPress(
                                onDragStart = { accumulatedDrag = 0f },
                                onDragEnd = { accumulatedDrag = 0f },
                                onDragCancel = { accumulatedDrag = 0f }
                            ) { change, dragAmount ->
                                change.consume()
                                accumulatedDrag += dragAmount.y
                                while (accumulatedDrag >= moveThreshold || accumulatedDrag <= -moveThreshold) {
                                    val currentIndex = latestQueue.indexOfFirst { it.id == queued.id }
                                    val direction = if (accumulatedDrag > 0) 1 else -1
                                    val targetIndex = currentIndex + direction
                                    if (currentIndex !in latestQueue.indices || targetIndex !in latestQueue.indices) {
                                        accumulatedDrag = 0f
                                        break
                                    }
                                    latestMove(currentIndex, targetIndex)
                                    accumulatedDrag -= direction * moveThreshold
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val index = queue.indexOfFirst { it.id == queued.id }
                    Column(
                        Modifier.weight(1f).clickable { onSelectQueueItem(index) }.padding(vertical = 6.dp)
                    ) {
                        Text(queued.title, maxLines = 1, fontWeight = FontWeight.SemiBold)
                        Text(queued.artist, maxLines = 1, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onRemoveQueueItem(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Quitar de la cola", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

}

@Composable
private fun PlayerSeekBar(
    position: () -> Float,
    duration: () -> Float,
    controller: MediaController?
) {
    var sliderPosition by remember { mutableFloatStateOf(position()) }
    var isSeeking by remember { mutableStateOf(false) }
    LaunchedEffect(isSeeking) {
        if (!isSeeking) {
            snapshotFlow { position() }.collect { sliderPosition = it }
        }
    }
    val durationValue = duration().coerceAtLeast(1f)
    Slider(
        value = sliderPosition.coerceIn(0f, durationValue),
        onValueChange = {
            isSeeking = true
            sliderPosition = it
        },
        onValueChangeFinished = {
            controller?.seekTo(sliderPosition.toLong())
            isSeeking = false
        },
        valueRange = 0f..durationValue,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun PlaybackTimeLabels(
    position: () -> Float,
    duration: () -> Float
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatTime(position().toLong() / 1000), style = MaterialTheme.typography.labelSmall)
        Text(formatTime(duration().toLong() / 1000), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun LyricsPane(
        track: TrackEntity,
        positionMs: () -> Long
    ) {
        val context = LocalContext.current
        var lines by remember(track.id) { mutableStateOf(emptyList<com.musica.paralosoidos.playback.LyricLine>()) }
        var message by remember(track.id) { mutableStateOf("Buscando letras...") }
        val activeLine = remember(lines, positionMs()) {
            com.musica.paralosoidos.playback.LrcLyrics.currentLineIndex(lines, positionMs())
        }

        LaunchedEffect(track.id) {
            lines = emptyList()
            message = "Buscando ${track.displayName.substringBeforeLast('.', track.displayName)}.lrc..."
            runCatching {
                withContext(Dispatchers.IO) {
                    LyricsFileRepository.findMatchingLyrics(
                        context = context,
                        audioFileName = track.displayName,
                        trackTitle = track.title
                    )
                }
            }.onSuccess { found ->
                if (found == null) {
                    message = "No se encontró el .lrc. Android puede limitar el acceso a archivos de letras no indexados."
                } else {
                    lines = found.second
                    message = "Letras: ${found.first}"
                }
            }.onFailure { error ->
                message = "No se pudieron leer las letras: ${error.localizedMessage ?: "error desconocido"}"
            }
        }

        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Letras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (lines.isNotEmpty()) {
                val lyricListState = androidx.compose.foundation.lazy.rememberLazyListState()
                LaunchedEffect(activeLine) {
                    if (activeLine >= 0) lyricListState.animateScrollToItem(activeLine)
                }
                LazyColumn(
                    state = lyricListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lines.size) { index ->
                        Text(
                            lines[index].text,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (index == activeLine) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == activeLine) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
}

@Composable
private fun EqualizerScreenContent(
    controller: MediaController?,
    onOutputProfileSelected: (String) -> Unit,
    outputProfileMessage: String?,
    onSaveCustomPreset: (com.musica.paralosoidos.data.SavedEqPreset) -> Unit
) {
    val bands by AudioEffectsManager.bands.collectAsState()
    val bassStrength by AudioEffectsManager.bassStrength.collectAsState()
    val currentPreset by AudioEffectsManager.selectedPreset.collectAsState()
    val selectedOutputProfile by AudioEffectsManager.selectedOutputProfile.collectAsState()
    val audioStatus by AudioEffectsManager.status.collectAsState()
    val isEnabled by AudioEffectsManager.isEnabled.collectAsState()
    val speakerBoost by AudioEffectsManager.speakerBoost.collectAsState()
    var normalVolume by remember { mutableFloatStateOf(100f) }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Ecualizador", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Procesamiento de sonido en vivo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Pill toggle button matching web prototype style
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isEnabled) MaterialTheme.colorScheme.primary
                        else Color(0x1AFFFFFF)
                    )
                    .clickable { AudioEffectsManager.setEnabled(!isEnabled) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isEnabled) "ENCENDIDO" else "APAGADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled) Color.Black else Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 1. Selector de volumen normal (0..100%)
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0x0DFFFFFF), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Volumen Multimedia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "${normalVolume.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(Color(0x26FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Slider(
                value = normalVolume,
                onValueChange = {
                    normalVolume = it
                    controller?.volume = (it / 100f).coerceIn(0f, 1f)
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color(0x1AFFFFFF)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("50%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100% (Límite normal)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Uso de Altavoz / Loudness Enhancer (0..100%)
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0x0DFFFFFF), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Uso de Altavoz (Amplificación +20 dB)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Amplificación de hardware; puede saturar en volúmenes altos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    if (speakerBoost > 0) "$speakerBoost% (+${speakerBoost / 5} dB)" else "0%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (speakerBoost > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(Color(0x26FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Slider(
                value = speakerBoost.toFloat(),
                onValueChange = { AudioEffectsManager.setSpeakerBoost(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFF59E0B),
                    activeTrackColor = Color(0xFFF59E0B),
                    inactiveTrackColor = Color(0x1AFFFFFF)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0% (Apagado)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("50% (+10 dB)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100% (+20 dB)", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            }
        }

        // Selector de Presets (Dropdown tipo Web)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0x14FFFFFF)),
            colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF))
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Preset:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                var presetMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        onClick = { presetMenuExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF141024),
                        border = BorderStroke(1.dp, Color(0x38FFFFFF))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                currentPreset,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = presetMenuExpanded,
                        onDismissRequest = { presetMenuExpanded = false },
                        modifier = Modifier.background(Color(0xFF18142A))
                    ) {
                        (AudioEffectsManager.presets.keys.toList() + "Personalizado").distinct().forEach { preset ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        preset,
                                        fontSize = 12.sp,
                                        fontWeight = if (currentPreset == preset) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentPreset == preset) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                },
                                onClick = {
                                    if (preset != "Personalizado") AudioEffectsManager.applyPreset(preset)
                                    presetMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Fila para guardar preset de ecualizador
        var newPresetName by remember { mutableStateOf("") }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newPresetName,
                onValueChange = { newPresetName = it },
                placeholder = { Text("Nombre para guardar preset...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0x1AFFFFFF),
                    focusedContainerColor = Color(0x0DFFFFFF),
                    unfocusedContainerColor = Color(0x0DFFFFFF)
                )
            )
            Button(
                onClick = {
                    if (newPresetName.isNotBlank()) {
                        val name = newPresetName.trim()
                        AudioEffectsManager.saveCustomPreset(name)?.let(onSaveCustomPreset)
                        newPresetName = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Faders Card (10 bandas con controles nativos)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0x14FFFFFF)),
            colors = CardDefaults.cardColors(containerColor = Color(0x0DFFFFFF))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    "Bandas de Frecuencia (-16dB a +16dB):",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AudioEffectsManager.bandFrequencies.forEachIndexed { index, freq ->
                        val gain = bands.getOrElse(index) { 0 }
                        val freqLabel = if (freq >= 1000) "${freq / 1000}k" else "$freq"

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        ) {
                            Text(
                                "${if (gain > 0) "+$gain" else "$gain"}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gain != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            VerticalEqualizerSlider(
                                value = gain,
                                onValueChange = { AudioEffectsManager.setBandGain(index, it) },
                                modifier = Modifier.weight(1f).fillMaxSize()
                            )
                            Text(freqLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

            // Bass Boost + Sonido 3D en dos columnas (igual que web)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bass Boost
                Column(
                    Modifier
                        .weight(1f)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("BASS BOOST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "+$bassStrength dB",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Slider(
                        value = bassStrength.toFloat(),
                        onValueChange = { AudioEffectsManager.setBassBoost(it.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color(0x1AFFFFFF)
                        )
                    )
                }

                // Sonido 3D / Virtual
                val virtualizerStrength by AudioEffectsManager.virtualizerStrength.collectAsState()
                val virtualizerAvailable by AudioEffectsManager.virtualizerAvailable.collectAsState()
                Column(
                    Modifier
                        .weight(1f)
                        .background(Color(0x0DFFFFFF), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SONIDO 3D / VIRTUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (virtualizerAvailable) "${virtualizerStrength}%" else "No disponible",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Slider(
                        value = virtualizerStrength.toFloat(),
                        onValueChange = { AudioEffectsManager.setVirtualizer(it.toInt()) },
                        valueRange = 0f..100f,
                        enabled = virtualizerAvailable,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color(0x1AFFFFFF)
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Salida de Audio y Dispositivo (Alta Fidelidad)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Estos perfiles ajustan el tono. Android y el dispositivo conectado determinan la ruta, el codec y la frecuencia de muestreo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AudioEffectsManager.outputProfiles.forEach { profile ->
                    val selected = selectedOutputProfile == profile.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOutputProfileSelected(profile.id) },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (selected) 1.5.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else Color(0x14FFFFFF)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) Color(0xFF071E23) else Color(0x0DFFFFFF)
                        )
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    profile.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    profile.detail,
                                    modifier = Modifier.padding(start = 6.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                profile.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                outputProfileMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Mensajes de estado del sistema de audio (solo si tienen contenido relevante)
            val eqMsg = audioStatus.equalizerMessage
            if (eqMsg.isNotBlank() && !eqMsg.contains("disponible al iniciar")) {
                Text(
                    eqMsg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

@Composable
private fun VerticalEqualizerSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val activeColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val currentValue by rememberUpdatedState(onValueChange)
    val updateFromPosition: (Float, Float) -> Unit = { y, height ->
        val thumbRadius = with(density) { 8.dp.toPx() }
        val usableHeight = (height - thumbRadius * 2f).coerceAtLeast(1f)
        val normalizedY = ((y - thumbRadius) / usableHeight).coerceIn(0f, 1f)
        currentValue(EqualizerBandMapper.gainForTouchPosition(normalizedY, 1f))
    }
    val progress = ((value.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB) - MIN_GAIN_DB) /
        (MAX_GAIN_DB - MIN_GAIN_DB).toFloat())

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { position -> updateFromPosition(position.y, size.height.toFloat()) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    updateFromPosition(change.position.y, size.height.toFloat())
                }
            }
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                setProgress { requested ->
                    currentValue(
                        (requested.coerceIn(0f, 1f) * (MAX_GAIN_DB - MIN_GAIN_DB) + MIN_GAIN_DB).toInt()
                    )
                    true
                }
            }
    ) {
        val inset = 8.dp.toPx()
        val x = size.width / 2f
        val centerY = size.height / 2f
        val top = inset
        val bottom = size.height - inset
        val thumbY = top + (bottom - top) * (1f - progress)
        drawLine(trackColor, Offset(x, top), Offset(x, bottom), strokeWidth = 3.dp.toPx())
        drawLine(
            activeColor,
            Offset(x, centerY),
            Offset(x, thumbY),
            strokeWidth = 4.dp.toPx()
        )
        drawCircle(activeColor, radius = 7.dp.toPx(), center = Offset(x, thumbY))
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

private fun formatTime(seconds: Long): String =
    "%d:%02d".format(seconds / 60, seconds % 60)
