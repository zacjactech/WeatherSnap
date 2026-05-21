package com.weather.feature.camera

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.common.DispatcherProvider
import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.file.CompressionResult
import com.weather.core.file.FileStorageManager
import com.weather.core.model.PhotoMetadata
import com.weather.core.model.WeatherTelemetry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val fileStorageManager: FileStorageManager,
    private val weatherSnapRepository: WeatherSnapRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val draftId: String? = savedStateHandle.get<String>("draftId")

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _telemetry = MutableStateFlow<WeatherTelemetry?>(null)
    val telemetry: StateFlow<WeatherTelemetry?> = _telemetry.asStateFlow()

    private val _lastPhotoPath = MutableStateFlow<String?>(null)
    val lastPhotoPath: StateFlow<String?> = _lastPhotoPath.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                draftId?.let { id ->
                    weatherSnapRepository.getSnapByIdStream(id).collect { snap ->
                        if (snap != null) {
                            _telemetry.value = snap.telemetry
                        }
                    }
                }
            } catch (_: Exception) {
                // Telemetry is optional in camera; silently ignore
            }
        }
    }

    /**
     * Spawns an asynchronous image compression task in the IO pool.
     */
    fun processCapturedPhoto(imageBytes: ByteArray) {
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = CameraUiState.Capturing
            try {
                if (draftId == null) {
                    _uiState.value = CameraUiState.Error(Exception("Missing draft ID"))
                    return@launch
                }

                val result = fileStorageManager.saveAndCompressPhoto(imageBytes)
                when (result) {
                    is CompressionResult.Success -> {
                        val currentDraft = weatherSnapRepository.getSnapByIdStream(draftId).firstOrNull()
                        if (currentDraft != null) {
                            // If there was a previous photo, delete its files
                            currentDraft.photo?.let { oldPhoto ->
                                fileStorageManager.deleteDraftFiles(oldPhoto)
                            }

                            val newPhoto = PhotoMetadata(
                                id = UUID.randomUUID().toString(),
                                filePath = result.compressedFile.absolutePath,
                                originalFilePath = result.originalFile.absolutePath,
                                thumbnailFilePath = result.thumbnailFile.absolutePath,
                                originalSizeBytes = result.originalSizeBytes,
                                compressedSizeBytes = result.compressedSizeBytes,
                                width = result.width,
                                height = result.height,
                                capturedAt = System.currentTimeMillis()
                            )

                            val updatedDraft = currentDraft.copy(photo = newPhoto)
                            weatherSnapRepository.saveSnapDraft(updatedDraft)

                            _lastPhotoPath.value = result.compressedFile.absolutePath
                            _uiState.value = CameraUiState.Success(
                                filePath = result.compressedFile.absolutePath,
                                width = result.width,
                                height = result.height
                            )
                        } else {
                            _uiState.value = CameraUiState.Error(Exception("Draft not found"))
                        }
                    }
                    is CompressionResult.CompressionFailure -> {
                        _uiState.value = CameraUiState.Error(result.cause ?: Exception(result.message))
                    }
                    is CompressionResult.Timeout -> {
                        _uiState.value = CameraUiState.Error(Exception("Compression timed out. Try a smaller image."))
                    }
                    is CompressionResult.InvalidFile -> {
                        _uiState.value = CameraUiState.Error(Exception("Invalid image captured. Please try again."))
                    }
                    is CompressionResult.PartialFailure -> {
                        _uiState.value = CameraUiState.Error(Exception(result.message))
                    }
                }
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
