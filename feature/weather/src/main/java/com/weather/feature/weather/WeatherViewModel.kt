package com.weather.feature.weather

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.core.common.Result
import com.weather.core.domain.usecase.GetWeatherTelemetryUseCase
import com.weather.core.domain.usecase.SaveWeatherDraftUseCase
import com.weather.core.domain.usecase.SearchCitiesUseCase
import com.weather.core.model.LocationSearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherTelemetryUseCase: GetWeatherTelemetryUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase,
    private val saveWeatherDraftUseCase: SaveWeatherDraftUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val latKey = "latitude"
    private val lonKey = "longitude"
    private val queryKey = "search_query"
    private val locationNameKey = "location_name"
    private val selectedTabKey = "selected_tab"

    val currentLatitude = savedStateHandle.getStateFlow(latKey, 47.6062)
    val currentLongitude = savedStateHandle.getStateFlow(lonKey, -122.3321)
    val searchQuery = savedStateHandle.getStateFlow(queryKey, "")
    val locationName = savedStateHandle.getStateFlow(locationNameKey, "")
    val selectedTab = savedStateHandle.getStateFlow(selectedTabKey, 0)

    val searchResults: StateFlow<Result<List<LocationSearchResult>>> = searchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length >= 2) {
                searchCitiesUseCase(query)
            } else {
                kotlinx.coroutines.flow.flowOf(Result.Success(emptyList()))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Result.Success(emptyList())
        )

    private val _currentTelemetry = MutableStateFlow<com.weather.core.model.WeatherTelemetry?>(null)

    /**
     * Combines lat and lon into a single atomic pair before calling the use case.
     * This eliminates the race condition where [currentLongitude].value is snapshotted
     * at subscribe time inside a [flatMapLatest] on [currentLatitude], resulting in
     * API calls with a mismatched lat/lon pair when both are updated simultaneously.
     */
    val uiState: StateFlow<WeatherUiState> = combine(currentLatitude, currentLongitude) { lat, lon ->
            lat to lon
        }
        .distinctUntilChanged()
        .flatMapLatest { (lat, lon) ->
            getWeatherTelemetryUseCase(lat, lon).map { result ->
                when (result) {
                    is Result.Loading -> WeatherUiState.Loading
                    is Result.Success -> {
                        _currentTelemetry.value = result.data
                        WeatherUiState.Success(result.data)
                    }
                    is Result.Error -> WeatherUiState.Error(
                        result.exception.message ?: "Failed to retrieve location weather telemetry."
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeatherUiState.Loading
        )

    fun updateSearchQuery(query: String) {
        savedStateHandle[queryKey] = query
    }

    fun updateCoordinates(latitude: Double, longitude: Double, name: String = "") {
        savedStateHandle[latKey] = latitude
        savedStateHandle[lonKey] = longitude
        savedStateHandle[queryKey] = ""
        savedStateHandle[locationNameKey] = name
        
        viewModelScope.launch {
            _currentTelemetry.value?.let { telemetry ->
                saveWeatherDraftUseCase(
                    telemetry = telemetry,
                    locationName = name.ifEmpty { null }
                )
            }
        }
    }

    fun createWeatherDraft() {
        viewModelScope.launch {
            _currentTelemetry.value?.let { telemetry ->
                val name = locationName.value
                saveWeatherDraftUseCase(
                    telemetry = telemetry,
                    locationName = name.ifEmpty { null }
                )
            }
        }
    }

    fun selectTab(index: Int) {
        savedStateHandle[selectedTabKey] = index
    }
}
