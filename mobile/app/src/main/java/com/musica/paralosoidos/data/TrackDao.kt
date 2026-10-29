package com.musica.paralosoidos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackDao {
    @Query("SELECT * FROM audio_tracks ORDER BY title COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<TrackEntity>>

    @Query("DELETE FROM audio_tracks")
    protected abstract suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAll(tracks: List<TrackEntity>)

    @Transaction
    open suspend fun replaceAll(tracks: List<TrackEntity>) {
        deleteAll()
        if (tracks.isNotEmpty()) insertAll(tracks)
    }
}
