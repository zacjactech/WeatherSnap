package com.weather.core.domain.usecase

import com.weather.core.domain.repository.WeatherDraftRepository
import com.weather.core.model.WeatherTelemetry
import javax.inject.Inject

/**
 * UseCase to retrieve the active telemetry draft and associated location name.
 */
class GetWeatherTelemetryDraftUseCase @Inject constructor(
    private val repository: WeatherDraftRepository
) {
    suspend operator fun invoke(): Pair<WeatherTelemetry?, String?>? {
        if (!repository.hasDraft()) return null
        val telemetry = repository.getCurrentTelemetryDraft()
        val locationName = repository.getCurrentLocationNameDraft()
        return Pair(telemetry, locationName)
    }
}
