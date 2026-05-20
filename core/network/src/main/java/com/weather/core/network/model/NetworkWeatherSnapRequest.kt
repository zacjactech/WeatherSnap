package com.weather.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkWeatherSnapRequest(
    val id: String,
    val temperatureCelsius: Double?,
    val condition: String?,
    val humidityPercentage: Int?,
    val windSpeedKph: Double?,
    val latitude: Double?,
    val longitude: Double?,
    val capturedAt: Long,
    val notes: String
)
