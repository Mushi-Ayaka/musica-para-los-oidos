package com.musica.paralosoidos.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.musica.paralosoidos.data.AudioOutputProfileIds
import com.musica.paralosoidos.data.SavedEqPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioEffectsStatus(
    val equalizerMessage: String = "El ecualizador estará disponible al iniciar la reproducción.",
    val bassBoostMessage: String = "Bass Boost estará disponible al iniciar la reproducción.",
    val virtualizerMessage: String = "El sonido virtual estará disponible al iniciar la reproducción."
)

data class AudioOutputProfile(
    val id: String,
    val name: String,
    val detail: String,
    val description: String,
    val correctionDb: List<Int>
)

object AudioEffectsManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0
    private val customPresetSettings = mutableMapOf<String, SavedEqPreset>()

    val bandFrequencies = EqualizerBandMapper.controlFrequenciesHz

    private val _bands = MutableStateFlow(List(bandFrequencies.size) { 0 })
    val bands: StateFlow<List<Int>> = _bands.asStateFlow()

    private val _bassStrength = MutableStateFlow(0)
    val bassStrength: StateFlow<Int> = _bassStrength.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _virtualizerAvailable = MutableStateFlow(false)
    val virtualizerAvailable: StateFlow<Boolean> = _virtualizerAvailable.asStateFlow()

    private val _status = MutableStateFlow(AudioEffectsStatus())
    val status: StateFlow<AudioEffectsStatus> = _status.asStateFlow()

    private val _selectedPreset = MutableStateFlow("Plano (Fidelidad de Estudio)")
    val selectedPreset: StateFlow<String> = _selectedPreset.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    val presets = linkedMapOf(
        "Plano (Fidelidad de Estudio)" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Bajos Profundos (Bass Boost)" to listOf(8, 6, 4, 2, 0, 0, 0, 1, 2, 3),
        "Voces Claras y Acústico" to listOf(-2, -1, 1, 3, 4, 4, 3, 2, 1, 0),
        "Electrónica y Club" to listOf(6, 5, 2, 0, -2, 2, 4, 5, 6, 6),
        "Rock y Guitarras" to listOf(5, 3, 2, -1, -2, 1, 3, 5, 4, 4),
        "Pop Brillante" to listOf(2, 1, 0, 2, 4, 3, 2, 3, 4, 5),
        "Cálido y Relajado" to listOf(4, 4, 2, 1, 0, 0, -1, -2, -3, -4),
        "Espacial 3D Envolvente" to listOf(3, 2, 0, -1, 1, 3, 4, 5, 6, 6)
    )

    val outputProfiles = listOf(
        AudioOutputProfile(
            id = AudioOutputProfileIds.STUDIO_MONITOR,
            name = "Audífonos de Estudio (Monitor Hi-Fi)",
            detail = "Curva plana",
            description = "Sin realce ni compensación tonal adicional.",
            correctionDb = List(bandFrequencies.size) { 0 }
        ),
        AudioOutputProfile(
            id = AudioOutputProfileIds.AUDIOPHILE_IEM,
            name = "Monitores In-Ear (IEM de Precisión)",
            detail = "Corrección tonal IEM",
            description = "Realce suave de sub-bajos y atenuación alrededor de 2 kHz.",
            correctionDb = listOf(2, 2, 1, 0, 0, 0, -1, 0, 0, 0)
        ),
        AudioOutputProfile(
            id = AudioOutputProfileIds.BLUETOOTH,
            name = "Bluetooth Hi-Res (LDAC / aptX HD)",
            detail = "Compensación tonal",
            description = "Realce suave de agudos. El códec y la calidad los gestiona Android.",
            correctionDb = listOf(0, 0, 0, 0, 0, 0, 0, 0, 2, 3)
        ),
        AudioOutputProfile(
            id = AudioOutputProfileIds.USB_DAC,
            name = "Salida de Línea / DAC Externo",
            detail = "Curva plana para DAC",
            description = "Sin compensación adicional; Android administra la salida y el muestreo.",
            correctionDb = List(bandFrequencies.size) { 0 }
        )
    )

    private val _selectedOutputProfile = MutableStateFlow(outputProfiles.first().id)
    val selectedOutputProfile: StateFlow<String> = _selectedOutputProfile.asStateFlow()

    fun attachSession(audioSessionId: Int) {
        if (audioSessionId == 0) {
            _status.value = AudioEffectsStatus(
                equalizerMessage = "El reproductor no proporcionó una sesión de audio válida.",
                bassBoostMessage = "El reproductor no proporcionó una sesión de audio válida.",
                virtualizerMessage = "El reproductor no proporcionó una sesión de audio válida."
            )
            return
        }
        if (audioSessionId == currentSessionId && (equalizer != null || bassBoost != null)) return

        releaseEffects()
        currentSessionId = audioSessionId
        val equalizerMessage = try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _isEnabled.value
            }
            val count = equalizer?.numberOfBands?.toInt() ?: 0
            if (count > 0) {
                "$count bandas del dispositivo conectadas a ${bandFrequencies.size} controles."
            } else {
                equalizer?.release()
                equalizer = null
                "Este dispositivo no ofrece bandas de ecualización disponibles."
            }
        } catch (error: RuntimeException) {
            equalizer?.releaseQuietly()
            equalizer = null
            Log.e(TAG, "No se pudo iniciar el ecualizador.", error)
            "Ecualizador no disponible: ${error.message ?: error.javaClass.simpleName}"
        }

        val bassBoostMessage = try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _isEnabled.value
                if (strengthSupported) {
                    setStrength(bassStrengthToEffect(_bassStrength.value))
                }
            }
            if (bassBoost?.strengthSupported == true) {
                "Bass Boost conectado."
            } else {
                bassBoost?.release()
                bassBoost = null
                "Bass Boost no está soportado por este dispositivo."
            }
        } catch (error: RuntimeException) {
            bassBoost?.releaseQuietly()
            bassBoost = null
            Log.e(TAG, "No se pudo iniciar Bass Boost.", error)
            "Bass Boost no disponible: ${error.message ?: error.javaClass.simpleName}"
        }
        val virtualizerMessage = try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _isEnabled.value && _virtualizerStrength.value > 0
                if (strengthSupported) {
                    setStrength((_virtualizerStrength.value * 10).toShort())
                }
            }
            if (virtualizer?.strengthSupported == true) {
                _virtualizerAvailable.value = true
                "Efecto virtual conectado."
            } else {
                virtualizer?.release()
                virtualizer = null
                _virtualizerAvailable.value = false
                "El dispositivo no admite sonido virtual en esta sesión."
            }
        } catch (error: RuntimeException) {
            virtualizer?.releaseQuietly()
            virtualizer = null
            _virtualizerAvailable.value = false
            Log.e(TAG, "No se pudo iniciar el efecto virtual.", error)
            "Sonido virtual no disponible: ${error.message ?: error.javaClass.simpleName}"
        }
        _status.value = AudioEffectsStatus(equalizerMessage, bassBoostMessage, virtualizerMessage)
        applyBands()
    }

    fun setBandGain(bandIdx: Int, dbGain: Int) {
        val current = _bands.value.toMutableList()
        if (bandIdx in current.indices) {
            current[bandIdx] = dbGain.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)
            _bands.value = current
            _selectedPreset.value = "Personalizado"
            applyBands()
        }
    }

    fun saveCustomPreset(presetName: String): SavedEqPreset? {
        if (presetName.isBlank()) return null
        val saved = SavedEqPreset(
            id = "eq-${System.currentTimeMillis()}",
            name = presetName,
            gainsDb = _bands.value.toList(),
            bassBoostDb = _bassStrength.value,
            virtualizerPercent = _virtualizerStrength.value
        )
        registerCustomPreset(saved)
        _selectedPreset.value = presetName
        return saved
    }

    fun registerCustomPreset(preset: SavedEqPreset) {
        if (preset.gainsDb.size != bandFrequencies.size ||
            preset.gainsDb.any { it !in MIN_GAIN_DB..MAX_GAIN_DB } ||
            preset.bassBoostDb !in 0..MAX_BASS_BOOST_DB ||
            preset.virtualizerPercent !in 0..100 ||
            preset.name.isBlank()
        ) {
            Log.e(TAG, "Se ignoró el preset EQ inválido: ${preset.name}.")
            return
        }
        customPresetSettings[preset.name] = preset
        presets[preset.name] = preset.gainsDb.toList()
    }

    fun applyPreset(presetName: String) {
        val target = presets[presetName] ?: return
        if (target.size != bandFrequencies.size) {
            Log.e(TAG, "El preset $presetName no tiene ${bandFrequencies.size} bandas.")
            return
        }
        _selectedPreset.value = presetName
        _bands.value = target
        val custom = customPresetSettings[presetName]
        _bassStrength.value = custom?.bassBoostDb ?: when (presetName) {
            "Bajos Profundos (Bass Boost)" -> 8
            "Electrónica y Club" -> 6
            "Rock y Guitarras" -> 4
            "Pop Brillante" -> 2
            "Cálido y Relajado" -> 3
            "Espacial 3D Envolvente" -> 4
            else -> 0
        }
        setVirtualizer(custom?.virtualizerPercent ?: if (presetName == "Espacial 3D Envolvente") 60 else 0)
        applyBassStrength()
        applyBands()
    }

    fun setBassBoost(level: Int) {
        _bassStrength.value = level.coerceIn(0, MAX_BASS_BOOST_DB)
        applyBassStrength()
    }

    fun setVirtualizer(level: Int) {
        _virtualizerStrength.value = level.coerceIn(0, 100)
        virtualizer?.let { effect ->
            try {
                if (!effect.strengthSupported) {
                    _virtualizerAvailable.value = false
                    _status.value = _status.value.copy(
                        virtualizerMessage = "El dispositivo no admite sonido virtual en esta sesión."
                    )
                    return
                }
                effect.setStrength((_virtualizerStrength.value * 10).toShort())
                effect.enabled = _isEnabled.value && _virtualizerStrength.value > 0
                _status.value = _status.value.copy(
                    virtualizerMessage = if (_virtualizerStrength.value == 0) {
                        "Sonido virtual desactivado."
                    } else {
                        "Efecto virtual aplicado."
                    }
                )
            } catch (error: RuntimeException) {
                reportVirtualizerError(error)
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        equalizer?.let { effect ->
            try {
                effect.enabled = enabled
                _status.value = _status.value.copy(
                    equalizerMessage = if (enabled) "Ecualizador activo." else "Ecualizador desactivado."
                )
            } catch (error: RuntimeException) {
                reportEqualizerError(error)
            }
        }
        bassBoost?.let { effect ->
            try {
                effect.enabled = enabled
                _status.value = _status.value.copy(
                    bassBoostMessage = if (enabled) "Bass Boost activo." else "Bass Boost desactivado."
                )
            } catch (error: RuntimeException) {
                reportBassBoostError(error)
            }
        }
        virtualizer?.let { effect ->
            try {
                effect.enabled = enabled && _virtualizerStrength.value > 0
                _status.value = _status.value.copy(
                    virtualizerMessage = if (enabled && _virtualizerStrength.value > 0) {
                        "Efecto virtual activo."
                    } else {
                        "Sonido virtual desactivado."
                    }
                )
            } catch (error: RuntimeException) {
                reportVirtualizerError(error)
            }
        }
    }

    fun setOutputProfile(profileId: String) {
        if (outputProfiles.none { it.id == profileId }) return
        _selectedOutputProfile.value = profileId
        applyBands()
    }

    private fun applyBassStrength() {
        bassBoost?.let { effect ->
            try {
                if (!effect.strengthSupported) {
                    _status.value = _status.value.copy(
                        bassBoostMessage = "Bass Boost no está soportado por este dispositivo."
                    )
                    return
                }
                effect.setStrength(bassStrengthToEffect(_bassStrength.value))
                _status.value = _status.value.copy(bassBoostMessage = "Bass Boost aplicado.")
            } catch (error: RuntimeException) {
                reportBassBoostError(error)
            }
        }
    }

    private fun applyBands() {
        val eq = equalizer ?: return
        try {
            val range = eq.bandLevelRange
            if (range.size < 2) {
                _status.value = _status.value.copy(
                    equalizerMessage = "El dispositivo no informó un rango de ganancia válido."
                )
                return
            }
            val centerFrequencies = List(eq.numberOfBands.toInt()) { band ->
                eq.getCenterFreq(band.toShort()).toInt()
            }
            val profileCorrection = outputProfiles
                .first { it.id == _selectedOutputProfile.value }
                .correctionDb
            val combinedGains = _bands.value.mapIndexed { index, gain ->
                (gain + profileCorrection[index]).coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)
            }
            val mappedLevels = EqualizerBandMapper.mapToHardwareLevels(
                controlFrequenciesHz = bandFrequencies,
                controlGainsDb = combinedGains,
                hardwareCenterFrequenciesMilliHz = centerFrequencies,
                minLevelMillibels = range[0].toInt(),
                maxLevelMillibels = range[1].toInt()
            )
            if (mappedLevels == null) {
                _status.value = _status.value.copy(
                    equalizerMessage = "No se pudieron relacionar los controles con las bandas del dispositivo."
                )
                return
            }
            mappedLevels.forEachIndexed { index, level ->
                eq.setBandLevel(index.toShort(), level)
            }
            _status.value = _status.value.copy(
                equalizerMessage = "Ecualizador aplicado a ${mappedLevels.size} bandas del dispositivo."
            )
        } catch (error: RuntimeException) {
            reportEqualizerError(error)
        }
    }

    fun release() {
        val equalizerError = equalizer.releaseWithError("ecualizador")
        val bassBoostError = bassBoost.releaseWithError("Bass Boost")
        val virtualizerError = virtualizer.releaseWithError("sonido virtual")
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentSessionId = 0
        _virtualizerAvailable.value = false
        if (equalizerError != null || bassBoostError != null || virtualizerError != null) {
            _status.value = AudioEffectsStatus(
                equalizerMessage = equalizerError ?: "Ecualizador liberado.",
                bassBoostMessage = bassBoostError ?: "Bass Boost liberado.",
                virtualizerMessage = virtualizerError ?: "Sonido virtual liberado."
            )
        }
    }

    private fun releaseEffects() {
        equalizer?.releaseWithError("ecualizador")
        bassBoost?.releaseWithError("Bass Boost")
        virtualizer?.releaseWithError("sonido virtual")
        equalizer = null
        bassBoost = null
        virtualizer = null
    }

    private fun reportEqualizerError(error: RuntimeException) {
        Log.e(TAG, "Error al aplicar el ecualizador.", error)
        _status.value = _status.value.copy(
            equalizerMessage = "No se pudo aplicar el ecualizador: ${error.message ?: error.javaClass.simpleName}"
        )
    }

    private fun reportBassBoostError(error: RuntimeException) {
        Log.e(TAG, "Error al aplicar Bass Boost.", error)
        _status.value = _status.value.copy(
            bassBoostMessage = "No se pudo aplicar Bass Boost: ${error.message ?: error.javaClass.simpleName}"
        )
    }

    private fun reportVirtualizerError(error: RuntimeException) {
        Log.e(TAG, "Error al aplicar el sonido virtual.", error)
        _status.value = _status.value.copy(
            virtualizerMessage = "No se pudo aplicar sonido virtual: ${error.message ?: error.javaClass.simpleName}"
        )
    }

    private fun BassBoost.releaseQuietly() {
        try {
            release()
        } catch (error: RuntimeException) {
            Log.w(TAG, "No se pudo liberar Bass Boost.", error)
        }
    }

    private fun Equalizer.releaseQuietly() {
        try {
            release()
        } catch (error: RuntimeException) {
            Log.w(TAG, "No se pudo liberar el ecualizador.", error)
        }
    }

    private fun Virtualizer.releaseQuietly() {
        try {
            release()
        } catch (error: RuntimeException) {
            Log.w(TAG, "No se pudo liberar el sonido virtual.", error)
        }
    }

    private fun Equalizer?.releaseWithError(name: String): String? {
        if (this == null) return null
        return try {
            release()
            null
        } catch (error: RuntimeException) {
            Log.e(TAG, "No se pudo liberar $name.", error)
            "No se pudo liberar $name: ${error.message ?: error.javaClass.simpleName}"
        }
    }

    private fun BassBoost?.releaseWithError(name: String): String? {
        if (this == null) return null
        return try {
            release()
            null
        } catch (error: RuntimeException) {
            Log.e(TAG, "No se pudo liberar $name.", error)
            "No se pudo liberar $name: ${error.message ?: error.javaClass.simpleName}"
        }
    }

    private fun Virtualizer?.releaseWithError(name: String): String? {
        if (this == null) return null
        return try {
            release()
            null
        } catch (error: RuntimeException) {
            Log.e(TAG, "No se pudo liberar $name.", error)
            "No se pudo liberar $name: ${error.message ?: error.javaClass.simpleName}"
        }
    }

    private fun bassStrengthToEffect(level: Int): Short =
        (level.coerceIn(0, MAX_BASS_BOOST_DB) * 1000 / MAX_BASS_BOOST_DB).toShort()

    private const val TAG = "AudioEffectsManager"
    private const val MIN_GAIN_DB = -12
    private const val MAX_GAIN_DB = 12
    private const val MAX_BASS_BOOST_DB = 12
}
