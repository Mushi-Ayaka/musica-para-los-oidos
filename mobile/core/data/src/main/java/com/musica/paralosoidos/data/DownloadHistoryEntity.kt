package com.musica.paralosoidos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val title: String,
    val channel: String,
    val format: String,
    val bitrate: Int?,
    val filePath: String,
    val downloadedAt: Long = System.currentTimeMillis()
)
