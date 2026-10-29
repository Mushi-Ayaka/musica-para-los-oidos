package com.musica.paralosoidos.playback

data class LyricLine(
    val timeMs: Long,
    val text: String
)

internal object LrcLyrics {
    private val timestamp = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")

    fun parse(source: String): List<LyricLine> = source.lineSequence()
        .flatMap { line ->
            val matches = timestamp.findAll(line).toList()
            val text = line.replace(timestamp, "").trim()
            if (matches.isEmpty() || text.isEmpty()) {
                emptySequence()
            } else {
                matches.asSequence().mapNotNull { match ->
                    val minutes = match.groupValues[1].toLongOrNull() ?: return@mapNotNull null
                    val seconds = match.groupValues[2].toLongOrNull() ?: return@mapNotNull null
                    val fraction = match.groupValues[3]
                    if (seconds > 59) return@mapNotNull null
                    val millis = when (fraction.length) {
                        0 -> 0L
                        1 -> fraction.toLong() * 100L
                        2 -> fraction.toLong() * 10L
                        else -> fraction.take(3).toLong()
                    }
                    LyricLine((minutes * 60L + seconds) * 1_000L + millis, text)
                }
            }
        }
        .sortedBy(LyricLine::timeMs)
        .toList()

    fun currentLineIndex(lines: List<LyricLine>, positionMs: Long): Int =
        lines.indexOfLast { it.timeMs <= positionMs }
}
