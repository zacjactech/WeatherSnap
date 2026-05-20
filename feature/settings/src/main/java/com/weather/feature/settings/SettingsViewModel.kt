package com.weather.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val autoSyncKey = "auto_sync"
    private val intervalKey = "sync_interval"

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            useFahrenheit = savedStateHandle.get<Boolean>(fahrenheitKey) ?: false,
            autoSyncEnabled = savedStateHandle.get<Boolean>(autoSyncKey) ?: true,
            syncIntervalMinutes = savedStateHandle.get<Int>(intervalKey) ?: 15
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Toggles temperature unit metrics displaying (Felsius vs Celsius).
     */
    fun setUseFahrenheit(value: Boolean) {
        savedStateHandle[fahrenheitKey] = value
        _uiState.update { it.copy(useFahrenheit = value) }
    }

    /**
     * Toggles background task synchronizations schedules.
     */
    fun setAutoSyncEnabled(value: Boolean) {
        savedStateHandle[autoSyncKey] = value
        _uiState.update { it.copy(autoSyncEnabled = value) }
    }

    /**
     * Changes periodic time refresh windows.
     */
    fun setSyncIntervalMinutes(value: Int) {
        savedStateHandle[intervalKey] = value
        _uiState.update { it.copy(syncIntervalMinutes = value) }
    }
}
