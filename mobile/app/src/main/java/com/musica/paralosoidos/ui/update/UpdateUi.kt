package com.musica.paralosoidos.ui.update

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun UpdateCircleButton(
    viewModel: UpdateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isChecking by viewModel.isChecking.collectAsStateWithLifecycle()

    val hasUpdate = uiState is UpdateUiState.Available ||
        uiState is UpdateUiState.Downloading ||
        uiState is UpdateUiState.ReadyToInstall

    IconButton(
        onClick = {
            if (hasUpdate) {
                viewModel.openDialog()
            } else {
                viewModel.checkForUpdates(force = true) { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        },
        modifier = modifier
    ) {
        if (isChecking) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        } else if (hasUpdate) {
            // Círculo blanco que resalta sobre fondo oscuro o claro
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.35f)), CircleShape)
            )
        } else {
            // Círculo oscuro/vacío (outline)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(
                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun UpdateDialog(
    viewModel: UpdateViewModel
) {
    val showDialog by viewModel.showDialog.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!showDialog) return

    AlertDialog(
        onDismissRequest = { viewModel.closeDialog() },
        title = {
            Text(
                when (uiState) {
                    is UpdateUiState.ReadyToInstall -> "Actualización lista"
                    is UpdateUiState.Downloading -> "Descargando actualización"
                    is UpdateUiState.Error -> "Actualización"
                    else -> "Actualización disponible"
                }
            )
        },
        text = {
            when (val state = uiState) {
                is UpdateUiState.Available -> {
                    Text(
                        "Hay una nueva versión disponible: v${state.release.versionName}" +
                            formatSize(state.release.sizeBytes) +
                            ".\n\n¿Deseas descargarla y actualizar?"
                    )
                }
                is UpdateUiState.Downloading -> {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "Descargando v${state.release.versionName} (${(state.progress * 100).toInt()}%)..."
                        )
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is UpdateUiState.ReadyToInstall -> {
                    Text(
                        "La versión v${state.release.versionName} se ha descargado correctamente.\n\nPulsa 'Instalar' para proceder con la actualización."
                    )
                }
                is UpdateUiState.Error -> {
                    Text(state.message)
                }
                else -> {
                    Text("Buscando actualizaciones...")
                }
            }
        },
        confirmButton = {
            when (uiState) {
                is UpdateUiState.Available -> {
                    Button(onClick = { viewModel.download() }) {
                        Text("Descargar y actualizar")
                    }
                }
                is UpdateUiState.ReadyToInstall -> {
                    Button(onClick = { viewModel.install() }) {
                        Text("Instalar")
                    }
                }
                is UpdateUiState.Error -> {
                    Button(onClick = { viewModel.download() }) {
                        Text("Reintentar")
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeDialog() }) {
                Text(if (uiState is UpdateUiState.Available) "Ahora no" else "Cerrar")
            }
        }
    )
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return ""
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return " (~%.1f MB)".format(mb)
}
