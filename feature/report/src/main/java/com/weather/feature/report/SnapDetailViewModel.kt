package com.weather.feature.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.model.WeatherSnap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Report Detail screen.
 *
 * Loads the [WeatherSnap] by its ID from Room, streaming updates in real-time.
 * The snapId is passed via [SavedStateHandle] from the navigation argument.
 */
@HiltViewModel
class SnapDetailViewModel @Inject constructor(
    private val weatherSnapRepository: WeatherSnapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * The ID of the snap to display, sourced from the navigation argument "snap_id".
     * Will throw [IllegalArgumentException] if the nav arg is missing (programming error).
     */
    private val snapId: String = checkNotNull(savedStateHandle["snap_id"]) {
        "snap_id navigation argument is required for ReportDetailScreen"
    }

    /**
     * Live stream of the snap from Room. Will be null while the first query is in-flight
     * or if the record was deleted.
     */
    val snap: StateFlow<WeatherSnap?> = weatherSnapRepository
        .getSnapByIdStream(snapId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
