package com.weather.core.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weather.core.model.PhotoMetadata
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry

@Entity(tableName = "weather_snaps")
data class WeatherSnapEntity(
    @PrimaryKey val id: String,
    @Embedded(prefix = "telemetry_") val telemetry: EmbeddedWeatherTelemetry?,
    @Embedded(prefix = "photo_") val photo: PhotoMetadataEntity?,
    val capturedAt: Long,
    val status: String,
    val notes: String
)

fun WeatherSnapEntity.asExternalModel(): WeatherSnap {
    return WeatherSnap(
        id = id,
        telemetry = telemetry?.let {
            WeatherTelemetry(
                temperatureCelsius = it.temperatureCelsius,
                condition = try { WeatherCondition.valueOf(it.condition) } catch (e: Exception) { WeatherCondition.UNKNOWN },
                humidityPercentage = it.humidityPercentage,
                windSpeedKph = it.windSpeedKph,
                windDirectionDegrees = it.windDirectionDegrees,
                pressure = it.pressure,
                latitude = it.latitude,
                longitude = it.longitude,
                visibilityKm = it.visibilityKm,
                uvIndex = it.uvIndex,
                cloudCoverPercent = it.cloudCoverPercent,
                dewPointCelsius = it.dewPointCelsius,
                highTempCelsius = it.highTempCelsius,
                lowTempCelsius = it.lowTempCelsius
            )
        },
        photo = photo?.let {
            PhotoMetadata(
                id = it.photoId,
                filePath = it.filePath,
                width = it.width,
                height = it.height,
                capturedAt = it.capturedAt
            )
        },
        capturedAt = capturedAt,
        status = try { SyncStatus.valueOf(status) } catch (e: Exception) { SyncStatus.DRAFT },
        notes = notes
    )
}

fun WeatherSnap.asEntity(): WeatherSnapEntity {
    return WeatherSnapEntity(
        id = id,
        telemetry = telemetry?.let {
            EmbeddedWeatherTelemetry(
                temperatureCelsius = it.temperatureCelsius,
                condition = it.condition.name,
                humidityPercentage = it.humidityPercentage,
                windSpeedKph = it.windSpeedKph,
                windDirectionDegrees = it.windDirectionDegrees,
                pressure = it.pressure,
                latitude = it.latitude,
                longitude = it.longitude,
                visibilityKm = it.visibilityKm,
                uvIndex = it.uvIndex,
                cloudCoverPercent = it.cloudCoverPercent,
                dewPointCelsius = it.dewPointCelsius,
                highTempCelsius = it.highTempCelsius,
                lowTempCelsius = it.lowTempCelsius
            )
        },
        photo = photo?.let {
            PhotoMetadataEntity(
                photoId = it.id,
                filePath = it.filePath,
                width = it.width,
                height = it.height,
                capturedAt = it.capturedAt
            )
        },
        capturedAt = capturedAt,
        status = status.name,
        notes = notes
    )
}
