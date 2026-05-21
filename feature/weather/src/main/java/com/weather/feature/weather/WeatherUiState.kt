package com.weather.feature.weather

import com.weather.core.common.UiState
import com.weather.core.model.UserSettings
import com.weather.core.model.WeatherTelemetry

sealed interface WeatherUiState : UiState {
    object Loading : WeatherUiState
    
    data class Success(
        val telemetry: WeatherTelemetry,
        val userSettings: UserSettings
    ) : WeatherUiState
    
    data class Error(
        val message: String
    ) : WeatherUiState
}
