package com.weather.core.network.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.database.dao.WeatherSnapDao
import com.weather.core.database.entity.asEntity
import com.weather.core.database.entity.asExternalModel
import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherSnap
import com.weather.core.network.WeatherSnapApi
import com.weather.core.network.model.NetworkWeatherSnapRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [WeatherSnapRepository].
 *
 * This class is the SINGLE SOURCE OF TRUTH (offline-first).
 * Remote synchronization is orchestrated at app-layer via [WeatherSyncWorker] +
 * WorkManager, which triggers [syncPendingSnaps].
 */
@Singleton
class WeatherSnapRepositoryImpl @Inject constructor(
    private val dao: WeatherSnapDao,
    private val weatherSnapApi: WeatherSnapApi,
    private val dispatcherProvider: DispatcherProvider
) : WeatherSnapRepository {

    override fun getSnapsStream(): Flow<List<WeatherSnap>> =
        dao.getSnapsStream()
            .map { entities -> entities.map { it.asExternalModel() } }
            .flowOn(dispatcherProvider.io)

    override fun getSnapByIdStream(id: String): Flow<WeatherSnap?> =
        dao.getSnapByIdStream(id)
            .map { it?.asExternalModel() }
            .flowOn(dispatcherProvider.io)

    override suspend fun saveSnapDraft(snap: WeatherSnap) = withContext(dispatcherProvider.io) {
        dao.insertSnap(snap.asEntity())
    }

    override suspend fun updateSnapSyncStatus(id: String, status: SyncStatus) =
        withContext(dispatcherProvider.io) {
            dao.updateSyncStatus(id, status.name)
        }

    override suspend fun deleteSnap(id: String) = withContext(dispatcherProvider.io) {
        dao.deleteSnapById(id)
    }

    override suspend fun getDiscardedSnaps(): List<WeatherSnap> = withContext(dispatcherProvider.io) {
        dao.getDiscardedSnaps().map { it.asExternalModel() }
    }

    /**
     * Fetches all DRAFT or FAILED snaps from Room and attempts to upload each one
     * to the remote backend. On success, marks the snap as SYNCED. On failure,
     * marks it as FAILED so WorkManager's retry policy will attempt it again.
     */
    override suspend fun syncPendingSnaps() = withContext(dispatcherProvider.io) {
        val pendingSnaps = dao.getPendingSnaps()
        for (entity in pendingSnaps) {
            try {
                val snap = entity.asExternalModel()
                val request = NetworkWeatherSnapRequest(
                    id = snap.id,
                    temperatureCelsius = snap.telemetry?.temperatureCelsius,
                    condition = snap.telemetry?.condition?.name,
                    humidityPercentage = snap.telemetry?.humidityPercentage,
                    windSpeedKph = snap.telemetry?.windSpeedKph,
                    latitude = snap.telemetry?.latitude,
                    longitude = snap.telemetry?.longitude,
                    capturedAt = snap.capturedAt,
                    notes = snap.notes
                )
                val response = weatherSnapApi.uploadSnap(request)
                if (response.isSuccessful) {
                    dao.updateSyncStatus(snap.id, SyncStatus.COMPLETED.name)
                } else {
                    dao.updateSyncStatus(snap.id, SyncStatus.FAILED.name)
                }
            } catch (e: Exception) {
                dao.updateSyncStatus(entity.id, SyncStatus.FAILED.name)
            }
        }
    }
}
