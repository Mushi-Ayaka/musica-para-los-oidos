package com.musica.paralosoidos.data.download

data class CleanTrackMetadata(
    val artist: String,
    val title: String
)

object MetadataCleaner {

    private val NOISE_BRACKETS_REGEX = Regex(
        """\s*[\(\[](?:official\s*(?:music\s*)?video|video\s*oficial|audio\s*oficial|lyrics?|letra|video\s*clip|visualizer|hd|4k|remaster(?:ed)?(?:\s*\d+)?|en\s*vivo|live|audio|videolyric|lyric\s*video|official\s*audio)[\)\]]""",
        RegexOption.IGNORE_CASE
    )

    private val FEAT_BRACKETS_REGEX = Regex(
        """\s*[\(\[]\s*(?:feat|ft)\.?\s+[^\)\]]+[\)\]]""",
        RegexOption.IGNORE_CASE
    )

    private val CHANNEL_NOISE_REGEX = Regex(
        """\s*-\s*Topic|\s+VEVO|\s+Official|\s+Oficial""",
        RegexOption.IGNORE_CASE
    )

    private val SPLIT_DELIMITERS = listOf(" - ", " – ", " — ")

    fun clean(rawTitle: String, channelName: String): CleanTrackMetadata {
        var cleanTitle = rawTitle.replace(NOISE_BRACKETS_REGEX, "").trim()
        cleanTitle = cleanTitle.replace(FEAT_BRACKETS_REGEX, "").trim()

        var detectedArtist = ""
        var detectedTitle = cleanTitle

        for (delimiter in SPLIT_DELIMITERS) {
            if (cleanTitle.contains(delimiter)) {
                val parts = cleanTitle.split(delimiter, limit = 2)
                if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                    detectedArtist = parts[0].trim()
                    detectedTitle = parts[1].trim()
                    break
                }
            }
        }

        if (detectedArtist.isBlank()) {
            val cleanChannel = channelName.replace(CHANNEL_NOISE_REGEX, "").trim()
            detectedArtist = cleanChannel.ifBlank { "Desconocido" }
        }

        val finalArtist = stripQuotesAndTrim(detectedArtist)
        val finalTitle = stripQuotesAndTrim(detectedTitle)

        return CleanTrackMetadata(
            artist = finalArtist.ifBlank { "Desconocido" },
            title = finalTitle.ifBlank { rawTitle.trim() }
        )
    }

    private fun stripQuotesAndTrim(value: String): String {
        return value.trim().trim('"', '\'', '“', '”', '‘', '’')
    }
}
