package com.musica.paralosoidos.data

import kotlinx.serialization.Serializable

@Serializable
enum class AudioOutputFormat(val key: String, val label: String) {
    BEST("best", "Mejor calidad disponible (formato original)"),
    MP3("mp3", "MP3")
}

@Serializable
enum class DownloadTaskStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    ERROR
}

@Serializable
data class DownloadTask(
    val id: String,
    val videoId: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val format: AudioOutputFormat,
    val bitrate: Int?,
    val syncLyrics: Boolean,
    val status: DownloadTaskStatus = DownloadTaskStatus.QUEUED,
    val progress: Int = 0,
    val speed: String = "",
    val errorMessage: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

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

@Serializable
data class FolderItem(
    val path: String,
    val name: String,
    val trackCount: Int,
    val isHidden: Boolean
)

@Serializable
data class DownloadSearchResult(
    val id: String,
    val title: String,
    val channel: String,
    val durationSeconds: Int,
    val thumbnailUrl: String,
    val viewCount: Long = 0,
    val uploadDate: String = ""
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val formattedViews: String
        get() = when {
            viewCount >= 1_000_000 -> "%.1fM vistas".format(viewCount / 1_000_000.0)
            viewCount >= 1_000 -> "%.1fK vistas".format(viewCount / 1_000.0)
            viewCount > 0 -> "$viewCount vistas"
            else -> ""
        }
}
