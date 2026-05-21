package com.weather.feature.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.common.DispatcherProvider
import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.domain.usecase.GetWeatherTelemetryDraftUseCase
import com.weather.core.domain.usecase.SaveWeatherSnapDraftUseCase
import com.weather.core.file.FileStorageManager
import com.weather.core.model.PhotoMetadata
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val saveWeatherSnapDraftUseCase: SaveWeatherSnapDraftUseCase,
    private val getWeatherTelemetryDraftUseCase: GetWeatherTelemetryDraftUseCase,
    private val weatherSnapRepository: WeatherSnapRepository,
    private val fileStorageManager: FileStorageManager,
    private val dispatcherProvider: DispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var activeDraft: WeatherSnap? = null
    private var locationName: String? = null
    
    val draftId: StateFlow<String?> = savedStateHandle.getStateFlow(DRAFT_ID_KEY, null)

    companion object {
        private const val DRAFT_ID_KEY = "draft_id"
        private const val LOCATION_NAME_KEY = "location_name"
    }

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            val savedId: String? = savedStateHandle[DRAFT_ID_KEY]
            locationName = savedStateHandle[LOCATION_NAME_KEY]
            if (savedId != null) {
                observeDraft(savedId)
            } else {
                val telemetryPair = getWeatherTelemetryDraftUseCase()
                if (telemetryPair != null) {
                    val (telemetry, locName) = telemetryPair
                    locationName = locName
                    savedStateHandle[LOCATION_NAME_KEY] = locName
                    if (telemetry != null) {
                        val draftId = UUID.randomUUID().toString()
                        savedStateHandle[DRAFT_ID_KEY] = draftId
                        
                        val newSnap = WeatherSnap(
                            id = draftId,
                            telemetry = telemetry,
                            photo = null,
                            capturedAt = System.currentTimeMillis(),
                            status = SyncStatus.DRAFT,
                            notes = ""
                        )
                        saveWeatherSnapDraftUseCase(newSnap)
                        observeDraft(draftId)
                    } else {
                        _uiState.value = ReportUiState.Error("No active weather data found. Please select a city first.")
                    }
                } else {
                    _uiState.value = ReportUiState.Error("No active weather data found. Please select a city first.")
                }
            }
        }
    }

    private fun observeDraft(draftId: String) {
        viewModelScope.launch {
            weatherSnapRepository.getSnapByIdStream(draftId).collect { snap ->
                if (snap != null) {
                    val current = activeDraft
                    if (current == null || current.id != snap.id || current.photo != snap.photo || current.telemetry != snap.telemetry) {
                        activeDraft = snap
                        _uiState.value = ReportUiState.Drafting(snap, locationName)
                    } else if (current.notes != snap.notes && _uiState.value !is ReportUiState.Drafting) {
                        activeDraft = snap
                        _uiState.value = ReportUiState.Drafting(snap, locationName)
                    }
                }
            }
        }
    }

        }
    }

    /**
     * Initializes a standard draft model bound to specific captured hardware telemetry indicators.
     * Kept for backwards compatibility if needed, but the init block takes care of this on start.
     */
    fun startNewDraft(
        photoPath: String?,
        lat: Double,
        lon: Double,
        temp: Double,
        condition: String
    ) {
        val weatherCondition = try {
            WeatherCondition.valueOf(condition.uppercase())
        } catch (e: Exception) {
            WeatherCondition.UNKNOWN
        }

        val photo = photoPath?.let {
            PhotoMetadata(
                id = UUID.randomUUID().toString(),
                filePath = it,
                width = 1920,
                height = 1080,
                capturedAt = System.currentTimeMillis()
            )
        }

        val draftId = savedStateHandle.get<String>(DRAFT_ID_KEY) ?: UUID.randomUUID().toString().also {
            savedStateHandle[DRAFT_ID_KEY] = it
        }

        val draft = WeatherSnap(
            id = draftId,
            telemetry = WeatherTelemetry(
                temperatureCelsius = temp,
                condition = weatherCondition,
                humidityPercentage = null,
                windSpeedKph = 0.0,
                latitude = lat,
                longitude = lon
            ),
            photo = photo,
            capturedAt = System.currentTimeMillis(),
            status = SyncStatus.DRAFT,
            notes = ""
        )
        activeDraft = draft
        _uiState.value = ReportUiState.Drafting(draft, locationName)
        viewModelScope.launch(dispatcherProvider.io) {
            saveWeatherSnapDraftUseCase(draft)
        }
    }

    /**
     * Live notes mutation flow updates during typing.
     */
    fun updateNotes(notes: String) {
        val currentDraft = activeDraft ?: return
        val updated = currentDraft.copy(notes = notes)
        activeDraft = updated
        _uiState.value = ReportUiState.Drafting(updated, locationName)
        viewModelScope.launch(dispatcherProvider.io) {
            saveWeatherSnapDraftUseCase(updated)
        }
    }

    /**
     * Commits the current active draft into Room SQLite via [SaveWeatherSnapDraftUseCase].
     * Marks the snap status as COMPLETED (synced/transmitted) and triggers success.
     * All IO work is dispatched off the main thread via [DispatcherProvider.io].
     */
    fun submitSnap() {
        val snap = activeDraft ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = ReportUiState.Submitting
            try {
                val completedSnap = snap.copy(status = SyncStatus.COMPLETED)
                saveWeatherSnapDraftUseCase(completedSnap)
                
                // Cleanup orphaned original raw file to free space
                snap.photo?.originalFilePath?.let { originalPath ->
                    fileStorageManager.deletePhoto(originalPath)
                }
                
                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(
                    e.message ?: "Failed to persist draft report to local storage."
                )
            }
        }
    }

    /**
     * Saves the active draft in local database with DRAFT status and sets state to success.
     */
    fun saveDraft() {
        val snap = activeDraft ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = ReportUiState.Submitting
            try {
                val draftSnap = snap.copy(status = SyncStatus.DRAFT)
                saveWeatherSnapDraftUseCase(draftSnap)
                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(
                    e.message ?: "Failed to save draft to local storage."
                )
            }
        }
    }

    /**
     * Safely discards the draft, marks it as DISCARDED, and deletes all associated files.
     */
    fun discardDraft() {
        val snap = activeDraft ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = ReportUiState.Submitting
            try {
                // Delete photo files safely
                snap.photo?.let {
                    fileStorageManager.deleteDraftFiles(it)
                }
                // Mark draft as discarded
                val discardedSnap = snap.copy(status = SyncStatus.DISCARDED)
                saveWeatherSnapDraftUseCase(discardedSnap)
                // Delete from room
                weatherSnapRepository.deleteSnap(snap.id)
                _uiState.value = ReportUiState.Success
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(
                    e.message ?: "Failed to discard draft."
                )
            }
        }
    }
}
