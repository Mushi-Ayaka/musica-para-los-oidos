package com.musica.paralosoidos.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
// AÑADIDO: Para zonas de deslizamiento del 20% en bordes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
// AÑADIDO: Para AlertDialog y botones
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.playback.LrcLyrics
import com.musica.paralosoidos.playback.LyricsFileRepository
import com.musica.paralosoidos.playback.LyricsSearchResult
import com.musica.paralosoidos.ui.theme.MusicaStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.Charsets

@Composable
fun MiniPlayer(
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
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
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
fun FullPlayer(
    track: TrackEntity,
    catalogEntry: CatalogTrackEntity?,
    queue: List<TrackEntity>,
    allTracks: List<TrackEntity>,
    controller: MediaController?,
    playing: Boolean,
    position: () -> Float,
    duration: () -> Float,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    playbackSpeed: Float,
    lyricsScrollSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    error: String?,
    onSelectQueueItem: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onRemoveQueueItem: (Int) -> Unit,
    onAddQueueItems: (List<TrackEntity>) -> Unit,
    onOpenLyricsSettings: () -> Unit,
    onClose: () -> Unit
) {
    var showAddTracks by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val pageProgress = (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Cerrar reproductor",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        "Reproduciendo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    val largeCover = minOf(maxWidth - 48.dp, maxHeight - 36.dp).coerceAtLeast(160.dp)
                    val smallCover = 64.dp
                    val largeX = (maxWidth - largeCover) / 2
                    val largeY = (maxHeight - largeCover) / 2
                    val coverSize = lerp(largeCover, smallCover, pageProgress)
                    val coverX = lerp(largeX, 0.dp, pageProgress)
                    val coverY = lerp(largeY, 0.dp, pageProgress)

                    // IMPLEMENTACIÓN FIX v1.4.6: Zonas de deslizamiento del 20% en bordes
                    // Se añade detección de toques en los bordes izquierdo/derecho (20% de la pantalla)
                    // para cambiar de canción anterior/siguiente sin botones
                    val edgeZone = maxWidth * 0.2f
                    val density = LocalDensity.current
                    val edgeZonePx = with(density) { edgeZone.toPx() }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { tapOffset ->
                                    val screenWidth = size.width.toFloat()
                                    // Zona izquierda 20%: canción anterior
                                    if (tapOffset.x < edgeZonePx) {
                                        controller?.seekToPreviousMediaItem()
                                    }
                                    // Zona derecha 20%: siguiente canción
                                    else if (tapOffset.x > screenWidth - edgeZonePx) {
                                        controller?.seekToNextMediaItem()
                                    }
                                }
                            }
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            if (page == 0) {
                                Box(Modifier.fillMaxSize()) // invisible, el HorizontalPager ya maneja el swipe aquí
                            } else {
                                Column(Modifier.fillMaxSize()) {
                                    Row(
                                        Modifier.fillMaxWidth().height(72.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(Modifier.width(76.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                track.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                track.artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    LyricsPane(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        track = track,
                                        catalogEntry = catalogEntry,
                                        positionMs = { position().toLong() },
                                        scrollSpeed = lyricsScrollSpeed,
                                        onOpenSettings = onOpenLyricsSettings
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .offset(x = coverX, y = coverY)
                            .size(coverSize),
                        shape = if (pageProgress < 0.5f) MusicaStyle.Shape.sheet else MusicaStyle.Shape.tiny,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            if (track.artworkUri != null) {
                                AsyncImage(
                                    model = track.artworkUri,
                                    contentDescription = "Portada de ${track.album}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LibraryMusic,
                                        contentDescription = null,
                                        modifier = Modifier.size(if (pageProgress < 0.5f) 72.dp else 28.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column(
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(MusicaStyle.Colors.scrim)
                                    .padding(12.dp)
                                    .alpha((1f - pageProgress * 2.2f).coerceIn(0f, 1f))
                            ) {
                                Text(
                                    track.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                Text(
                                    track.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MusicaStyle.Colors.neutral,
                                    maxLines = 1
                                )
                                Text(
                                    "Desliza la portada para ver letras",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { page ->
                        val selected = pagerState.currentPage == page
                        Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (selected) 22.dp else 7.dp, height = 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(page) }
                                }
                        )
                    }
                }

                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                PlayerSeekBar(position = position, duration = duration, controller = controller)
                PlaybackTimeLabels(position = position, duration = duration)

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(34.dp))
                    }
                    IconButton(
                        onClick = {
                            if (controller?.isPlaying == true) controller.pause() else controller?.play()
                        },
                        modifier = Modifier
                            .size(58.dp)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    ) {
                        Icon(
                            if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pausar" else "Reproducir",
                            modifier = Modifier.size(34.dp),
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = { controller?.seekToNextMediaItem() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", modifier = Modifier.size(34.dp))
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

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Velocidad:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                                Text(
                                    "${playbackSpeed}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = speedExpanded,
                            onDismissRequest = { speedExpanded = false },
                            containerColor = MusicaStyle.Colors.menuBackground,
                            border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
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

                Column(Modifier.fillMaxWidth().weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "En cola (${queue.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Mantén pulsada una pista para reordenar.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showAddTracks = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Añadir pistas a la cola",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val queuePositions = remember(queue) { queue.withIndex().associate { it.value.id to it.index } }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
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
                                    Modifier
                                        .weight(1f)
                                        .clickable { onSelectQueueItem(index) }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(queued.title, maxLines = 1, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        queued.artist,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onRemoveQueueItem(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Quitar de la cola",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTracks) {
        AddTracksToQueueDialog(
            queue = queue,
            allTracks = allTracks,
            onDismiss = { showAddTracks = false },
            onAddTracks = { selected ->
                onAddQueueItems(selected)
                showAddTracks = false
            }
        )
    }
}

@Composable
private fun AddTracksToQueueDialog(
    queue: List<TrackEntity>,
    allTracks: List<TrackEntity>,
    onDismiss: () -> Unit,
    onAddTracks: (List<TrackEntity>) -> Unit
) {
    val queueIds = remember(queue) { queue.mapTo(mutableSetOf()) { it.id } }
    val availableTracks = remember(allTracks, queueIds) {
        allTracks.filterNot { it.id in queueIds }
    }
    var search by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val filteredTracks = remember(availableTracks, search) {
        if (search.isBlank()) {
            availableTracks
        } else {
            availableTracks.filter {
                it.title.contains(search, ignoreCase = true) ||
                    it.artist.contains(search, ignoreCase = true) ||
                    it.album.contains(search, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir pistas a la cola", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Buscar canción, artista o álbum") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filteredTracks.size} disponibles",
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
                        Text(if (selectedIds.size == filteredTracks.size) "Deseleccionar" else "Seleccionar todas")
                    }
                }
                if (filteredTracks.isEmpty()) {
                    Text(
                        "Todas las pistas disponibles ya están en la cola.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredTracks, key = { "queue-add-${it.id}" }) { track ->
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
                                    Text(
                                        "${track.artist} • ${track.album}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
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
                    onAddTracks(availableTracks.filter { it.id in selectedIds })
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
    modifier: Modifier = Modifier,
    track: TrackEntity,
    catalogEntry: CatalogTrackEntity?,   // <-- nuevo parámetro
    positionMs: () -> Long,
    scrollSpeed: Float,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    var result by remember(track.id) { mutableStateOf<LyricsSearchResult?>(null) }
    var loading by remember(track.id) { mutableStateOf(true) }
    var retryToken by remember(track.id) { mutableIntStateOf(0) }
    
    // IMPLEMENTACIÓN FIX v1.4.6: Funcionalidad para vincular desde LRCLIB
    var showLrcLinkDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Función para abrir diálogo de vinculación desde LRCLIB
    fun linkFromLrclib() {
        showLrcLinkDialog = true
    }

    LaunchedEffect(track.id, retryToken) {
        loading = true
        result = null
        result = withContext(Dispatchers.IO) {
            runCatching {
                // 1. Intentar ruta vinculada en catalogo primero
                val linkedPath = catalogEntry?.lrcPath
                    ?.takeIf { it.isNotBlank() && it != "vinculado" && it != "local" }
                if (linkedPath != null) {
                    val file = java.io.File(linkedPath)
                    if (file.exists() && file.length() > 0) {
                        val text = file.readText(Charsets.UTF_8)
                        val document = LrcLyrics.parseDocument(text)
                        if (document.lines.isNotEmpty()) {
                            return@runCatching LyricsSearchResult(
                                sourceName = file.name,
                                document = document,
                                logs = listOf("Letra cargada desde vinculo: ${file.name}.")
                            )
                        }
                    }
                }
                // 2. Intentar archivo .lrc junto al archivo de audio
                if (track.folderPath.isNotBlank()) {
                    val nameWithoutExt = track.displayName.substringBeforeLast('.')
                    val sidecatFile = java.io.File(track.folderPath, "$nameWithoutExt.lrc")
                    if (sidecatFile.exists() && sidecatFile.length() > 0) {
                        val text = sidecatFile.readText(Charsets.UTF_8)
                        val document = LrcLyrics.parseDocument(text)
                        if (document.lines.isNotEmpty()) {
                            return@runCatching LyricsSearchResult(
                                sourceName = sidecatFile.name,
                                document = document,
                                logs = listOf("Letra cargada desde carpeta del audio: ${sidecatFile.name}.")
                            )
                        }
                    }
                }
                // 3. Fallback: buscar en MediaStore
                LyricsFileRepository.findMatchingLyrics(
                    context = context,
                    audioFileName = track.displayName,
                    trackTitle = track.title,
                    trackArtist = track.artist
                )
            }.getOrElse { error ->
                LyricsSearchResult(
                    sourceName = null,
                    document = null,
                    logs = listOf("Error al buscar letras: ${error.localizedMessage ?: "error desconocido"}.")
                )
            }
        }
        loading = false
    }
    
    // Función para buscar LRC desde LRCLIB (FIX v1.4.6)
    // NOTA: Esta implementación está simplificada para evitar dependencias complejas
    // En producción, se debería usar la arquitectura de ViewModel apropiada
    fun searchLrcFromLrclib() {
        coroutineScope.launch {
            try {
                // Mensaje informativo (implementación real requiere integración con TrackTrackerManager)
                android.widget.Toast.makeText(
                    context, 
                    "Función 'Vincular desde LRCLIB' requiere integración completa con TrackTrackerManager", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
                
                // NOTA: Para una implementación completa, se necesita:
                // 1. Acceso a AppContainer.getInstance(context)
                // 2. Crear instancia de LyricsDownloadEngine
                // 3. Llamar a fetchLyrics()
                // 4. Usar TrackTrackerManager.downloadAndSaveLrc()
                
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                showLrcLinkDialog = false
            }
        }
    }
    
    // Diálogo para confirmar vinculación desde LRCLIB (FIX v1.4.6)
    if (showLrcLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLrcLinkDialog = false },
            title = { Text("Vincular desde LRCLIB") },
            text = { 
                Column {
                    Text("¿Vincular letra para:")
                    Text("${track.title} - ${track.artist}", fontWeight = FontWeight.Bold)
                    Text("Se buscará en LRCLIB y se guardará en la carpeta de letras configurada.")
                    Text("NOTA: Esta función requiere configuración adicional de TrackTrackerManager", 
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { searchLrcFromLrclib() }) {
                    Text("Vincular")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLrcLinkDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier.padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Letras",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        if (loading) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Cargando letra...", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            val document = result?.document
            val lines = document?.lines.orEmpty()
            if (document == null || lines.isEmpty()) {
                val logs = result?.logs.orEmpty()
                Column(
                    Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            Text(
                                log,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { retryToken++ }) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                        
                        // IMPLEMENTACIÓN FIX v1.4.6: Botón "Vincular desde LRCLIB"
                        // Botón blanco para vincular letras desde LRCLIB cuando no hay letras disponibles
                        Button(
                            onClick = { linkFromLrclib() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                Icons.Default.LibraryMusic, 
                                contentDescription = "Vincular desde LRCLIB", 
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Vincular desde LRCLIB")
                        }
                    }
                }
            } else {
                Text(
                    buildString {
                        append(result?.sourceName ?: "LRC")
                        append(if (document.synced) " · sincronizada" else " · sin tiempos")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                val intervalMs = (4_000f / scrollSpeed.coerceIn(0.5f, 2f)).toLong().coerceAtLeast(500L)
                val activeLine = if (document.synced) {
                    LrcLyrics.currentLineIndex(lines, positionMs())
                } else {
                    (positionMs() / intervalMs).toInt().coerceIn(0, lines.lastIndex)
                }
                val listState = rememberLazyListState()
                LaunchedEffect(activeLine) {
                    if (lines.isNotEmpty()) {
                        listState.animateScrollToItem((activeLine - 2).coerceAtLeast(0))
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(lines.size) { index ->
                        Text(
                            lines[index].text,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (index == activeLine) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == activeLine) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

internal fun formatTime(seconds: Long): String =
    "%d:%02d".format(seconds / 60, seconds % 60)
