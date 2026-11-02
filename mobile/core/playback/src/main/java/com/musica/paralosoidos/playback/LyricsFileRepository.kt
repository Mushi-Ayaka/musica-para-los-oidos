package com.musica.paralosoidos.playback

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

data class LyricsSearchResult(
    val sourceName: String?,
    val document: LyricsDocument?,
    val logs: List<String>
)

object LyricsFileRepository {
    private const val MIN_SCORE = 0.5

    fun findMatchingLyrics(
        context: Context,
        audioFileName: String,
        trackTitle: String,
        trackArtist: String = ""
    ): LyricsSearchResult {
        val logs = mutableListOf<String>()
        val audioLabel = audioFileName.ifBlank { trackTitle }
        logs += "Buscando letras para \"$audioLabel\"."

        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        val selection = "LOWER(${MediaStore.MediaColumns.DISPLAY_NAME}) LIKE ?"
        val args = arrayOf("%.lrc")

        val candidates = runCatching {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                args,
                "${MediaStore.MediaColumns.DISPLAY_NAME} COLLATE NOCASE ASC"
            )?.use { cursor ->
                buildList {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn) ?: continue
                        val score = LyricsMatcher.score(
                            audioFileName = audioFileName,
                            trackTitle = trackTitle,
                            trackArtist = trackArtist,
                            candidateName = name
                        )
                        add(
                            Candidate(
                                uri = ContentUris.withAppendedId(collection, id),
                                name = name,
                                score = score
                            )
                        )
                    }
                }
            } ?: emptyList()
        }.getOrElse { error ->
            logs += "No se pudo consultar MediaStore: ${error.localizedMessage ?: "error desconocido"}."
            return LyricsSearchResult(null, null, logs)
        }

        if (candidates.isEmpty()) {
            logs += "MediaStore no tiene archivos .lrc indexados."
            return LyricsSearchResult(null, null, logs)
        }

        val ranked = candidates
            .filter { it.score >= MIN_SCORE }
            .sortedByDescending(Candidate::score)
        if (ranked.isEmpty()) {
            logs += "No hay coincidencias suficientemente parecidas (umbral ${MIN_SCORE})."
            logs += "Candidatos encontrados: ${candidates.size}."
            candidates.sortedByDescending(Candidate::score).take(5).forEach {
                logs += "· ${it.name} (${"%.2f".format(it.score)})"
            }
            return LyricsSearchResult(null, null, logs)
        }

        ranked.drop(1).take(3).forEach {
            logs += "Alternativa: ${it.name} (${"%.2f".format(it.score)})."
        }

        for (candidate in ranked.take(8)) {
            val text = runCatching {
                context.contentResolver.openInputStream(candidate.uri)
                    ?.bufferedReader(Charsets.UTF_8)
                    ?.use { it.readText() }
            }.getOrNull()
            if (text.isNullOrBlank()) {
                logs += "No se pudo leer ${candidate.name}."
                continue
            }

            val document = LrcLyrics.parseDocument(text)
            if (document.lines.isEmpty()) {
                logs += "${candidate.name} no contiene líneas de letra válidas."
                continue
            }
            logs += "Elegido: ${candidate.name} (${"%.2f".format(candidate.score)})."
            logs += if (document.synced) {
                "Letras sincronizadas con ${document.lines.size} líneas."
            } else {
                "Letras sin tiempos; se mostrarán línea a línea."
            }
            return LyricsSearchResult(candidate.name, document, logs)
        }

        logs += "Ninguna coincidencia se pudo leer como LRC."
        return LyricsSearchResult(null, null, logs)
    }

    private data class Candidate(
        val uri: android.net.Uri,
        val name: String,
        val score: Double
    )
}
