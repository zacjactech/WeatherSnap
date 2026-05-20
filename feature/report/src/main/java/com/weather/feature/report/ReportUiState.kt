package com.weather.feature.report

import com.weather.core.common.UiState
import com.weather.core.model.WeatherSnap

sealed interface ReportUiState : UiState {
    object Idle : ReportUiState
    
    data class Drafting(
        val draft: WeatherSnap,
        val locationName: String? = null
    ) : ReportUiState
    
    object Submitting : ReportUiState
    
    object Success : ReportUiState
    
    data class Error(
        val message: String
    ) : ReportUiState
}
