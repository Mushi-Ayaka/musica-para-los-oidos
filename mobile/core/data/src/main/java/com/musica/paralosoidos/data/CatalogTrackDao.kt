package com.musica.paralosoidos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CatalogTrackDao {
    @Query("SELECT * FROM catalog_tracks ORDER BY title COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<CatalogTrackEntity>>

    @Query("DELETE FROM catalog_tracks")
    protected abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAll(tracks: List<CatalogTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(track: CatalogTrackEntity)

    @Transaction
    open suspend fun replaceAll(tracks: List<CatalogTrackEntity>) {
        deleteAll()
        if (tracks.isNotEmpty()) insertAll(tracks)
    }
}
