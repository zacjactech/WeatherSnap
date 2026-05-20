package com.weather.feature.settings

import com.weather.core.common.UiState

data class SettingsUiState(
    val useFahrenheit: Boolean = false,
    val autoSyncEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 15
) : UiState
