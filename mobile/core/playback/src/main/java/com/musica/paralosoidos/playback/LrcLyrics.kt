package com.musica.paralosoidos.playback

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class LyricsDocument(
    val lines: List<LyricLine>,
    val synced: Boolean
)

object LrcLyrics {
    private val timestamp = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
    private val metadataTag = Regex(
        """^\[(ar|ti|al|by|offset|re|ve|length|tool|encoding):.*]$""",
        RegexOption.IGNORE_CASE
    )

    fun parse(source: String): List<LyricLine> = parseDocument(source).lines

    fun parseDocument(source: String): LyricsDocument {
        val syncedLines = mutableListOf<LyricLine>()
        val plainLines = mutableListOf<String>()

        source.removePrefix("\uFEFF").lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || metadataTag.matches(line)) return@forEach

            val matches = timestamp.findAll(line).toList()
            val text = line.replace(timestamp, "").trim()
            if (matches.isEmpty()) {
                if (text.isNotEmpty()) plainLines += text
                return@forEach
            }
            if (text.isEmpty()) return@forEach

            matches.forEach { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: return@forEach
                val seconds = match.groupValues[2].toLongOrNull() ?: return@forEach
                if (seconds > 59) return@forEach
                val fraction = match.groupValues[3]
                val millis = when (fraction.length) {
                    0 -> 0L
                    1 -> fraction.toLong() * 100L
                    2 -> fraction.toLong() * 10L
                    else -> fraction.take(3).toLong()
                }
                syncedLines += LyricLine((minutes * 60L + seconds) * 1_000L + millis, text)
            }
        }

        return if (syncedLines.isNotEmpty()) {
            LyricsDocument(
                lines = syncedLines.sortedBy(LyricLine::timeMs),
                synced = true
            )
        } else {
            LyricsDocument(
                lines = plainLines.mapIndexed { index, text ->
                    LyricLine(index * 1_000L, text)
                },
                synced = false
            )
        }
    }

    fun currentLineIndex(lines: List<LyricLine>, positionMs: Long): Int =
        lines.indexOfLast { it.timeMs <= positionMs }
}
