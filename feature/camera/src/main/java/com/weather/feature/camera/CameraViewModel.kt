package com.weather.feature.camera

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.common.DispatcherProvider
import com.weather.core.file.FileStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val fileStorageManager: FileStorageManager,
    private val dispatcherProvider: DispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /**
     * Spawns an asynchronous image compression task in the IO pool.
     */
    fun processCapturedPhoto(imageBytes: ByteArray) {
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = CameraUiState.Capturing
            try {
                val compressedFile = fileStorageManager.saveAndCompressPhoto(imageBytes)
                _uiState.value = CameraUiState.Success(
                    filePath = compressedFile.absolutePath,
                    width = 1920,
                    height = 1080
                )
            } catch (e: Exception) {
                _uiState.value = CameraUiState.Error(e)
            }
        }
    }

    /**
     * Resets camera session viewfinder bindings.
     */
    fun resetCamera() {
        _uiState.value = CameraUiState.Ready
    }
}
