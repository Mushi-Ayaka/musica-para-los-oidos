package com.musica.paralosoidos.playback

import java.text.Normalizer
import java.util.Locale

object LyricsMatcher {
    private val extension = Regex("""\.(lrc|mp3|m4a|aac|flac|wav|ogg|opus)$""", RegexOption.IGNORE_CASE)
    private val noise = Regex(
        """(?i)\b(mp3|m4a|aac|flac|wav|ogg|opus|128k|160k|192k|256k|320k|private|official|audio|lyrics?|video|hd|hq)\b"""
    )
    private val nonAlphanumeric = Regex("""[^a-z0-9]+""")
    private val whitespace = Regex("""\s+""")
    private val leadingTrackNumber = Regex("""^\d{1,3}\s+""")

    fun normalize(value: String): String {
        val withoutExtension = extension.replace(value.trim(), "")
        val decomposed = Normalizer.normalize(withoutExtension, Normalizer.Form.NFD)
        val withoutAccents = decomposed.replace(Regex("""\p{Mn}+"""), "")
        return withoutAccents
            .lowercase(Locale.ROOT)
            .replace(nonAlphanumeric, " ")
            .replace(noise, " ")
            .replace(whitespace, " ")
            .trim()
            .replace(leadingTrackNumber, "")
            .trim()
    }

    fun score(
        audioFileName: String,
        trackTitle: String,
        trackArtist: String,
        candidateName: String
    ): Double {
        val candidate = normalize(candidateName)
        if (candidate.isBlank()) return 0.0

        val queries = buildList {
            add(normalize(audioFileName))
            add(normalize(trackTitle))
            if (trackArtist.isNotBlank()) {
                add(normalize("$trackArtist $trackTitle"))
                add(normalize("$trackTitle $trackArtist"))
            }
        }.filter(String::isNotBlank).distinct()

        if (queries.isEmpty()) return 0.0
        return queries.maxOf { query -> scoreAgainst(query, candidate) }
    }

    private fun scoreAgainst(query: String, candidate: String): Double {
        if (query == candidate) return 1.0
        val queryTokens = query.split(' ').filter(String::isNotBlank).toSet()
        val candidateTokens = candidate.split(' ').filter(String::isNotBlank).toSet()
        if (queryTokens.isEmpty() || candidateTokens.isEmpty()) return 0.0

        val common = queryTokens.intersect(candidateTokens).size.toDouble()
        val queryCoverage = common / queryTokens.size
        val candidatePrecision = common / candidateTokens.size
        val tokenScore = (queryCoverage * 0.7) + (candidatePrecision * 0.3)

        val containment = when {
            candidate.contains(query) || query.contains(candidate) -> {
                val shorter = minOf(query.length, candidate.length).toDouble()
                val longer = maxOf(query.length, candidate.length).toDouble()
                0.82 + (shorter / longer) * 0.12
            }
            else -> 0.0
        }
        return maxOf(tokenScore, containment).coerceIn(0.0, 1.0)
    }
}
