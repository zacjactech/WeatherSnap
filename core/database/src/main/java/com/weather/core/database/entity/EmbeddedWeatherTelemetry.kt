package com.weather.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherTelemetry

/**
 * Embedded sub-object for weather telemetry stored inside [WeatherSnapEntity]
 * and [WeatherDraftEntity]. All optional fields are nullable to match the
 * domain model — no data loss on round-trip through Room.
 */
data class EmbeddedWeatherTelemetry(
    val temperatureCelsius: Double,
    val condition: String,
    val humidityPercentage: Int?,
    val windSpeedKph: Double,
    val windDirectionDegrees: Double?,
    val pressure: Double?,
    val latitude: Double,
    val longitude: Double,
    val visibilityKm: Double?,
    val uvIndex: Int?,
    val cloudCoverPercent: Int?,
    val dewPointCelsius: Double?,
    val highTempCelsius: Double?,
    val lowTempCelsius: Double?
)

@Entity(tableName = "city_suggestion_cache")
data class CitySuggestionCacheEntity(
    @PrimaryKey
    val normalizedQuery: String,
    val resultsJson: String,
    val cachedAt: Long
)

@Entity(tableName = "weather_drafts")
data class WeatherDraftEntity(
    @PrimaryKey
    val id: String = "current_draft",
    @Embedded(prefix = "telemetry_") val telemetry: EmbeddedWeatherTelemetry?,
    val locationName: String?,
    val createdAt: Long,
    val updatedAt: Long
)

fun WeatherTelemetry.toDraftEntity(locationName: String?): WeatherDraftEntity {
    return WeatherDraftEntity(
        telemetry = EmbeddedWeatherTelemetry(
            temperatureCelsius = this.temperatureCelsius,
            condition = this.condition.name,
            humidityPercentage = this.humidityPercentage,
            windSpeedKph = this.windSpeedKph,
            windDirectionDegrees = this.windDirectionDegrees,
            pressure = this.pressure,
            latitude = this.latitude,
            longitude = this.longitude,
            visibilityKm = this.visibilityKm,
            uvIndex = this.uvIndex,
            cloudCoverPercent = this.cloudCoverPercent,
            dewPointCelsius = this.dewPointCelsius,
            highTempCelsius = this.highTempCelsius,
            lowTempCelsius = this.lowTempCelsius
        ),
        locationName = locationName,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

fun WeatherDraftEntity.toWeatherTelemetry(): WeatherTelemetry {
    return WeatherTelemetry(
        temperatureCelsius = telemetry?.temperatureCelsius ?: 0.0,
        condition = try {
            WeatherCondition.valueOf(telemetry?.condition ?: "UNKNOWN")
        } catch (e: Exception) {
            WeatherCondition.UNKNOWN
        },
        humidityPercentage = telemetry?.humidityPercentage,
        windSpeedKph = telemetry?.windSpeedKph ?: 0.0,
        windDirectionDegrees = telemetry?.windDirectionDegrees,
        pressure = telemetry?.pressure,
        latitude = telemetry?.latitude ?: 0.0,
        longitude = telemetry?.longitude ?: 0.0,
        visibilityKm = telemetry?.visibilityKm,
        uvIndex = telemetry?.uvIndex,
        cloudCoverPercent = telemetry?.cloudCoverPercent,
        dewPointCelsius = telemetry?.dewPointCelsius,
        highTempCelsius = telemetry?.highTempCelsius,
        lowTempCelsius = telemetry?.lowTempCelsius
    )
}