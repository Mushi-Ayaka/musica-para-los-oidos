package com.musica.paralosoidos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.SavedThemePreset
import com.musica.paralosoidos.data.ThemeSettingsState
import com.musica.paralosoidos.ui.theme.ThemePresetCatalog

@Composable
fun ThemeSettingsScreen(
    settings: ThemeSettingsState,
    onSettingsChange: (ThemeSettingsState) -> Unit
) {
    var presetName by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Estilo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Personaliza el tema, la tipografía, el fondo y las esquinas de la aplicación.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        SectionTitle("Temas", "Selecciona un tema predeterminado o guarda el actual.")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ThemePresetCatalog.themes, key = { it.id }) { preset ->
                val selected = settings.presetId == preset.id
                Card(
                    modifier = Modifier
                        .size(width = 148.dp, height = 94.dp)
                        .clickable {
                            onSettingsChange(
                                preset.settings.copy(
                                    customPresets = settings.customPresets
                                )
                            )
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(preset.settings.surfaceColor))
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else Color(0x33888888)
                    )
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(preset.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            preset.description,
                            color = Color(0xFFB8B8C2),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        SectionTitle("Fondo", "Elige un fondo; la atenuación mantiene el texto legible.")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(ThemePresetCatalog.wallpapers, key = { it.id }) { wallpaper ->
                val selected = settings.wallpaperUri == wallpaper.uri
                Card(
                    modifier = Modifier
                        .size(width = 118.dp, height = 82.dp)
                        .clickable { onSettingsChange(settings.copy(wallpaperUri = wallpaper.uri)) },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else Color(0x33888888)
                    ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        if (wallpaper.uri.isNotBlank()) {
                            AsyncImage(
                                model = wallpaper.uri,
                                contentDescription = wallpaper.name,
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }
                        Text(
                            wallpaper.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        SliderSetting(
            title = "Redondeo",
            value = settings.cornerRadiusDp.toFloat(),
            valueText = "${settings.cornerRadiusDp} dp",
            range = 8f..32f,
            onChange = { onSettingsChange(settings.copy(cornerRadiusDp = it.toInt())) }
        )
        SliderSetting(
            title = "Desenfoque",
            value = settings.blurDp.toFloat(),
            valueText = "${settings.blurDp} dp",
            range = 0f..40f,
            onChange = { onSettingsChange(settings.copy(blurDp = it.toInt())) }
        )
        SliderSetting(
            title = "Atenuación del fondo",
            value = settings.dimAmount,
            valueText = "${(settings.dimAmount * 100).toInt()}%",
            range = 0.1f..0.95f,
            onChange = { onSettingsChange(settings.copy(dimAmount = it)) }
        )

        SectionTitle("Tipografía", "Selecciona la familia tipográfica de la interfaz.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "jakarta" to "Jakarta",
                "system" to "Sistema",
                "serif" to "Serif",
                "mono" to "Monospace"
            ).forEach { (id, label) ->
                TextButton(onClick = { onSettingsChange(settings.copy(fontFamily = id)) }) {
                    Text(
                        label,
                        color = if (settings.fontFamily == id) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (settings.fontFamily == id) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        SectionTitle("Colores", "Elige un color de la paleta o introduce su código hexadecimal.")
        ColorSettingField("Fondo", settings.backgroundColor) {
            onSettingsChange(settings.copy(backgroundColor = it))
        }
        ColorSettingField("Superficie", settings.surfaceColor) {
            onSettingsChange(settings.copy(surfaceColor = it))
        }
        ColorSettingField("Superficie elevada", settings.elevatedColor) {
            onSettingsChange(settings.copy(elevatedColor = it))
        }
        ColorSettingField("Acento principal", settings.primaryAccentColor) {
            onSettingsChange(settings.copy(primaryAccentColor = it))
        }
        ColorSettingField("Acento secundario", settings.secondaryAccentColor) {
            onSettingsChange(settings.copy(secondaryAccentColor = it))
        }
        ColorSettingField("Texto", settings.textColor) {
            onSettingsChange(settings.copy(textColor = it))
        }
        ColorSettingField("Texto secundario", settings.mutedTextColor) {
            onSettingsChange(settings.copy(mutedTextColor = it))
        }
        ColorSettingField("Fondo del reproductor", settings.playerBackgroundColor) {
            onSettingsChange(settings.copy(playerBackgroundColor = it))
        }
        ColorSettingField("Acento del reproductor", settings.playerAccentColor) {
            onSettingsChange(settings.copy(playerAccentColor = it))
        }

        if (settings.customPresets.isNotEmpty()) {
            SectionTitle("Tus temas", "Temas personalizados guardados.")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(settings.customPresets, key = { it.id }) { preset ->
                    TextButton(onClick = {
                        onSettingsChange(preset.settings.copy(customPresets = settings.customPresets))
                    }) {
                        Text(preset.name)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = presetName,
                onValueChange = { presetName = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Nombre del tema") }
            )
            TextButton(
                enabled = presetName.isNotBlank(),
                onClick = {
                    val name = presetName.trim()
                    val custom = SavedThemePreset(
                        id = "custom-${System.currentTimeMillis()}",
                        name = name,
                        settings = settings.copy(customPresets = emptyList())
                    )
                    onSettingsChange(
                        settings.copy(customPresets = settings.customPresets + custom)
                    )
                    presetName = ""
                }
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun ColorSettingField(
    label: String,
    color: String,
    onColorChange: (String) -> Unit
) {
    var value by remember(color) { mutableStateOf(color) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = { updated ->
                value = updated
                if (updated.matches(Regex("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$"))) {
                    onColorChange(updated)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(label) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            COLOR_SWATCHES.forEach { swatch ->
                val selected = color.equals(swatch, ignoreCase = true)
                Spacer(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(swatch)))
                        .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        .clickable {
                            value = swatch
                            onColorChange(swatch)
                        }
                )
            }
        }
    }
}

private val COLOR_SWATCHES = listOf(
    "#06B6D4", "#8B5CF6", "#EC4899", "#F97316",
    "#22C55E", "#EAB308", "#F4F1EA", "#111827"
)

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(valueText, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
