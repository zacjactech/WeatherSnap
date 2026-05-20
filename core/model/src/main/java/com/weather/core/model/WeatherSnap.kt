package com.weather.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherSnap(
    val id: String,
    val telemetry: WeatherTelemetry?,
    val photo: PhotoMetadata?,
    val capturedAt: Long,
    val status: SyncStatus,
    val notes: String = ""
)
