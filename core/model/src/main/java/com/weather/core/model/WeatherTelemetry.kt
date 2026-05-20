package com.weather.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a snapshot of current weather telemetry.
 *
 * [humidityPercentage] is nullable because Open-Meteo only returns humidity
 * in the hourly block — it may not be available for the exact current moment.
 * [pressure] is nullable as it requires additional API parameters.
 */
@Serializable
data class WeatherTelemetry(
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val humidityPercentage: Int?,
    val windSpeedKph: Double,
    val pressure: Double? = null,
    val latitude: Double,
    val longitude: Double
)
