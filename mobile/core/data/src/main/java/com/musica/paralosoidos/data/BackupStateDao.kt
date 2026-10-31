package com.musica.paralosoidos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BackupStateDao {
    @Query("SELECT dataJson FROM app_backup_state WHERE id = 1")
    suspend fun getDataJson(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(state: BackupStateEntity)
}
