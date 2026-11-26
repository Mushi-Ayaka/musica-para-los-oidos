package com.musica.paralosoidos.ui.player

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.TrackEntity
import com.musica.paralosoidos.playback.LrcLyrics
import com.musica.paralosoidos.playback.LyricsFileRepository
import com.musica.paralosoidos.playback.LyricsSearchResult
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.Charsets

@Composable
internal fun LyricsPane(
    modifier: Modifier = Modifier,
    track: TrackEntity,
    catalogEntry: CatalogTrackEntity?,
    positionMs: () -> Long,
    scrollSpeed: Float,
    onLinkLrcFromLrclib: suspend (TrackEntity) -> Boolean = { false }
) {
    val context = LocalContext.current
    var result by remember(track.id) { mutableStateOf<LyricsSearchResult?>(null) }
    var loading by remember(track.id) { mutableStateOf(true) }
    var retryToken by remember(track.id) { mutableIntStateOf(0) }
    var isLinkingLrc by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun triggerLrclibLink(notFoundMessage: String) {
        coroutineScope.launch {
            isLinkingLrc = true
            val success = onLinkLrcFromLrclib(track)
            if (success) {
                retryToken++
                Toast.makeText(context, "Letra sincronizada vinculada con éxito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, notFoundMessage, Toast.LENGTH_SHORT).show()
            }
            isLinkingLrc = false
        }
    }

    LaunchedEffect(track.id, retryToken) {
        loading = true
        result = null
        result = withContext(Dispatchers.IO) {
            runCatching {
                val linkedPath = catalogEntry?.lrcPath
                    ?.takeIf { it.isNotBlank() && it != "vinculado" && it != "local" }
                if (linkedPath != null) {
                    val file = File(linkedPath)
                    if (file.exists() && file.length() > 0) {
                        val text = file.readText(Charsets.UTF_8)
                        val document = LrcLyrics.parseDocument(text)
                        if (document.lines.isNotEmpty()) {
                            return@runCatching LyricsSearchResult(
                                sourceName = file.name,
                                document = document,
                                logs = listOf("Letra cargada desde vínculo: ${file.name}.")
                            )
                        }
                    }
                }
                if (track.folderPath.isNotBlank()) {
                    val nameWithoutExt = track.displayName.substringBeforeLast('.')
                    val sidecarFile = File(track.folderPath, "$nameWithoutExt.lrc")
                    if (sidecarFile.exists() && sidecarFile.length() > 0) {
                        val text = sidecarFile.readText(Charsets.UTF_8)
                        val document = LrcLyrics.parseDocument(text)
                        if (document.lines.isNotEmpty()) {
                            return@runCatching LyricsSearchResult(
                                sourceName = sidecarFile.name,
                                document = document,
                                logs = listOf("Letra cargada desde carpeta del audio: ${sidecarFile.name}.")
                            )
                        }
                    }
                }
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { retryToken++ }, enabled = !isLinkingLrc) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reintentar")
                        }
                        LrclibLinkButton(
                            isLinking = isLinkingLrc,
                            compact = false,
                            onClick = { triggerLrclibLink("No se encontró letra sincronizada en LRCLIB") }
                        )
                    }
                }
            } else {
                if (document.synced) {
                    Text(
                        "${result?.sourceName ?: "LRC"} · sincronizada",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${result?.sourceName ?: "LRC"} · sin tiempos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LrclibLinkButton(
                            isLinking = isLinkingLrc,
                            compact = true,
                            onClick = { triggerLrclibLink("No se encontró letra con tiempo en LRCLIB") }
                        )
                    }
                }
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

@Composable
private fun LrclibLinkButton(
    isLinking: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLinking,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        contentPadding = if (compact) PaddingValues(horizontal = 10.dp, vertical = 2.dp) else ButtonDefaults.ContentPadding,
        modifier = if (compact) Modifier.height(30.dp) else Modifier
    ) {
        if (isLinking) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 12.dp else 14.dp),
                strokeWidth = 2.dp,
                color = Color.Black
            )
            Spacer(Modifier.width(if (compact) 4.dp else 6.dp))
            Text(
                if (compact) "Buscando..." else "Buscando en LRCLIB...",
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                Icons.Filled.Download,
                contentDescription = "Vincular desde LRCLIB",
                modifier = Modifier.size(if (compact) 14.dp else 16.dp)
            )
            Spacer(Modifier.width(if (compact) 4.dp else 6.dp))
            Text(
                "Vincular desde LRCLIB",
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
