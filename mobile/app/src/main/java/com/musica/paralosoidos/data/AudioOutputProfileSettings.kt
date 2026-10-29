package com.musica.paralosoidos.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.audioOutputProfileDataStore by preferencesDataStore(name = "audio_output_profile")

object AudioOutputProfileIds {
    const val STUDIO_MONITOR = "studio_monitor"
    const val AUDIOPHILE_IEM = "audiophile_iem"
    const val BLUETOOTH = "bluetooth_ldac"
    const val USB_DAC = "usb_dac"

    val all = setOf(STUDIO_MONITOR, AUDIOPHILE_IEM, BLUETOOTH, USB_DAC)
}

class AudioOutputProfileSettings(context: Context) {
    private val dataStore = context.applicationContext.audioOutputProfileDataStore

    val profileId: Flow<String> = dataStore.data.map { preferences ->
        preferences[PROFILE_ID_KEY]
            ?.takeIf { it in AudioOutputProfileIds.all }
            ?: AudioOutputProfileIds.STUDIO_MONITOR
    }

    suspend fun save(profileId: String) {
        require(profileId in AudioOutputProfileIds.all) {
            "El perfil de salida seleccionado no es válido."
        }
        dataStore.edit { preferences ->
            preferences[PROFILE_ID_KEY] = profileId
        }
    }

    suspend fun restore(profileId: String?) {
        save(profileId?.takeIf { it in AudioOutputProfileIds.all } ?: AudioOutputProfileIds.STUDIO_MONITOR)
    }

    private companion object {
        val PROFILE_ID_KEY = stringPreferencesKey("profile_id")
    }
}
