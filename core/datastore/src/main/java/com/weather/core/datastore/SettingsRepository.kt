package com.weather.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weather.core.model.GpsPrecisionMode
import com.weather.core.model.ImageResolution
import com.weather.core.model.PressureUnit
import com.weather.core.model.UserSettings
import com.weather.core.model.WindSpeedUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    private object Keys {
        val USE_FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
        val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
        val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val AUTO_CAPTURE_TELEMETRY = booleanPreferencesKey("auto_capture_telemetry")
        val SAVE_ORIGINAL_PHOTOS = booleanPreferencesKey("save_original_photos")
        val IMAGE_RESOLUTION = stringPreferencesKey("image_resolution")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
        val SYNC_INTERVAL = intPreferencesKey("sync_interval")
        val GPS_PRECISION_MODE = stringPreferencesKey("gps_precision_mode")
    }

    val settingsFlow: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            useFahrenheit = preferences[Keys.USE_FAHRENHEIT] ?: false,
            windSpeedUnit = preferences[Keys.WIND_SPEED_UNIT]?.let { enumValueOf<WindSpeedUnit>(it) } ?: WindSpeedUnit.KMH,
            pressureUnit = preferences[Keys.PRESSURE_UNIT]?.let { enumValueOf<PressureUnit>(it) } ?: PressureUnit.HPA,
            autoCaptureTelemetry = preferences[Keys.AUTO_CAPTURE_TELEMETRY] ?: true,
            saveOriginalPhotos = preferences[Keys.SAVE_ORIGINAL_PHOTOS] ?: true,
            imageResolution = preferences[Keys.IMAGE_RESOLUTION]?.let { enumValueOf<ImageResolution>(it) } ?: ImageResolution.STANDARD,
            autoSyncEnabled = preferences[Keys.AUTO_SYNC] ?: true,
            syncIntervalMinutes = preferences[Keys.SYNC_INTERVAL] ?: 15,
            gpsPrecisionMode = preferences[Keys.GPS_PRECISION_MODE]?.let { enumValueOf<GpsPrecisionMode>(it) } ?: GpsPrecisionMode.STANDARD
        )
    }

    suspend fun setUseFahrenheit(value: Boolean) {
        dataStore.edit { it[Keys.USE_FAHRENHEIT] = value }
    }

    suspend fun setWindSpeedUnit(value: WindSpeedUnit) {
        dataStore.edit { it[Keys.WIND_SPEED_UNIT] = value.name }
    }

    suspend fun setPressureUnit(value: PressureUnit) {
        dataStore.edit { it[Keys.PRESSURE_UNIT] = value.name }
    }

    suspend fun setAutoCaptureTelemetry(value: Boolean) {
        dataStore.edit { it[Keys.AUTO_CAPTURE_TELEMETRY] = value }
    }

    suspend fun setSaveOriginalPhotos(value: Boolean) {
        dataStore.edit { it[Keys.SAVE_ORIGINAL_PHOTOS] = value }
    }

    suspend fun setImageResolution(value: ImageResolution) {
        dataStore.edit { it[Keys.IMAGE_RESOLUTION] = value.name }
    }

    suspend fun setAutoSyncEnabled(value: Boolean) {
        dataStore.edit { it[Keys.AUTO_SYNC] = value }
    }

    suspend fun setSyncIntervalMinutes(value: Int) {
        dataStore.edit { it[Keys.SYNC_INTERVAL] = value }
    }

    suspend fun setGpsPrecisionMode(value: GpsPrecisionMode) {
        dataStore.edit { it[Keys.GPS_PRECISION_MODE] = value.name }
    }
}
