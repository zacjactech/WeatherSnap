package com.weather.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.weather.core.common.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fahrenheitKey = "use_fahrenheit"
    private val windSpeedUnitKey = "wind_speed_unit"
    private val pressureUnitKey = "pressure_unit"
    
    private val autoCaptureTelemetryKey = "auto_capture_telemetry"
    private val saveOriginalPhotosKey = "save_original_photos"
    private val imageResolutionKey = "image_resolution"

    private val autoSyncKey = "auto_sync"
    private val intervalKey = "sync_interval"
    private val gpsPrecisionModeKey = "gps_precision_mode"

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            useFahrenheit = savedStateHandle.get<Boolean>(fahrenheitKey) ?: false,
            windSpeedUnit = savedStateHandle.get<String>(windSpeedUnitKey)?.let { WindSpeedUnit.valueOf(it) } ?: WindSpeedUnit.KMH,
            pressureUnit = savedStateHandle.get<String>(pressureUnitKey)?.let { PressureUnit.valueOf(it) } ?: PressureUnit.HPA,
            
            autoCaptureTelemetry = savedStateHandle.get<Boolean>(autoCaptureTelemetryKey) ?: true,
            saveOriginalPhotos = savedStateHandle.get<Boolean>(saveOriginalPhotosKey) ?: true,
            imageResolution = savedStateHandle.get<String>(imageResolutionKey)?.let { ImageResolution.valueOf(it) } ?: ImageResolution.STANDARD,
            
            autoSyncEnabled = savedStateHandle.get<Boolean>(autoSyncKey) ?: true,
            syncIntervalMinutes = savedStateHandle.get<Int>(intervalKey) ?: 15,
            gpsPrecisionMode = savedStateHandle.get<String>(gpsPrecisionModeKey)?.let { GpsPrecisionMode.valueOf(it) } ?: GpsPrecisionMode.STANDARD
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ── Display / Units ──

    fun setUseFahrenheit(value: Boolean) {
        savedStateHandle[fahrenheitKey] = value
        _uiState.update { it.copy(useFahrenheit = value) }
    }
    
    fun setWindSpeedUnit(value: WindSpeedUnit) {
        savedStateHandle[windSpeedUnitKey] = value.name
        _uiState.update { it.copy(windSpeedUnit = value) }
    }
    
    fun setPressureUnit(value: PressureUnit) {
        savedStateHandle[pressureUnitKey] = value.name
        _uiState.update { it.copy(pressureUnit = value) }
    }

    // ── Camera & Media ──
    
    fun setAutoCaptureTelemetry(value: Boolean) {
        savedStateHandle[autoCaptureTelemetryKey] = value
        _uiState.update { it.copy(autoCaptureTelemetry = value) }
    }
    
    fun setSaveOriginalPhotos(value: Boolean) {
        savedStateHandle[saveOriginalPhotosKey] = value
        _uiState.update { it.copy(saveOriginalPhotos = value) }
    }
    
    fun setImageResolution(value: ImageResolution) {
        savedStateHandle[imageResolutionKey] = value.name
        _uiState.update { it.copy(imageResolution = value) }
    }

    // ── Sync & GPS ──

    fun setAutoSyncEnabled(value: Boolean) {
        savedStateHandle[autoSyncKey] = value
        _uiState.update { it.copy(autoSyncEnabled = value) }
    }

    fun setSyncIntervalMinutes(value: Int) {
        savedStateHandle[intervalKey] = value
        _uiState.update { it.copy(syncIntervalMinutes = value) }
    }
    
    fun setGpsPrecisionMode(value: GpsPrecisionMode) {
        savedStateHandle[gpsPrecisionModeKey] = value.name
        _uiState.update { it.copy(gpsPrecisionMode = value) }
    }
}
