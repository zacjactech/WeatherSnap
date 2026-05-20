package com.weather.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.common.DispatcherProvider
import com.weather.core.domain.usecase.GetWeatherSnapsUseCase
import com.weather.core.domain.usecase.SyncWeatherSnapsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWeatherSnapsUseCase: GetWeatherSnapsUseCase,
    private val syncWeatherSnapsUseCase: SyncWeatherSnapsUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getWeatherSnapsUseCase()
        .map { snaps ->
            if (snaps.isEmpty()) {
                HistoryUiState.Empty
            } else {
                HistoryUiState.Success(snaps)
            }
        }
        .catch { e ->
            emit(HistoryUiState.Error(
                e.message ?: "An unexpected database reading error occurred."
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState.Loading
        )

    /**
     * Triggers manual synchronization upload of pending snapshots.
     */
    fun forceSync() {
        viewModelScope.launch(dispatcherProvider.io) {
            try {
                syncWeatherSnapsUseCase()
            } catch (e: Exception) {
                // Handled silently by the synchronizer flow; logging can be integrated
            }
        }
    }
}
