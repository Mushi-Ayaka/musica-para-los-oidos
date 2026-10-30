package com.musica.paralosoidos.ui

import com.musica.paralosoidos.ui.theme.MusicaStyle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
                    shape = MusicaStyle.Shape.row,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(preset.settings.surfaceColor))
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MusicaStyle.Colors.neutralBorder
                    )
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(preset.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            preset.description,
                            color = MusicaStyle.Colors.neutral,
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
                    shape = MusicaStyle.Shape.medium,
                    border = androidx.compose.foundation.BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MusicaStyle.Colors.neutralBorder
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

        SectionTitle("Colores", "Elige un color con el selector interactivo o introduce su código hexadecimal.")
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
    var showCustomPicker by remember { mutableStateOf(false) }

    val parsedColor = remember(color) {
        try {
            Color(android.graphics.Color.parseColor(color))
        } catch (_: Exception) {
            Color.White
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(parsedColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { showCustomPicker = true }
                )
                OutlinedButton(
                    onClick = { showCustomPicker = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Personalizar color", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Selector", fontSize = 11.sp)
                }
            }
        }

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
            label = { Text("Hexadecimal") }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            COLOR_SWATCHES.forEach { swatch ->
                val selected = color.equals(swatch, ignoreCase = true)
                Box(
                    Modifier
                        .size(28.dp)
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

    if (showCustomPicker) {
        CustomColorPickerDialog(
            title = "Personalizar: $label",
            initialColorHex = color,
            onDismiss = { showCustomPicker = false },
            onColorSelected = { selectedHex ->
                value = selectedHex
                onColorChange(selectedHex)
                showCustomPicker = false
            }
        )
    }
}

@Composable
private fun CustomColorPickerDialog(
    title: String,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val initialColorInt = remember(initialColorHex) {
        try {
            android.graphics.Color.parseColor(initialColorHex)
        } catch (_: Exception) {
            android.graphics.Color.WHITE
        }
    }
    val initialHsv = remember(initialColorInt) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColorInt, it) }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var valueBrightness by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentColorInt = remember(hue, saturation, valueBrightness) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, valueBrightness))
    }
    val currentHex = remember(currentColorInt) {
        String.format("#%06X", 0xFFFFFF and currentColorInt)
    }
    var hexInput by remember(currentHex) { mutableStateOf(currentHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Previsualizacion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(MusicaStyle.Shape.small)
                            .background(Color(currentColorInt))
                            .border(1.dp, MusicaStyle.Colors.overlay, MusicaStyle.Shape.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentHex,
                            color = if (valueBrightness > 0.5f && saturation < 0.5f) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tono (Hue)
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tono (Color)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${hue.toInt()}°", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            hexInput = String.format("#%06X", 0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, valueBrightness)))
                        },
                        valueRange = 0f..360f
                    )
                }

                // Saturacion
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saturación", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(saturation * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            hexInput = String.format("#%06X", 0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, valueBrightness)))
                        },
                        valueRange = 0f..1f
                    )
                }

                // Brillo
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Brillo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${(valueBrightness * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = valueBrightness,
                        onValueChange = {
                            valueBrightness = it
                            hexInput = String.format("#%06X", 0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, valueBrightness)))
                        },
                        valueRange = 0f..1f
                    )
                }

                // Entrada Hex manual
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { text ->
                        hexInput = text
                        if (text.matches(Regex("^#(?:[0-9a-fA-F]{6})$"))) {
                            try {
                                val c = android.graphics.Color.parseColor(text)
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(c, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                valueBrightness = hsv[2]
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Código Hexadecimal") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onColorSelected(currentHex) }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
