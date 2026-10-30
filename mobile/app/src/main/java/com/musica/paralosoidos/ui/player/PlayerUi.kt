package com.musica.paralosoidos.ui.player

import java.util.Locale

import com.musica.paralosoidos.ui.theme.MusicaStyle

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

@Composable
internal fun MiniPlayer(
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
        shape = MusicaStyle.Shape.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
    ) {
        Column {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MusicaStyle.Colors.borderStrong
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
                            .clip(MusicaStyle.Shape.tiny),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Card(
                        modifier = Modifier.size(42.dp),
                        shape = MusicaStyle.Shape.tiny,
                        colors = CardDefaults.cardColors(containerColor = MusicaStyle.Colors.borderStrong)
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
internal fun FullPlayer(
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
                        shape = MusicaStyle.Shape.sheet,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
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
                    shape = MusicaStyle.Shape.tiny,
                    color = MusicaStyle.Colors.borderStrong,
                    border = BorderStroke(1.dp, MusicaStyle.Colors.overlaySoft)
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
                    modifier = Modifier.background(MusicaStyle.Colors.menuBackground)
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
        val queuePositions = remember(queue) { queue.withIndex().associate { it.value.id to it.index } }
        if (showAddTracks) {
            val queueIds = queue.mapTo(mutableSetOf()) { it.id }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allTracks.filterNot { it.id in queueIds }, key = { "add-${it.id}" }) { candidate ->
                    Surface(
                        onClick = { onAddQueueItem(candidate) },
                        shape = MusicaStyle.Shape.small,
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
                    val index = queuePositions[queued.id] ?: 0
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
internal fun PlayerSeekBar(
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
internal fun PlaybackTimeLabels(
    position: () -> Float,
    duration: () -> Float
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatTime(position().toLong() / 1000), style = MaterialTheme.typography.labelSmall)
        Text(formatTime(duration().toLong() / 1000), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun LyricsPane(
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

internal fun formatTime(seconds: Long): String =
    "%d:%02d".format(seconds / 60, seconds % 60)
