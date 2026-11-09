package com.musica.paralosoidos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadHistoryEntity)

    @Query("SELECT * FROM download_history ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadHistoryEntity>>

    @Query("DELETE FROM download_history")
    suspend fun clearAll()
}
