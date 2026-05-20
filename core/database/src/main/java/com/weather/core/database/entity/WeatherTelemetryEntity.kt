package com.weather.core.database.entity

/**
 * Embedded sub-entity for weather telemetry stored inside [WeatherSnapEntity].
 * humidityPercentage is nullable because Open-Meteo may not always provide it.
 */
data class WeatherTelemetryEntity(
    val temperatureCelsius: Double,
    val condition: String,
    val humidityPercentage: Int?,
    val windSpeedKph: Double,
    val latitude: Double,
    val longitude: Double
)
