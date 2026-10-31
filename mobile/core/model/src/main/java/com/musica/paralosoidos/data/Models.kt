package com.musica.paralosoidos.data

import kotlinx.serialization.Serializable

@Serializable
data class ThemeSettingsState(
    val presetId: String = "amoled",
    val backgroundColor: String = "#000000",
    val surfaceColor: String = "#0a0a0c",
    val elevatedColor: String = "#131317",
    val playerBackgroundColor: String = "#0f0f14",
    val playerAccentColor: String = "#06b6d4",
    val primaryAccentColor: String = "#06b6d4",
    val secondaryAccentColor: String = "#8b5cf6",
    val textColor: String = "#ffffff",
    val mutedTextColor: String = "#a1a1aa",
    val cornerRadiusDp: Int = 20,
    val fontFamily: String = "jakarta",
    val wallpaperUri: String = "",
    val blurDp: Int = 16,
    val dimAmount: Float = 0.8f,
    val customPresets: List<SavedThemePreset> = emptyList()
)

@Serializable
data class SavedThemePreset(
    val id: String,
    val name: String,
    val settings: ThemeSettingsState
)

@Serializable
data class SavedEqPreset(
    val id: String,
    val name: String,
    val gainsDb: List<Int>,
    val bassBoostDb: Int,
    val virtualizerPercent: Int
)

@Serializable
data class MusicPlaylist(
    val id: String,
    val name: String,
    val trackIds: List<Long>
)

object AudioOutputProfileIds {
    const val STUDIO_MONITOR = "studio_monitor"
    const val AUDIOPHILE_IEM = "audiophile_iem"
    const val BLUETOOTH = "bluetooth_ldac"
    const val USB_DAC = "usb_dac"

    val all = setOf(STUDIO_MONITOR, AUDIOPHILE_IEM, BLUETOOTH, USB_DAC)
}
