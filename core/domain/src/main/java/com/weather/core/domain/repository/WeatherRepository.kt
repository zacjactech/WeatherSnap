package com.weather.core.domain.repository

import com.weather.core.common.Result
import com.weather.core.model.WeatherTelemetry
import kotlinx.coroutines.flow.Flow

import com.weather.core.model.LocationSearchResult

interface WeatherRepository {
    /**
     * Fetches real-time weather telemetry from remote or local fallback cache for a coordinate.
     */
    fun getCurrentWeather(lat: Double, lon: Double): Flow<Result<WeatherTelemetry>>

    /**
     * Searches for a city name using the Open-Meteo geocoding API.
     */
    fun searchCities(query: String): Flow<Result<List<LocationSearchResult>>>
}
