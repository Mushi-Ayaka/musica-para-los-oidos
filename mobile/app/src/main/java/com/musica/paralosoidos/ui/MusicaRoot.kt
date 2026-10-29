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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.playback.AudioEffectsManager
import com.musica.paralosoidos.playback.EqualizerBandMapper
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
    val tracks by container.tracks.observeAll().collectAsState(initial = emptyList())
    val themeSettings by container.preferences.themeSettings.collectAsState(
        initial = com.musica.paralosoidos.data.ThemeSettingsState()
    )
    var themeSettingsDraft by remember(themeSettings) { mutableStateOf(themeSettings) }
    val customEqPresets by container.preferences.customEqPresets.collectAsState(initial = emptyList())
    val playlists by container.preferences.playlists.collectAsState(initial = emptyList())

    var nowPlaying by remember { mutableStateOf<TrackEntity?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(1f) }
    var queueRevision by remember { mutableIntStateOf(0) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var volumeBooster by remember { mutableFloatStateOf(100f) }
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
                    val imported = PlaylistTextCodec.importPlaylistJson(content, tracks)
                    container.preferences.savePlaylists(playlists + imported)
                }.onSuccess {
                    playlistMessage = "Lista importada correctamente."
                }.onFailure {
                    playlistMessage = "No se pudo importar la lista: ${it.localizedMessage ?: "error desconocido"}"
                }
            }
        }
    }
    val storedOutputProfileId by container.audioOutputProfileSettings.profileId.collectAsState(
        initial = AudioOutputProfileIds.STUDIO_MONITOR
    )
    val latestTracks by rememberUpdatedState(tracks)
    val audioGranted = hasAudioPermission()

    LaunchedEffect(customEqPresets) {
        customEqPresets.forEach(AudioEffectsManager::registerCustomPreset)
    }

    LaunchedEffect(themeSettingsDraft) {
        if (themeSettingsDraft != themeSettings) {
            delay(300)
            runCatching { container.preferences.saveThemeSettings(themeSettingsDraft) }
                .onSuccess { themeMessage = null }
                .onFailure {
                    themeMessage = "No se pudo guardar el estilo: ${it.localizedMessage ?: "error desconocido"}"
                }
        }
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
                queueRevision++
                nowPlaying = latestTracks.firstOrNull {
                    it.id.toString() == player.currentMediaItem?.mediaId
                }
                isPlaying = player.isPlaying
                position = player.currentPosition.coerceAtLeast(0L).toFloat()
                duration = player.duration.takeIf { it > 0L }?.toFloat() ?: 1f
                shuffleEnabled = player.shuffleModeEnabled
                repeatMode = player.repeatMode
                playbackSpeed = player.playbackParameters.speed
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playbackError = error.localizedMessage ?: "No se pudo reproducir este archivo."
                isPlaying = false
            }
        }
        controller.addListener(listener)
        nowPlaying = latestTracks.firstOrNull {
            it.id.toString() == controller.currentMediaItem?.mediaId
        }
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

    val playbackQueue = remember(tracks, controller, queueRevision) {
        controller?.let { player ->
            (0 until player.mediaItemCount).mapNotNull { index ->
                tracks.firstOrNull {
                    it.id.toString() == player.getMediaItemAt(index).mediaId
                }
            }
        }.orEmpty()
    }

    // Filtrar y ordenar pistas de biblioteca
    val filteredTracks = remember(tracks, searchQuery, currentSortOrder, selectedLetterFilter) {
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
            SortOrder.Recent -> result // Por orden de insercion/id
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
                        progress = (position / duration).coerceIn(0f, 1f)
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
                                            runCatching { container.refreshLocalAudio() }
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
                                            runCatching { container.refreshLocalAudio() }
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
                        item {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tus listas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Button(onClick = { showNewPlaylistDialog = true }) {
                                    Text("Nueva lista")
                                }
                            }
                        }
                        playlistMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
                        if (playlists.isEmpty()) {
                            item { Text("Crea una lista para organizar tus pistas.") }
                        } else {
                            items(playlists, key = { "playlist-${it.id}" }) { playlist ->
                                MusicPlaylistCard(
                                    playlist = playlist,
                                    allTracks = tracks,
                                    onPlay = { selected ->
                                        if (selected.isNotEmpty()) playTrack(selected.first(), selected)
                                    },
                                    onAddTrack = { track ->
                                        coroutineScope.launch {
                                            runCatching {
                                                container.preferences.savePlaylists(
                                                    playlists.map {
                                                        if (it.id == playlist.id) it.copy(trackIds = it.trackIds + track.id)
                                                        else it
                                                    }
                                                )
                                            }.onFailure {
                                                playlistMessage = "No se pudo actualizar la lista: ${it.localizedMessage ?: "error desconocido"}"
                                            }
                                        }
                                    },
                                    onRemoveTrack = { track ->
                                        coroutineScope.launch {
                                            runCatching {
                                                container.preferences.savePlaylists(
                                                    playlists.map {
                                                        if (it.id == playlist.id) it.copy(trackIds = it.trackIds - track.id)
                                                        else it
                                                    }
                                                )
                                            }.onFailure {
                                                playlistMessage = "No se pudo actualizar la lista: ${it.localizedMessage ?: "error desconocido"}"
                                            }
                                        }
                                    },
                                    onMoveTrack = { track, direction ->
                                        coroutineScope.launch {
                                            runCatching {
                                                val updatedIds = playlist.trackIds.toMutableList()
                                                val currentIndex = updatedIds.indexOf(track.id)
                                                val targetIndex = currentIndex + direction
                                                if (currentIndex >= 0 && targetIndex in updatedIds.indices) {
                                                    val movedId = updatedIds.removeAt(currentIndex)
                                                    updatedIds.add(targetIndex, movedId)
                                                    container.preferences.savePlaylists(
                                                        playlists.map {
                                                            if (it.id == playlist.id) it.copy(trackIds = updatedIds)
                                                            else it
                                                        }
                                                    )
                                                }
                                            }.onFailure {
                                                playlistMessage = "No se pudo ordenar la lista: ${it.localizedMessage ?: "error desconocido"}"
                                            }
                                        }
                                    }
                                )
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
                            volumeBooster = volumeBooster,
                            onVolumeChange = {
                                volumeBooster = it
                                // Normaliza volumen 0..200% al reproductor
                                controller?.volume = (it / 100f).coerceIn(0f, 1f)
                            },
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
                        ThemeSettingsScreen(
                            settings = themeSettingsDraft,
                            onSettingsChange = { themeSettingsDraft = it }
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
                position = position,
                duration = duration,
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
private fun MusicPlaylistCard(
    playlist: com.musica.paralosoidos.data.MusicPlaylist,
    allTracks: List<TrackEntity>,
    onPlay: (List<TrackEntity>) -> Unit,
    onAddTrack: (TrackEntity) -> Unit,
    onRemoveTrack: (TrackEntity) -> Unit,
    onMoveTrack: (TrackEntity, Int) -> Unit
) {
    val selectedTracks = playlist.trackIds.mapNotNull { id -> allTracks.firstOrNull { it.id == id } }
    var addMenuExpanded by remember(playlist.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(playlist.name, fontWeight = FontWeight.Bold)
                    Text(
                        "${selectedTracks.size} pistas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { addMenuExpanded = true }) {
                    Icon(Icons.Default.MusicNote, contentDescription = "Agregar pista")
                }
                DropdownMenu(
                    expanded = addMenuExpanded,
                    onDismissRequest = { addMenuExpanded = false }
                ) {
                    allTracks.filterNot { it.id in playlist.trackIds }.forEach { track ->
                        DropdownMenuItem(
                            text = { Text(track.title, maxLines = 1) },
                            onClick = {
                                onAddTrack(track)
                                addMenuExpanded = false
                            }
                        )
                    }
                }
                IconButton(onClick = { onPlay(selectedTracks) }, enabled = selectedTracks.isNotEmpty()) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir lista")
                }
            }
            selectedTracks.forEach { track ->
                val trackIndex = playlist.trackIds.indexOf(track.id)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${track.title} · ${track.artist}",
                        modifier = Modifier.weight(1f).clickable { onPlay(selectedTracks) }.padding(vertical = 6.dp),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                    IconButton(
                        enabled = trackIndex > 0,
                        onClick = { onMoveTrack(track, -1) }
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Subir pista en la lista")
                    }
                    IconButton(
                        enabled = trackIndex >= 0 && trackIndex < playlist.trackIds.lastIndex,
                        onClick = { onMoveTrack(track, 1) }
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Bajar pista en la lista")
                    }
                    IconButton(onClick = { onRemoveTrack(track) }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar pista de la lista")
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPlayer(
    track: TrackEntity,
    playing: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    progress: Float
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
                progress = { progress },
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
    position: Float,
    duration: Float,
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
    var sliderPosition by remember(track.id) { mutableFloatStateOf(position) }
    var isSeeking by remember(track.id) { mutableStateOf(false) }
    LaunchedEffect(position, isSeeking) {
        if (!isSeeking) sliderPosition = position
    }

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
                    positionMs = position.toLong()
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

        Slider(
            value = sliderPosition.coerceIn(0f, duration.coerceAtLeast(1f)),
            onValueChange = {
                isSeeking = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                controller?.seekTo(sliderPosition.toLong())
                isSeeking = false
            },
            valueRange = 0f..duration.coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(position.toLong() / 1000), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(duration.toLong() / 1000), style = MaterialTheme.typography.labelSmall)
        }

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
private fun LyricsPane(
        track: TrackEntity,
        positionMs: Long
    ) {
        val context = LocalContext.current
        var lines by remember(track.id) { mutableStateOf(emptyList<com.musica.paralosoidos.playback.LyricLine>()) }
        var message by remember(track.id) { mutableStateOf("Buscando letras...") }
        val activeLine = remember(lines, positionMs) {
            com.musica.paralosoidos.playback.LrcLyrics.currentLineIndex(lines, positionMs)
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
    volumeBooster: Float,
    onVolumeChange: (Float) -> Unit,
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

        // Selector de volumen
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0x0DFFFFFF), RoundedCornerShape(14.dp))
                .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Volumen y Ganancia Preamp (Volumen++)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    buildString {
                        append("${volumeBooster.toInt()}%")
                        if (volumeBooster > 100f) append(" (Amplificado)")
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(Color(0x26FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Slider(
                value = volumeBooster,
                onValueChange = onVolumeChange,
                valueRange = 0f..200f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color(0x1AFFFFFF)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100% (Normal)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("200% (Maximo)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                    "Bandas de Frecuencia (-12dB a +12dB):",
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
    val progress = ((value.coerceIn(-12, 12) + 12) / 24f)

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
                    currentValue((requested.coerceIn(0f, 1f) * 24f - 12f).toInt())
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
