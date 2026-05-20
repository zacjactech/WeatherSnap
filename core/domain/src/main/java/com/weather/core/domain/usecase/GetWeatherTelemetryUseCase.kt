package com.weather.core.domain.usecase

import com.weather.core.common.Result
import com.weather.core.domain.repository.WeatherRepository
import com.weather.core.model.WeatherTelemetry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to retrieve telemetry information for a geocoordinate.
 */
class GetWeatherTelemetryUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(lat: Double, lon: Double): Flow<Result<WeatherTelemetry>> {
        return repository.getCurrentWeather(lat, lon)
    }
}
