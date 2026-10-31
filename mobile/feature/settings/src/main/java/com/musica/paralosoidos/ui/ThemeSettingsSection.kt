package com.musica.paralosoidos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.musica.paralosoidos.data.ThemeSettingsState
import kotlinx.coroutines.delay

@Composable
fun ThemeSettingsSection(
    savedSettings: ThemeSettingsState,
    onSave: (ThemeSettingsState) -> Unit
) {
    var draft by remember(savedSettings) { mutableStateOf(savedSettings) }
    LaunchedEffect(draft) {
        if (draft != savedSettings) {
            delay(300)
            onSave(draft)
        }
    }
    ThemeSettingsScreen(settings = draft, onSettingsChange = { draft = it })
}
