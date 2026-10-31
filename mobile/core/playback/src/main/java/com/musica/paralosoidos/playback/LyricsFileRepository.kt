package com.musica.paralosoidos.playback

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

object LyricsFileRepository {
    fun findMatchingLyrics(
        context: Context,
        audioFileName: String,
        trackTitle: String
    ): Pair<String, List<LyricLine>>? {
        val expectedNames = sequenceOf(
            audioFileName.substringBeforeLast('.', audioFileName).trim(),
            trackTitle.trim()
        ).filter(String::isNotBlank)
            .map { "$it.lrc" }
            .distinctBy(String::lowercase)
            .toList()
        if (expectedNames.isEmpty()) return null

        val selection = expectedNames.joinToString(" OR ") {
            "LOWER(${MediaStore.MediaColumns.DISPLAY_NAME}) = ?"
        }
        val args = expectedNames.map(String::lowercase).toTypedArray()
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        val match = context.contentResolver.query(
            collection,
            projection,
            selection,
            args,
            "${MediaStore.MediaColumns.DISPLAY_NAME} COLLATE NOCASE ASC"
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            )
            ContentUris.withAppendedId(collection, id) to name
        } ?: return null

        val text = context.contentResolver.openInputStream(match.first)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: error("No se pudo leer el archivo de letras ${match.second}.")
        val parsed = LrcLyrics.parse(text)
        if (parsed.isEmpty()) {
            error("El archivo ${match.second} no contiene líneas LRC sincronizadas válidas.")
        }
        return match.second to parsed
    }
}
