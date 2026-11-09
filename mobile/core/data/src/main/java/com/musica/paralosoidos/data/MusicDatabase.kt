package com.musica.paralosoidos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TrackEntity::class, BackupStateEntity::class, CatalogTrackEntity::class, DownloadHistoryEntity::class],
    version = 7,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun backupStateDao(): BackupStateDao
    abstract fun catalogTrackDao(): CatalogTrackDao
    abstract fun downloadHistoryDao(): DownloadHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio_tracks ADD COLUMN artworkUri TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio_tracks ADD COLUMN displayName TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS app_backup_state " +
                        "(id INTEGER NOT NULL PRIMARY KEY, dataJson TEXT NOT NULL)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS catalog_tracks (" +
                        "id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, artist TEXT NOT NULL, " +
                        "album TEXT NOT NULL, durationMs INTEGER NOT NULL, youtubeUrl TEXT, " +
                        "localUri TEXT, metadataJson TEXT NOT NULL)"
                )
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE audio_tracks ADD COLUMN folderPath TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE audio_tracks ADD COLUMN folderName TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `download_history` " +
                    "(`id` TEXT NOT NULL, `videoId` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                    "`channel` TEXT NOT NULL, `format` TEXT NOT NULL, `bitrate` INTEGER, " +
                    "`filePath` TEXT NOT NULL, `downloadedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
                )
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE catalog_tracks ADD COLUMN lrcPath TEXT")
                db.execSQL("ALTER TABLE catalog_tracks ADD COLUMN lrcSynced INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
