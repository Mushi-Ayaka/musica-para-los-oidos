package com.musica.paralosoidos.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.musica.paralosoidos.data.ThemeSettingsState
import kotlinx.coroutines.delay

@Composable
fun ThemeSettingsSection(
    savedSettings: ThemeSettingsState,
    onSave: (ThemeSettingsState) -> Unit
) {
    var draft by remember(savedSettings) { mutableStateOf(savedSettings) }
    val context = LocalContext.current
    val pickWallpaper = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val savedUri = runCatching {
                val file = java.io.File(context.filesDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                android.net.Uri.fromFile(file).toString()
            }.getOrElse {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                uri.toString()
            }
            draft = draft.copy(wallpaperUri = savedUri)
        }
    }

    LaunchedEffect(draft) {
        if (draft != savedSettings) {
            delay(300)
            onSave(draft)
        }
    }

    ThemeSettingsScreen(
        settings = draft,
        onSettingsChange = { draft = it },
        onPickWallpaper = { pickWallpaper.launch(arrayOf("image/*")) },
        onClearWallpaper = { draft = draft.copy(wallpaperUri = "") }
    )
}
