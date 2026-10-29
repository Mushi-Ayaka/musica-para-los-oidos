package com.musica.paralosoidos.playback

import kotlin.math.ln
import kotlin.math.roundToInt

internal object EqualizerBandMapper {
    val controlFrequenciesHz = listOf(32, 64, 125, 250, 500, 1_000, 2_000, 4_000, 8_000, 16_000)

    fun gainForTouchPosition(y: Float, height: Float): Int {
        if (!height.isFinite() || height <= 0f || !y.isFinite()) return 0
        val normalized = (y / height).coerceIn(0f, 1f)
        return ((0.5f - normalized) * 24f).roundToInt().coerceIn(-12, 12)
    }

    fun mapToHardwareLevels(
        controlFrequenciesHz: List<Int>,
        controlGainsDb: List<Int>,
        hardwareCenterFrequenciesMilliHz: List<Int>,
        minLevelMillibels: Int,
        maxLevelMillibels: Int
    ): List<Short>? {
        if (controlFrequenciesHz.isEmpty() || controlFrequenciesHz.size != controlGainsDb.size) return null
        if (controlFrequenciesHz.any { it <= 0 } || controlFrequenciesHz.zipWithNext().any { it.first >= it.second }) {
            return null
        }
        if (minLevelMillibels > maxLevelMillibels) return null
        if (hardwareCenterFrequenciesMilliHz.any { it <= 0 }) return null
        if (hardwareCenterFrequenciesMilliHz.isEmpty()) return emptyList()

        return hardwareCenterFrequenciesMilliHz.map { centerFrequencyMilliHz ->
            val frequencyHz = centerFrequencyMilliHz / 1_000f
            val gainDb = interpolateLogarithmically(
                frequencyHz,
                controlFrequenciesHz,
                controlGainsDb
            )
            (gainDb * 100f)
                .roundToInt()
                .coerceIn(minLevelMillibels, maxLevelMillibels)
                .toShort()
        }
    }

    private fun interpolateLogarithmically(
        frequencyHz: Float,
        controlFrequenciesHz: List<Int>,
        controlGainsDb: List<Int>
    ): Float {
        if (frequencyHz <= controlFrequenciesHz.first()) return controlGainsDb.first().toFloat()
        if (frequencyHz >= controlFrequenciesHz.last()) return controlGainsDb.last().toFloat()

        val upperIndex = controlFrequenciesHz.indexOfFirst { it >= frequencyHz }
        val lowerIndex = upperIndex - 1
        val lowerFrequency = controlFrequenciesHz[lowerIndex].toFloat()
        val upperFrequency = controlFrequenciesHz[upperIndex].toFloat()
        val fraction = (ln(frequencyHz) - ln(lowerFrequency)) /
            (ln(upperFrequency) - ln(lowerFrequency))
        val lowerGain = controlGainsDb[lowerIndex].toFloat()
        val upperGain = controlGainsDb[upperIndex].toFloat()
        return lowerGain + (upperGain - lowerGain) * fraction
    }
}
