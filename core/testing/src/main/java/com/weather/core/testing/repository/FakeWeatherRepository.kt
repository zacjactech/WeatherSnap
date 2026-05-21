package com.weather.core.testing.repository

import com.weather.core.common.Result
import com.weather.core.domain.repository.WeatherRepository
import com.weather.core.model.LocationSearchResult
import com.weather.core.model.WeatherTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeWeatherRepository : WeatherRepository {
    var shouldReturnError = false
    var weatherTelemetryToReturn: WeatherTelemetry? = null
    var searchResultsToReturn: List<LocationSearchResult> = emptyList()

    override fun getCurrentWeather(lat: Double, lon: Double): Flow<Result<WeatherTelemetry>> = flow {
        emit(Result.Loading)
        if (shouldReturnError) {
            emit(Result.Error(Exception("Test exception")))
        } else {
            weatherTelemetryToReturn?.let {
                emit(Result.Success(it))
            } ?: emit(Result.Error(Exception("No telemetry set")))
        }
    }

    override fun searchCities(query: String): Flow<Result<List<LocationSearchResult>>> = flow {
        emit(Result.Loading)
        if (shouldReturnError) {
            emit(Result.Error(Exception("Test exception")))
        } else {
            emit(Result.Success(searchResultsToReturn))
        }
    }
}
