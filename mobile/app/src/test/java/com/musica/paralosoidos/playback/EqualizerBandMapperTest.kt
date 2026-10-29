package com.musica.paralosoidos.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EqualizerBandMapperTest {
    @Test
    fun touchPositionMapsTopCenterAndBottomToGainLimits() {
        assertEquals(12, EqualizerBandMapper.gainForTouchPosition(0f, 1f))
        assertEquals(0, EqualizerBandMapper.gainForTouchPosition(0.5f, 1f))
        assertEquals(-12, EqualizerBandMapper.gainForTouchPosition(1f, 1f))
    }

    @Test
    fun touchPositionClampsOutsideTheSlider() {
        assertEquals(12, EqualizerBandMapper.gainForTouchPosition(-0.2f, 1f))
        assertEquals(-12, EqualizerBandMapper.gainForTouchPosition(1.2f, 1f))
    }

    @Test
    fun mapsControlsToHardwareFrequenciesInMillibels() {
        val levels = EqualizerBandMapper.mapToHardwareLevels(
            controlFrequenciesHz = listOf(100, 1_000),
            controlGainsDb = listOf(-6, 6),
            hardwareCenterFrequenciesMilliHz = listOf(100_000, 1_000_000),
            minLevelMillibels = -1_200,
            maxLevelMillibels = 1_200
        )

        assertEquals(listOf(-600, 600), levels?.map { it.toInt() })
    }

    @Test
    fun interpolatesAtTheLogarithmicMidpoint() {
        val levels = EqualizerBandMapper.mapToHardwareLevels(
            controlFrequenciesHz = listOf(100, 1_000),
            controlGainsDb = listOf(-6, 6),
            hardwareCenterFrequenciesMilliHz = listOf(316_228),
            minLevelMillibels = -1_200,
            maxLevelMillibels = 1_200
        )

        assertEquals(listOf(0), levels?.map { it.toInt() })
    }

    @Test
    fun clampsMappedLevelsToTheDeviceRange() {
        val levels = EqualizerBandMapper.mapToHardwareLevels(
            controlFrequenciesHz = listOf(100, 1_000),
            controlGainsDb = listOf(-12, 12),
            hardwareCenterFrequenciesMilliHz = listOf(100_000, 1_000_000),
            minLevelMillibels = -500,
            maxLevelMillibels = 500
        )

        assertEquals(listOf(-500, 500), levels?.map { it.toInt() })
    }

    @Test
    fun rejectsInvalidControlConfigurationAndSupportsNoHardwareBands() {
        assertNull(
            EqualizerBandMapper.mapToHardwareLevels(
                controlFrequenciesHz = listOf(1_000, 100),
                controlGainsDb = listOf(0, 0),
                hardwareCenterFrequenciesMilliHz = listOf(100_000),
                minLevelMillibels = -1_200,
                maxLevelMillibels = 1_200
            )
        )
        assertEquals(
            emptyList<Short>(),
            EqualizerBandMapper.mapToHardwareLevels(
                controlFrequenciesHz = listOf(100, 1_000),
                controlGainsDb = listOf(0, 0),
                hardwareCenterFrequenciesMilliHz = emptyList(),
                minLevelMillibels = -1_200,
                maxLevelMillibels = 1_200
            )
        )
    }
}
