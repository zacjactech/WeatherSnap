package com.weather.core.model



/**
 * Domain model representing a snapshot of current weather telemetry.
 *
 * [humidityPercentage] is nullable because Open-Meteo only returns humidity
 * in the hourly block — it may not be available for the exact current moment.
 * [pressure] is sourced from the `surface_pressure` hourly field.
 * [visibilityKm], [uvIndex], [cloudCoverPercent], [dewPointCelsius] are optional
 * fields populated when the API provides them.
 * [windDirectionDegrees] is the actual compass bearing from the API (0–360°).
 * [highTempCelsius] / [lowTempCelsius] are the actual daily max/min temperatures.
 */

data class WeatherTelemetry(
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val humidityPercentage: Int?,
    val windSpeedKph: Double,
    val windDirectionDegrees: Double? = null,
    val pressure: Double? = null,
    val latitude: Double,
    val longitude: Double,
    val visibilityKm: Double? = null,
    val uvIndex: Int? = null,
    val cloudCoverPercent: Int? = null,
    val dewPointCelsius: Double? = null,
    val highTempCelsius: Double? = null,
    val lowTempCelsius: Double? = null
)
