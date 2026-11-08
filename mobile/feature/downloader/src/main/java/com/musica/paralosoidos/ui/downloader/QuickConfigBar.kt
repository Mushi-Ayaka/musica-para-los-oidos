package com.musica.paralosoidos.ui.downloader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.DownloadSettingsState

data class QualityOption(
    val format: AudioOutputFormat,
    val bitrate: Int?,
    val title: String,
    val subtitle: String
)

val QUALITY_OPTIONS = listOf(
    QualityOption(AudioOutputFormat.MP3, 320, "320 kbps MP3", "Alta fidelidad"),
    QualityOption(AudioOutputFormat.MP3, 256, "256 kbps MP3", "Equilibrado"),
    QualityOption(AudioOutputFormat.MP3, 128, "128 kbps MP3", "Rapido y ligero"),
    QualityOption(AudioOutputFormat.BEST, null, "Mejor calidad disponible", "Formato original de la fuente")
)

@Composable
fun QuickConfigBar(
    settings: DownloadSettingsState,
    onSelectQuality: (AudioOutputFormat, Int?) -> Unit,
    onToggleSyncLyrics: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    val currentQualityLabel = when {
        settings.format == AudioOutputFormat.BEST -> "Mejor calidad original"
        settings.format == AudioOutputFormat.MP3 && settings.mp3Bitrate != null -> "${settings.mp3Bitrate} kbps MP3"
        else -> "320 kbps MP3"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quality Dropdown Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled) { expandedDropdown = true }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.HighQuality,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Calidad predeterminada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            currentQualityLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar opciones de calidad",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        QUALITY_OPTIONS.forEach { option ->
                            val isSelected = (settings.format == option.format) &&
                                (option.bitrate == null || settings.mp3Bitrate == option.bitrate)
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            option.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            option.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    expandedDropdown = false
                                    onSelectQuality(option.format, option.bitrate)
                                }
                            )
                        }
                    }
                }
            }

            // Sync LRC Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Sincronizar .lrc",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Descargar archivo de letra sincronizada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = settings.syncLyrics,
                    onCheckedChange = { if (enabled) onToggleSyncLyrics(it) },
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}
