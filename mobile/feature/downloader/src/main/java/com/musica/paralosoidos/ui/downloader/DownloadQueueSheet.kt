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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.AudioOutputFormat
import com.musica.paralosoidos.data.DownloadTask
import com.musica.paralosoidos.data.DownloadTaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadQueueSheet(
    queue: List<DownloadTask>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onRetry: (String) -> Unit,
    onCancel: (String) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasCompleted = queue.any { it.status == DownloadTaskStatus.COMPLETED }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cola de descargas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${queue.size} tareas en total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasCompleted) {
                    TextButton(onClick = onClearCompleted) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar completadas", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List or Empty State
            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "No hay descargas en la cola",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Las pistas que descargues apareceran aqui para que sigas su progreso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(queue, key = { it.id }) { task ->
                        DownloadTaskCard(
                            task = task,
                            onPause = { onPause(task.id) },
                            onResume = { onResume(task.id) },
                            onRetry = { onRetry(task.id) },
                            onCancel = { onCancel(task.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DownloadTaskCard(
    task: DownloadTask,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatDescription = if (task.format == AudioOutputFormat.BEST) {
        "Original"
    } else {
        "${task.bitrate ?: 320} kbps MP3"
    } + if (task.syncLyrics) " • Con letra LRC" else ""

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (task.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = task.thumbnailUrl,
                            contentDescription = task.title,
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = task.channel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Badge Chip
                DownloadStatusChip(status = task.status)
            }

            // Progress Bar if DOWNLOADING
            if (task.status == DownloadTaskStatus.DOWNLOADING) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { task.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${task.progress}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (task.speed.isNotBlank()) {
                            Text(
                                text = task.speed,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Error notice if ERROR
            val errorMsg = task.errorMessage
            if (task.status == DownloadTaskStatus.ERROR && !errorMsg.isNullOrBlank()) {
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (task.status) {
                    DownloadTaskStatus.DOWNLOADING -> {
                        IconButton(onClick = onPause) {
                            Icon(
                                Icons.Default.Pause,
                                contentDescription = "Pausar descarga",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar descarga",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DownloadTaskStatus.PAUSED -> {
                        IconButton(onClick = onResume) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Reanudar descarga",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar descarga",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DownloadTaskStatus.ERROR -> {
                        IconButton(onClick = onRetry) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reintentar descarga",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar de la lista",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    DownloadTaskStatus.COMPLETED -> {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar de la lista",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DownloadTaskStatus.QUEUED -> {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Quitar de la cola",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadStatusChip(status: DownloadTaskStatus) {
    val (label, containerColor, contentColor, icon) = when (status) {
        DownloadTaskStatus.QUEUED -> Quadruple(
            "En cola",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.HourglassEmpty
        )
        DownloadTaskStatus.DOWNLOADING -> Quadruple(
            "Descargando",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Downloading
        )
        DownloadTaskStatus.PAUSED -> Quadruple(
            "Pausado",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.PauseCircle
        )
        DownloadTaskStatus.COMPLETED -> Quadruple(
            "Completado",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.CheckCircle
        )
        DownloadTaskStatus.ERROR -> Quadruple(
            "Error",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.ErrorOutline
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
