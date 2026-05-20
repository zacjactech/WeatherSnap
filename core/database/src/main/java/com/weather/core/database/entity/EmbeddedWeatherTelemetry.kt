package com.weather.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherTelemetry

data class EmbeddedWeatherTelemetry(
    val temperatureCelsius: Double,
    val condition: String,
    val humidityPercentage: Int?,
    val windSpeedKph: Double,
    val pressure: Double?,
    val latitude: Double,
    val longitude: Double
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
            pressure = this.pressure,
            latitude = this.latitude,
            longitude = this.longitude
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
        pressure = telemetry?.pressure,
        latitude = telemetry?.latitude ?: 0.0,
        longitude = telemetry?.longitude ?: 0.0
    )
}