package com.musica.paralosoidos.ui.equalizer

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
internal fun EqualizerScreenContent(
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
                    .clip(MusicaStyle.Shape.pill)
                    .background(
                        if (isEnabled) MaterialTheme.colorScheme.primary
                        else MusicaStyle.Colors.borderStrong
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
                .background(MusicaStyle.Colors.divider, MusicaStyle.Shape.row)
                .border(1.dp, MusicaStyle.Colors.border, MusicaStyle.Shape.row)
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
                        .background(MusicaStyle.Colors.overlaySoft, MusicaStyle.Shape.micro)
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
                    inactiveTrackColor = MusicaStyle.Colors.borderStrong
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
                .background(MusicaStyle.Colors.divider, MusicaStyle.Shape.row)
                .border(1.dp, MusicaStyle.Colors.border, MusicaStyle.Shape.row)
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
                    color = if (speakerBoost > 0) MusicaStyle.Colors.gold else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(MusicaStyle.Colors.overlaySoft, MusicaStyle.Shape.micro)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Slider(
                value = speakerBoost.toFloat(),
                onValueChange = { AudioEffectsManager.setSpeakerBoost(it.toInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MusicaStyle.Colors.gold,
                    activeTrackColor = MusicaStyle.Colors.gold,
                    inactiveTrackColor = MusicaStyle.Colors.borderStrong
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0% (Apagado)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("50% (+10 dB)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100% (+20 dB)", fontSize = 10.sp, color = MusicaStyle.Colors.gold, fontWeight = FontWeight.Bold)
            }
        }

        // Selector de Presets (Dropdown tipo Web)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MusicaStyle.Shape.row,
            border = BorderStroke(1.dp, MusicaStyle.Colors.border),
            colors = CardDefaults.cardColors(containerColor = MusicaStyle.Colors.divider)
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
                        shape = MusicaStyle.Shape.small,
                        color = MusicaStyle.Colors.menuBackground,
                        border = BorderStroke(1.dp, MusicaStyle.Colors.overlayMedium)
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
                        modifier = Modifier.background(MusicaStyle.Colors.menuBackgroundAlt)
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
                shape = MusicaStyle.Shape.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MusicaStyle.Colors.borderStrong,
                    focusedContainerColor = MusicaStyle.Colors.divider,
                    unfocusedContainerColor = MusicaStyle.Colors.divider
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
                shape = MusicaStyle.Shape.small,
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
            shape = MusicaStyle.Shape.row,
            border = BorderStroke(1.dp, MusicaStyle.Colors.border),
            colors = CardDefaults.cardColors(containerColor = MusicaStyle.Colors.divider)
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
                        .background(MusicaStyle.Colors.divider, MusicaStyle.Shape.row)
                        .border(1.dp, MusicaStyle.Colors.border, MusicaStyle.Shape.row)
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
                            inactiveTrackColor = MusicaStyle.Colors.borderStrong
                        )
                    )
                }

                // Sonido 3D / Virtual
                val virtualizerStrength by AudioEffectsManager.virtualizerStrength.collectAsState()
                val virtualizerAvailable by AudioEffectsManager.virtualizerAvailable.collectAsState()
                Column(
                    Modifier
                        .weight(1f)
                        .background(MusicaStyle.Colors.divider, MusicaStyle.Shape.row)
                        .border(1.dp, MusicaStyle.Colors.border, MusicaStyle.Shape.row)
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
                            inactiveTrackColor = MusicaStyle.Colors.borderStrong
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
                        shape = MusicaStyle.Shape.row,
                        border = BorderStroke(
                            if (selected) 1.5.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MusicaStyle.Colors.border
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MusicaStyle.Colors.infoSurface else MusicaStyle.Colors.divider
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
internal fun VerticalEqualizerSlider(
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
