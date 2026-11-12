package com.musica.paralosoidos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.DownloadSearchResult
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.data.tracker.TrackTrackerManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackLinksBottomSheet(
    track: TrackEntity,
    catalogEntry: CatalogTrackEntity?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (youtubeUrl: String?, lrcPath: String?, lrcSynced: Boolean) -> Unit,
    onAutoSearchYouTube: suspend () -> DownloadSearchResult?,
    onAutoSearchLrc: suspend () -> String?,
    onDownloadLrc: suspend (String) -> Boolean,
    onUnlink: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var ytUrl by remember(catalogEntry) { mutableStateOf(catalogEntry?.youtubeUrl.orEmpty()) }
    var lrcPath by remember(catalogEntry) { mutableStateOf(catalogEntry?.lrcPath.orEmpty()) }
    var isLrcSynced by remember(catalogEntry) { mutableStateOf(catalogEntry?.lrcSynced ?: false) }

    var isSearchingYt by remember { mutableStateOf(false) }
    var isSearchingLrc by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingYouTubeMatch by remember { mutableStateOf<DownloadSearchResult?>(null) }

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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vínculos de la pista",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${track.title} - ${track.artist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 1: YouTube Tracker
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "YouTube Tracker",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        val isYtLinked = ytUrl.isNotBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isYtLinked) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (isYtLinked) "Vinculado" else "Sin vincular",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isYtLinked) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ytUrl,
                        onValueChange = { ytUrl = it },
                        label = { Text("URL de YouTube") },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isSearchingYt = true
                                statusMessage = null
                                try {
                                    val match = onAutoSearchYouTube()
                                    if (match != null) {
                                        if (TrackTrackerManager.isMusicTitle(track.title)) {
                                            ytUrl = "https://www.youtube.com/watch?v=${match.id}"
                                            statusMessage = "Coincidencia de YouTube encontrada."
                                        } else {
                                            pendingYouTubeMatch = match
                                        }
                                    } else {
                                        statusMessage = "No se encontro coincidencia en YouTube."
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Exception) {
                                    statusMessage = "Error al buscar en YouTube: ${error.localizedMessage ?: "error desconocido"}"
                                } finally {
                                    isSearchingYt = false
                                }
                            }
                        },
                        enabled = !isSearchingYt,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSearchingYt) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando en YouTube...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buscar coincidencia automática")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: LRC Tracker — NUEVO DISEÑO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "LRC Tracker",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        val hasLrcLinked = isLrcSynced || lrcPath.isNotBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (hasLrcLinked) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (hasLrcLinked) "Letra vinculada" else "Sin letra",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasLrcLinked) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Boton unico: buscar + descargar + vincular atomicamente
                    Button(
                        onClick = {
                            scope.launch {
                                isSearchingLrc = true
                                statusMessage = null
                                try {
                                    val lyrics = onAutoSearchLrc()
                                    if (!lyrics.isNullOrBlank()) {
                                        val ok = onDownloadLrc(lyrics)
                                        if (ok) {
                                            isLrcSynced = true
                                            lrcPath = "vinculado"
                                            statusMessage = "Letra descargada y vinculada correctamente."
                                        } else {
                                            statusMessage = "Se encontro letra pero no se pudo guardar el archivo."
                                        }
                                    } else {
                                        statusMessage = "No se encontro letra en LRCLIB para esta pista."
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Exception) {
                                    statusMessage = "Error al buscar letra: ${error.localizedMessage ?: "error desconocido"}"
                                } finally {
                                    isSearchingLrc = false
                                }
                            }
                        },
                        enabled = !isSearchingLrc,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSearchingLrc) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscando y descargando letra...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val hasLrc = isLrcSynced || lrcPath.isNotBlank()
                            Text(if (hasLrc) "Actualizar letra de LRCLIB" else "Descargar letra de LRCLIB")
                        }
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        onUnlink()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Desvincular")
                }

                Button(
                    onClick = {
                        onSave(
                            ytUrl.trim().takeIf(String::isNotBlank),
                            lrcPath.trim().takeIf(String::isNotBlank),
                            isLrcSynced
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    pendingYouTubeMatch?.let { match ->
        AlertDialog(
            onDismissRequest = {
                pendingYouTubeMatch = null
                statusMessage = "Vínculo de YouTube descartado."
            },
            title = { Text("Confirma el vínculo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AsyncImage(
                        model = "https://img.youtube.com/vi/${match.id}/mqdefault.jpg",
                        contentDescription = "Miniatura de ${match.title}",
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(match.title, fontWeight = FontWeight.SemiBold)
                    Text(
                        "El título de esta pista parece un identificador. Confirma que el video corresponde a la canción antes de guardar el vínculo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ytUrl = "https://www.youtube.com/watch?v=${match.id}"
                    pendingYouTubeMatch = null
                    statusMessage = "Coincidencia confirmada. Pulsa Guardar para conservarla."
                }) {
                    Text("Aceptar vínculo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingYouTubeMatch = null
                    statusMessage = "Vínculo de YouTube descartado."
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
