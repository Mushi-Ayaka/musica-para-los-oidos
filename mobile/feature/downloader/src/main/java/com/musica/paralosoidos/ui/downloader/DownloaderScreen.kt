package com.musica.paralosoidos.ui.downloader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.musica.paralosoidos.data.DownloadHistoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderScreen(
    viewModel: DownloaderViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isServiceBlocked by viewModel.isServiceBlocked.collectAsStateWithLifecycle()
    val serviceNoticeMessage by viewModel.serviceNoticeMessage.collectAsStateWithLifecycle()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()
    val isQueueSheetOpen by viewModel.isQueueSheetOpen.collectAsStateWithLifecycle()
    val downloadHistory by viewModel.downloadHistory.collectAsStateWithLifecycle()

    val canPerformActions = isOnline && !isServiceBlocked
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header with Queue Badge Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MUSICA PARA LOS OIDOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.1.sp
                )
                Text(
                    "Descargar",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            BadgedBox(
                badge = {
                    if (activeCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "$activeCount",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                IconButton(onClick = viewModel::openQueueSheet) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Ver cola de descargas",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Offline Autoprotection Banner
        if (!isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sin conexion a Internet. Las descargas y busquedas se habilitaran automaticamente al conectarse.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Service Blocked (YouTube Bot Detection / OTA update) Banner
        if (isServiceBlocked) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = serviceNoticeMessage ?: "Servicio de descargas temporalmente no disponible por cambios en YouTube. Se actualizara automaticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.retryService() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reintentar", fontSize = 12.sp)
                    }
                }
            }
        }

        // User Feedback (Snack-style) Notice
        if (userFeedbackMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = userFeedbackMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search Bar
        DownloaderSearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChanged,
            onSearch = viewModel::search,
            isSearching = isSearching,
            enabled = canPerformActions
        )

        // Quick Config Bar
        QuickConfigBar(
            settings = settings,
            onSelectQuality = viewModel::updateQuality,
            onToggleSyncLyrics = viewModel::toggleSyncLyrics,
            enabled = canPerformActions
        )

        // Results or Empty State
        if (searchResults.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resultados (${searchResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            searchResults.forEach { result ->
                DownloadResultCard(
                    result = result,
                    activeSettings = settings,
                    onDownload = viewModel::downloadTrack,
                    enabled = canPerformActions
                )
            }
        } else if (!isSearching) {
            // Guide / Initial Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Text(
                        "Busca musica en YouTube",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Escribe el nombre de la cancion, artista o pega un enlace de YouTube directamente para comenzar a descargar.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // Download History Section
    if (downloadHistory.isNotEmpty()) {
        val historyExpanded = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { historyExpanded.value = !historyExpanded.value }
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Historial de descargas (${downloadHistory.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                if (historyExpanded.value) "Ocultar" else "Ver todo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (historyExpanded.value) {
            downloadHistory.take(20).forEach { item ->
                DownloadHistoryItem(item = item)
            }
        }
    }

    // Modal Bottom Sheet for Queue
    if (isQueueSheetOpen) {
        DownloadQueueSheet(
            queue = queue,
            sheetState = queueSheetState,
            onDismiss = viewModel::closeQueueSheet,
            onPause = viewModel::pauseTask,
            onResume = viewModel::resumeTask,
            onRetry = viewModel::retryTask,
            onCancel = viewModel::cancelTask,
            onClearCompleted = viewModel::clearCompletedTasks
        )
    }
}


@Composable
private fun DownloadHistoryItem(
    item: DownloadHistoryEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lrcFile = remember(item.filePath) {
        val mp3 = File(item.filePath)
        File(mp3.parent ?: "", mp3.nameWithoutExtension + ".lrc")
    }
    val hasLrc = remember(lrcFile) { lrcFile.exists() }
    val dateStr = remember(item.downloadedAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.downloadedAt))
    }
    val qualityLabel = if (item.format == "MP3" && item.bitrate != null) {
        "MP3 ${item.bitrate}K"
    } else {
        item.format
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        item.channel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        qualityLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            if (hasLrc) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Letra",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}
