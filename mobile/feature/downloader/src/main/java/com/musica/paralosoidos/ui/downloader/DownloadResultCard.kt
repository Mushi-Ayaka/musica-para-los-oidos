package com.musica.paralosoidos.ui.downloader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.DownloadSearchResult
import com.musica.paralosoidos.data.DownloadSettingsState

@Composable
fun DownloadResultCard(
    result: DownloadSearchResult,
    activeSettings: DownloadSettingsState,
    onDownload: (DownloadSearchResult, AudioOutputFormat, Int?) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    val activeFormat = activeSettings.format ?: AudioOutputFormat.MP3
    val activeBitrate = if (activeFormat == AudioOutputFormat.MP3) (activeSettings.mp3Bitrate ?: 320) else null

    val downloadButtonLabel = when {
        activeFormat == AudioOutputFormat.BEST -> "Descargar (Original)"
        activeBitrate != null -> "Descargar (${activeBitrate}K)"
        else -> "Descargar (320K)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Thumbnail with Duration Badge and metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 16:9 Thumbnail
                Box(
                    modifier = Modifier
                        .width(136.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (result.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = result.thumbnailUrl,
                            contentDescription = result.title,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.matchParentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Duration Badge (Bottom-Right)
                    if (result.durationSeconds > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = result.formattedDuration,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Title and Channel Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(76.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column {
                        Text(
                            text = result.channel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val viewsText = result.formattedViews
                        if (viewsText.isNotBlank()) {
                            Text(
                                text = viewsText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Download Button
                Button(
                    onClick = { onDownload(result, activeFormat, activeBitrate) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = downloadButtonLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                // Options Menu Button
                Box {
                    OutlinedIconButton(
                        onClick = { showOptionsMenu = true },
                        enabled = enabled,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Opciones avanzadas de formato",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        Text(
                            "Descargar con formato especifico",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        QUALITY_OPTIONS.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(opt.title, fontWeight = FontWeight.SemiBold)
                                        Text(opt.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onDownload(result, opt.format, opt.bitrate)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
