package com.weather.feature.camera

import com.weather.core.common.UiState

sealed interface CameraUiState : UiState {
    object Ready : CameraUiState
    
    object Capturing : CameraUiState
    
    data class Success(
        val filePath: String,
        val width: Int,
        val height: Int
    ) : CameraUiState
    
    data class Error(
        val exception: Throwable
    ) : CameraUiState
}
