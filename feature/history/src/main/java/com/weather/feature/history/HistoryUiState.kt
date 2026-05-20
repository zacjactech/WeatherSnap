package com.weather.feature.history

import com.weather.core.common.UiState
import com.weather.core.model.WeatherSnap

sealed interface HistoryUiState : UiState {
    object Loading : HistoryUiState
    
    data class Success(
        val snaps: List<WeatherSnap>
    ) : HistoryUiState
    
    object Empty : HistoryUiState
    
    data class Error(
        val message: String
    ) : HistoryUiState
}
