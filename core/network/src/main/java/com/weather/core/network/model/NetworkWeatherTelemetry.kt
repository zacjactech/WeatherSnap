package com.weather.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkWeatherTelemetry(
    val tempC: Double,
    val condition: String,
    val humidity: Int,
    val windKph: Double,
    val lat: Double,
    val lon: Double
)
