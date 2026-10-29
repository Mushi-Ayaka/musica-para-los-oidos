package com.musica.paralosoidos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_tracks")
data class CatalogTrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val youtubeUrl: String?,
    val localUri: String?,
    val metadataJson: String
)
