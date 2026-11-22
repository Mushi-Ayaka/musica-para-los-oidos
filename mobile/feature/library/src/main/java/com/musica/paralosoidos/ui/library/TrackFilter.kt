package com.musica.paralosoidos.ui.library

import com.musica.paralosoidos.data.CatalogTrackEntity
import com.musica.paralosoidos.data.TrackEntity
import java.util.Locale

enum class SortOrder(val label: String) {
    AZ("A-Z"),
    ZA("Z-A"),
    Recent("Recientes"),
    MostPlayed("Más escuchadas"),
    Linked("Vinculados"),
    Unlinked("Desvinculados")
}

internal data class IndexedTrack(
    val track: TrackEntity,
    val title: String,
    val artist: String,
    val album: String
)

internal object TrackFilter {
    fun index(tracks: List<TrackEntity>): List<IndexedTrack> = tracks.map {
        IndexedTrack(
            track = it,
            title = it.title.lowercase(Locale.ROOT),
            artist = it.artist.lowercase(Locale.ROOT),
            album = it.album.lowercase(Locale.ROOT)
        )
    }

    fun apply(
        indexed: List<IndexedTrack>,
        query: String,
        order: SortOrder,
        letter: Char?,
        catalogMap: Map<String, CatalogTrackEntity> = emptyMap(),
        hasLocalLrc: (TrackEntity) -> Boolean = { false },
        playCounts: Map<Long, Int> = emptyMap()
    ): List<TrackEntity> {
        var result = indexed
        if (query.isNotBlank()) {
            val q = query.trim().lowercase(Locale.ROOT)
            result = result.filter {
                it.title.contains(q) || it.artist.contains(q) || it.album.contains(q)
            }
        }
        if (letter != null) {
            result = if (letter == '#') {
                result.filter { it.track.title.firstOrNull()?.isLetter() != true }
            } else {
                result.filter { it.track.title.startsWith(letter, ignoreCase = true) }
            }
        }
        val isTrackLinked: (TrackEntity) -> Boolean = { trk ->
            val cat = catalogMap[trk.uri]
            !cat?.youtubeUrl.isNullOrBlank() || (cat?.lrcSynced == true) || !cat?.lrcPath.isNullOrBlank() || hasLocalLrc(trk)
        }
        return when (order) {
            SortOrder.AZ -> result.sortedBy { it.title }
            SortOrder.ZA -> result.sortedByDescending { it.title }
            SortOrder.Recent -> result.sortedWith(
                compareByDescending<IndexedTrack> { maxOf(it.track.dateAdded, it.track.dateModified) }
                    .thenByDescending { it.track.id }
            )
            SortOrder.MostPlayed -> result.sortedWith(
                compareByDescending<IndexedTrack> { playCounts[it.track.id] ?: 0 }
                    .thenBy { it.title }
            )
            SortOrder.Linked -> result.filter { isTrackLinked(it.track) }.sortedBy { it.title }
            SortOrder.Unlinked -> result.filterNot { isTrackLinked(it.track) }.sortedBy { it.title }
        }.map { it.track }
    }
}
