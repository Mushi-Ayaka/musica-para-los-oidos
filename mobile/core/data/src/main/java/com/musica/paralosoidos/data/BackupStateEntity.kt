package com.musica.paralosoidos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_backup_state")
data class BackupStateEntity(
    @PrimaryKey val id: Int = 1,
    val dataJson: String
)
