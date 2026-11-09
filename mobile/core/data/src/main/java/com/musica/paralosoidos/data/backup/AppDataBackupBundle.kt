package com.musica.paralosoidos.data.backup

import com.musica.paralosoidos.data.BackupPlaylist
import com.musica.paralosoidos.data.SavedEqPreset
import com.musica.paralosoidos.data.ThemeSettingsState
import kotlinx.serialization.Serializable

@Serializable
data class PlaybackBackupState(
    val queueUris: List<String> = emptyList(),
    val queueTrackTitles: List<String> = emptyList(),
    val currentTrackUri: String? = null,
    val currentTrackTitle: String? = null,
    val currentPositionMs: Long = 0L
)

@Serializable
data class TrackMetadataBackup(
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val uri: String = "",
    val youtubeUrl: String? = null,
    val lrcPath: String? = null,
    val lrcSynced: Boolean = false
)

@Serializable
data class DownloadHistoryItemBackup(
    val id: String,
    val videoId: String,
    val title: String,
    val channel: String,
    val format: String,
    val bitrate: Int?,
    val filePath: String,
    val downloadedAt: Long
)

@Serializable
data class DownloadConfigBackup(
    val defaultFormat: String = "MP3",
    val defaultBitrate: Int? = 320,
    val syncLyrics: Boolean = true,
    val concurrency: Int = 1,
    val retryCount: Int = 1
)

@Serializable
data class AudioEqBackupState(
    val mediaVolume: Float = 100f,
    val speakerBoost: Int = 0,
    val eqEnabled: Boolean = false,
    val activeEqPreset: String = "",
    val customEqPresets: List<SavedEqPreset> = emptyList(),
    val eqBands: List<Int> = emptyList(),
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val outputProfileId: String? = null
)

@Serializable
data class TrackerConfigBackup(
    val circleColorHex: String = "#FFFFFF",
    val frequency: String = "ON_NEW_TRACKS_ONLY",
    val intensity: String = "MEDIUM"
)

@Serializable
data class AppearanceBackupState(
    val themeSettings: ThemeSettingsState? = null,
    val customThemes: List<ThemeSettingsState> = emptyList(),
    val wallpaperBase64: String? = null,
    val lyricsScrollSpeed: Float = 1.0f
)

@Serializable
data class AppDataBackupBundle(
    val app: String = "Musica para los Oidos",
    val schemaVersion: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val playback: PlaybackBackupState? = null,
    val tracks: List<TrackMetadataBackup> = emptyList(),
    val playlists: List<BackupPlaylist> = emptyList(),
    val hiddenFolders: Set<String> = emptySet(),
    val downloadHistory: List<DownloadHistoryItemBackup> = emptyList(),
    val downloadConfig: DownloadConfigBackup? = null,
    val audioEq: AudioEqBackupState? = null,
    val trackerConfig: TrackerConfigBackup? = null,
    val appearance: AppearanceBackupState? = null,
    val autoBackupInterval: String = "DAILY"
)
