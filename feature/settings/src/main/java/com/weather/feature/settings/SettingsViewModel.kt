package com.weather.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.common.DispatcherProvider
import com.weather.feature.settings.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState() // default values
        )

    fun setUseFahrenheit(value: Boolean) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setUseFahrenheit(value)
    }

    fun setWindSpeedUnit(value: WindSpeedUnit) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setWindSpeedUnit(value)
    }

    fun setPressureUnit(value: PressureUnit) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setPressureUnit(value)
    }

    fun setAutoCaptureTelemetry(value: Boolean) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setAutoCaptureTelemetry(value)
    }

    fun setSaveOriginalPhotos(value: Boolean) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setSaveOriginalPhotos(value)
    }

    fun setImageResolution(value: ImageResolution) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setImageResolution(value)
    }

    fun setAutoSyncEnabled(value: Boolean) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setAutoSyncEnabled(value)
    }

    fun setSyncIntervalMinutes(value: Int) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setSyncIntervalMinutes(value)
    }

    fun setGpsPrecisionMode(value: GpsPrecisionMode) = viewModelScope.launch(dispatcherProvider.io) {
        repository.setGpsPrecisionMode(value)
    }
}
