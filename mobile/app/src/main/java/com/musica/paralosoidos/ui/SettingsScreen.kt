package com.musica.paralosoidos.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.ThemeSettingsState
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.ui.design.SectionTitle
import com.musica.paralosoidos.ui.theme.MusicaStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    themeSettings: ThemeSettingsState,
    lyricsScrollSpeed: Float,
    crossfadeSeconds: Int,
    trackerFrequency: String,
    trackerIntensity: String,
    trackerCircleColorHex: String,
    downloadConcurrency: Int,
    downloadFolderLabel: String?,
    autoBackupInterval: String,
    tracks: List<TrackEntity>,
    visibleTracks: List<TrackEntity>,
    catalogTracks: List<CatalogTrackEntity>,
    trackerSyncProgress: Pair<Int, Int>?,
    isTrackerSyncing: Boolean,
    themeMessage: String?,
    onSaveThemeSettings: (ThemeSettingsState) -> Unit,
    onSaveLyricsScrollSpeed: (Float) -> Unit,
    onSaveCrossfadeSeconds: (Int) -> Unit,
    onSaveTrackerFrequency: (String) -> Unit,
    onSaveTrackerIntensity: (String) -> Unit,
    onSaveTrackerCircleColor: (String) -> Unit,
    onSaveDownloadConcurrency: (Int) -> Unit,
    onPickDownloadFolder: () -> Unit,
    onPinWidget: () -> Unit,
    onExportCentralBackup: () -> Unit,
    onImportCentralBackup: () -> Unit,
    onImportThemes: () -> Unit,
    onImportPlaylistJson: () -> Unit,
    onUploadDriveBackup: () -> Unit,
    onSaveAutoBackupInterval: (String) -> Unit,
    onStartTrackerSync: () -> Unit,
    onStopTrackerSync: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Ajustes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Apariencia, reproducción, descargas y copias de seguridad.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            ThemeSettingsSection(
                savedSettings = themeSettings,
                onSave = onSaveThemeSettings
            )
        }

        item {
            LyricsSettingsSection(
                scrollSpeed = lyricsScrollSpeed,
                onScrollSpeedChange = onSaveLyricsScrollSpeed
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Reproducción y Widget", "Fundido entre canciones y acceso rápido desde el escritorio.")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MusicaStyle.Shape.row,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Crossfade (fundido entre canciones)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (crossfadeSeconds > 0) "Activo: $crossfadeSeconds segundos" else "Desactivado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0 to "Off", 2 to "2s", 4 to "4s", 6 to "6s", 8 to "8s", 10 to "10s").forEach { (sec, label) ->
                                    FilterChip(
                                        selected = crossfadeSeconds == sec,
                                        onClick = { onSaveCrossfadeSeconds(sec) },
                                        label = { Text(label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MusicaStyle.Colors.border)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                Text(
                                    "Widget de escritorio (4×1)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Portada, título y controles de reproducción en tu pantalla de inicio.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onPinWidget,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Añadir")
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Trackers y Descargas", "Ajusta la vinculación automática, la carpeta y la concurrencia.")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MusicaStyle.Shape.row,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val frequencyLabel = when (trackerFrequency) {
                            "ON_APP_OPEN" -> "Al abrir la app"
                            "EVERY_6_HOURS" -> "Cada 6 horas"
                            "DAILY" -> "Diario"
                            "MANUAL_ONLY" -> "Solo manual"
                            else -> "Al añadir canciones"
                        }
                        SettingDropdownRow(
                            title = "Frecuencia de Tracker",
                            selectedLabel = frequencyLabel,
                            options = listOf(
                                "ON_NEW_TRACKS_ONLY" to "Al añadir canciones",
                                "ON_APP_OPEN" to "Al abrir la app",
                                "EVERY_6_HOURS" to "Cada 6 horas",
                                "DAILY" to "Diario",
                                "MANUAL_ONLY" to "Solo manual"
                            ),
                            onSelect = onSaveTrackerFrequency
                        )

                        val intensityLabel = when (trackerIntensity) {
                            "LOW" -> "Baja (ahorro batería/red)"
                            "HIGH" -> "Alta (rápida)"
                            else -> "Media (3 en lote)"
                        }
                        SettingDropdownRow(
                            title = "Intensidad de Tracker",
                            selectedLabel = intensityLabel,
                            options = listOf(
                                "LOW" to "Baja (ahorro batería/red)",
                                "MEDIUM" to "Media (3 en lote)",
                                "HIGH" to "Alta (rápida)"
                            ),
                            onSelect = onSaveTrackerIntensity
                        )

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
                                            .clickable { onSaveTrackerCircleColor(hex) }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Descargas simultáneas", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Máximo $downloadConcurrency a la vez", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 3).forEach { count ->
                                    val selected = downloadConcurrency == count
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSaveDownloadConcurrency(count) },
                                        label = { Text("$count", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MusicaStyle.Colors.border)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                Text(
                                    "Carpeta de descarga",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    downloadFolderLabel?.let { "Actual: $it" } ?: "Sin configurar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (downloadFolderLabel != null)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }
                            OutlinedButton(
                                onClick = onPickDownloadFolder,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (downloadFolderLabel != null) "Cambiar" else "Elegir")
                            }
                        }
                    }
                }
            }
        }

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
                        OutlinedButton(onClick = onExportCentralBackup, modifier = Modifier.fillMaxWidth()) {
                            Text("Exportar copia completa (.zip)")
                        }
                        OutlinedButton(onClick = onImportCentralBackup, modifier = Modifier.fillMaxWidth()) {
                            Text("Restaurar copia completa")
                        }
                        OutlinedButton(onClick = onImportThemes, modifier = Modifier.fillMaxWidth()) {
                            Text("Importar temas")
                        }
                        OutlinedButton(onClick = onImportPlaylistJson, modifier = Modifier.fillMaxWidth()) {
                            Text("Importar lista de reproducción (.json)")
                        }
                        Button(onClick = onUploadDriveBackup, modifier = Modifier.fillMaxWidth()) {
                            Text("Subir copia a Google Drive")
                        }

                        val autoBackupLabel = when (autoBackupInterval) {
                            "HOURLY" -> "Cada 1 hora"
                            "SIX_HOURS" -> "Cada 6 horas"
                            "OFF" -> "Desactivado"
                            else -> "Diario (24h)"
                        }
                        SettingDropdownRow(
                            title = "Respaldo periódico automático",
                            selectedLabel = autoBackupLabel,
                            options = listOf(
                                "DAILY" to "Diario (24h)",
                                "SIX_HOURS" to "Cada 6 horas",
                                "HOURLY" to "Cada 1 hora",
                                "OFF" to "Desactivado"
                            ),
                            onSelect = onSaveAutoBackupInterval
                        )
                    }
                }
            }
        }

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
                        val localUris = tracks.map { it.uri }.toSet()
                        val activeCatalog = catalogTracks.filter { it.localUri != null && it.localUri in localUris }
                        val ytCount = activeCatalog.count { !it.youtubeUrl.isNullOrBlank() }
                        val lrcCount = activeCatalog.count { it.lrcSynced || !it.lrcPath.isNullOrBlank() }

                        Text("Canciones en biblioteca: ${tracks.size}", style = MaterialTheme.typography.bodyMedium)
                        Text("Vinculadas a YouTube: $ytCount / ${tracks.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Con letra LRC: $lrcCount / ${tracks.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

                        if (trackerSyncProgress != null) {
                            val (cur, tot) = trackerSyncProgress
                            Text("Sincronizando: $cur / $tot...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = onStartTrackerSync,
                            enabled = !isTrackerSyncing && visibleTracks.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isTrackerSyncing) "Sincronizando..." else "Sincronizar Trackers ahora")
                        }

                        if (isTrackerSyncing) {
                            OutlinedButton(
                                onClick = onStopTrackerSync,
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
            item { Text(message, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun SettingDropdownRow(
    title: String,
    selectedLabel: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MusicaStyle.Shape.tiny)
            .clickable { expanded = true }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(selectedLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(10.dp),
                color = MusicaStyle.Colors.borderStrong,
                border = BorderStroke(1.dp, MusicaStyle.Colors.overlaySoft)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Cambiar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MusicaStyle.Colors.menuBackground
            ) {
                options.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            expanded = false
                            onSelect(key)
                        }
                    )
                }
            }
        }
    }
}
