package com.weather.core.database.repository

import com.weather.core.database.dao.WeatherDraftDao
import com.weather.core.database.entity.WeatherDraftEntity
import com.weather.core.database.entity.toDraftEntity
import com.weather.core.database.entity.toWeatherTelemetry
import com.weather.core.domain.repository.WeatherDraftRepository
import com.weather.core.model.WeatherTelemetry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDraftRepositoryImpl @Inject constructor(
    private val weatherDraftDao: WeatherDraftDao
) : com.weather.core.domain.repository.WeatherDraftRepository {

    fun getCurrentDraftFlow(): Flow<WeatherDraftEntity?> {
        return weatherDraftDao.getCurrentDraftFlow()
    }

    suspend fun getCurrentDraft(): WeatherDraftEntity? {
        return weatherDraftDao.getCurrentDraft()
    }

    override suspend fun saveDraft(telemetry: WeatherTelemetry, locationName: String?) {
        val existingDraft = weatherDraftDao.getCurrentDraft()
        if (existingDraft != null) {
            val updatedDraft = existingDraft.copy(
                telemetry = telemetry.toDraftEntity(locationName).telemetry,
                locationName = locationName,
                updatedAt = System.currentTimeMillis()
            )
            weatherDraftDao.updateDraft(updatedDraft)
        } else {
            val newDraft = telemetry.toDraftEntity(locationName)
            weatherDraftDao.insertDraft(newDraft)
        }
    }

    override suspend fun createDraftIfNotExists(telemetry: WeatherTelemetry, locationName: String?) {
        if (!weatherDraftDao.hasDraft()) {
            val draft = telemetry.toDraftEntity(locationName)
            weatherDraftDao.insertDraft(draft)
        }
    }

    suspend fun deleteDraft() {
        weatherDraftDao.deleteCurrentDraft()
    }

    override suspend fun hasDraft(): Boolean {
        return weatherDraftDao.hasDraft()
    }

    override suspend fun getCurrentTelemetryDraft(): WeatherTelemetry? {
        return weatherDraftDao.getCurrentDraft()?.toWeatherTelemetry()
    }

    override suspend fun getCurrentLocationNameDraft(): String? {
        return weatherDraftDao.getCurrentDraft()?.locationName
    }
}