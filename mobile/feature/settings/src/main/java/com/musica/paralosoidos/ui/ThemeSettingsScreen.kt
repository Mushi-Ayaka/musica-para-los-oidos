package com.musica.paralosoidos.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musica.paralosoidos.data.SavedThemePreset
import com.musica.paralosoidos.data.ThemeSettingsState
import com.musica.paralosoidos.ui.design.SectionTitle
import com.musica.paralosoidos.ui.theme.MusicaStyle
import com.musica.paralosoidos.ui.theme.ThemePresetCatalog
import java.util.Locale

private data class FontOption(val id: String, val label: String)

private val FONT_OPTIONS = listOf(
    FontOption("jakarta", "Jakarta"),
    FontOption("system", "Sistema"),
    FontOption("serif", "Serif"),
    FontOption("mono", "Monoespaciada")
)

@Composable
fun ThemeSettingsScreen(
    settings: ThemeSettingsState,
    onSettingsChange: (ThemeSettingsState) -> Unit,
    onPickWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    val customWallpaper = settings.wallpaperUri.isNotBlank() &&
        !settings.wallpaperUri.startsWith("android.resource://")

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Estilo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Personaliza el fondo, los colores y la tipografía. Los cambios se guardan automáticamente.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        SettingsCard(
            title = "Temas",
            subtitle = "Elige una base y ajusta lo que quieras después."
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ThemePresetCatalog.themes, key = { it.id }) { preset ->
                    val selected = settings.presetId == preset.id
                    Card(
                        modifier = Modifier
                            .size(width = 148.dp, height = 94.dp)
                            .clickable {
                                onSettingsChange(
                                    preset.settings.copy(customPresets = settings.customPresets)
                                )
                            },
                        shape = MusicaStyle.Shape.row,
                        colors = CardDefaults.cardColors(
                            containerColor = Color(android.graphics.Color.parseColor(preset.settings.surfaceColor))
                        ),
                        border = BorderStroke(
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
        }

        SettingsCard(
            title = "Fondo",
            subtitle = "Usa una imagen de tu galería o un fondo predefinido."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPickWallpaper,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Elegir de galería", maxLines = 1)
                }
                if (settings.wallpaperUri.isNotBlank()) {
                    OutlinedButton(
                        onClick = onClearWallpaper,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Quitar")
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (customWallpaper) {
                    item(key = "custom-wallpaper") {
                        WallpaperCard(
                            name = "Personalizada",
                            uri = settings.wallpaperUri,
                            selected = true,
                            onClick = {}
                        )
                    }
                }
                items(ThemePresetCatalog.wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperCard(
                        name = wallpaper.name,
                        uri = wallpaper.uri,
                        selected = settings.wallpaperUri == wallpaper.uri,
                        onClick = { onSettingsChange(settings.copy(wallpaperUri = wallpaper.uri)) }
                    )
                }
            }

            SliderSetting(
                title = "Desenfoque",
                value = settings.blurDp.toFloat(),
                valueText = "${settings.blurDp} dp",
                range = 0f..40f,
                onChange = { onSettingsChange(settings.copy(blurDp = it.toInt())) }
            )
            SliderSetting(
                title = "Atenuación",
                value = settings.dimAmount,
                valueText = "${(settings.dimAmount * 100).toInt()}%",
                range = 0.1f..0.95f,
                onChange = { onSettingsChange(settings.copy(dimAmount = it)) }
            )
        }

        SettingsCard(
            title = "Forma y tipografía",
            subtitle = "Ajusta las esquinas y la familia de la interfaz."
        ) {
            SliderSetting(
                title = "Redondeo",
                value = settings.cornerRadiusDp.toFloat(),
                valueText = "${settings.cornerRadiusDp} dp",
                range = 8f..32f,
                onChange = { onSettingsChange(settings.copy(cornerRadiusDp = it.toInt())) }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FONT_OPTIONS, key = { it.id }) { option ->
                    FilterChip(
                        selected = settings.fontFamily == option.id,
                        onClick = { onSettingsChange(settings.copy(fontFamily = option.id)) },
                        label = { Text(option.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        SettingsCard(
            title = "Colores de la interfaz",
            subtitle = "Toca la muestra o edita el código HEX de cada color."
        ) {
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
        }

        SettingsCard(
            title = "Colores del reproductor",
            subtitle = "El fondo y el acento de la pantalla de reproducción."
        ) {
            ColorSettingField("Fondo del reproductor", settings.playerBackgroundColor) {
                onSettingsChange(settings.copy(playerBackgroundColor = it))
            }
            ColorSettingField("Acento del reproductor", settings.playerAccentColor) {
                onSettingsChange(settings.copy(playerAccentColor = it))
            }
        }

        if (settings.customPresets.isNotEmpty()) {
            SettingsCard(
                title = "Tus temas",
                subtitle = "Combinaciones guardadas para volver a usarlas."
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(settings.customPresets, key = { it.id }) { preset ->
                        OutlinedButton(
                            onClick = {
                                onSettingsChange(
                                    preset.settings.copy(customPresets = settings.customPresets)
                                )
                            }
                        ) {
                            Text(preset.name, maxLines = 1)
                        }
                    }
                }
            }
        }

        SettingsCard(
            title = "Guardar tema",
            subtitle = "Guarda la combinación actual con un nombre."
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Nombre del tema") }
                )
                Button(
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
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title, subtitle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MusicaStyle.Shape.row,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MusicaStyle.Colors.overlay)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun WallpaperCard(
    name: String,
    uri: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 118.dp, height = 82.dp)
            .clickable(onClick = onClick),
        shape = MusicaStyle.Shape.medium,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MusicaStyle.Colors.neutralBorder
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (uri.isNotBlank()) {
                    AsyncImage(
                        model = uri,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "Sin fondo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                name,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
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
        runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(Color.White)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        BasicTextField(
            value = value,
            onValueChange = { updated ->
                value = updated
                if (updated.matches(HEX_PATTERN)) {
                    onColorChange(updated)
                }
            },
            modifier = Modifier
                .width(104.dp)
                .height(40.dp)
                .clip(MusicaStyle.Shape.small)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MusicaStyle.Colors.overlay, MusicaStyle.Shape.small)
                .padding(horizontal = 9.dp, vertical = 11.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
            )
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MusicaStyle.Shape.small)
                .background(parsedColor)
                .border(1.dp, MusicaStyle.Colors.overlay, MusicaStyle.Shape.small)
                .clickable(onClickLabel = "Abrir selector de color") { showCustomPicker = true }
        )
    }

    if (showCustomPicker) {
        CustomColorPickerDialog(
            title = "Color: $label",
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
        runCatching { android.graphics.Color.parseColor(initialColorHex) }
            .getOrDefault(android.graphics.Color.WHITE)
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
        String.format(Locale.US, "#%06X", 0xFFFFFF and currentColorInt)
    }
    var hexInput by remember(currentHex) { mutableStateOf(currentHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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

                ColorSlider(
                    label = "Tono",
                    valueText = "${hue.toInt()}°",
                    value = hue,
                    range = 0f..360f
                ) {
                    hue = it
                    hexInput = hexFor(hue, saturation, valueBrightness)
                }
                ColorSlider(
                    label = "Saturación",
                    valueText = "${(saturation * 100).toInt()}%",
                    value = saturation,
                    range = 0f..1f
                ) {
                    saturation = it
                    hexInput = hexFor(hue, saturation, valueBrightness)
                }
                ColorSlider(
                    label = "Brillo",
                    valueText = "${(valueBrightness * 100).toInt()}%",
                    value = valueBrightness,
                    range = 0f..1f
                ) {
                    valueBrightness = it
                    hexInput = hexFor(hue, saturation, valueBrightness)
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { text ->
                        hexInput = text
                        if (text.matches(HEX_PATTERN)) {
                            runCatching {
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(
                                    android.graphics.Color.parseColor(text),
                                    hsv
                                )
                                hue = hsv[0]
                                saturation = hsv[1]
                                valueBrightness = hsv[2]
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Código HEX") }
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

@Composable
private fun ColorSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valueText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

private fun hexFor(hue: Float, saturation: Float, brightness: Float): String =
    String.format(
        Locale.US,
        "#%06X",
        0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
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

private val HEX_PATTERN = Regex("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")
