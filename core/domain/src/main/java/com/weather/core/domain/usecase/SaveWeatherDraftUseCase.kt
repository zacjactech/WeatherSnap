package com.weather.core.domain.usecase

import com.weather.core.domain.repository.WeatherDraftRepository
import com.weather.core.model.WeatherTelemetry
import javax.inject.Inject

class SaveWeatherDraftUseCase @Inject constructor(
    private val weatherDraftRepository: WeatherDraftRepository
) {
    suspend operator fun invoke(telemetry: WeatherTelemetry, locationName: String?) {
        weatherDraftRepository.saveDraft(telemetry, locationName)
    }

    suspend fun createIfNotExists(telemetry: WeatherTelemetry, locationName: String?) {
        weatherDraftRepository.createDraftIfNotExists(telemetry, locationName)
    }
}