package com.weather.core.domain.repository

import com.weather.core.model.WeatherTelemetry

interface WeatherDraftRepository {
    suspend fun saveDraft(telemetry: WeatherTelemetry, locationName: String?)
    suspend fun createDraftIfNotExists(telemetry: WeatherTelemetry, locationName: String?)
    suspend fun hasDraft(): Boolean
    suspend fun getCurrentTelemetryDraft(): WeatherTelemetry?
    suspend fun getCurrentLocationNameDraft(): String?
}