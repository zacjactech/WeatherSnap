package com.weather.core.database.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.database.dao.WeatherSnapDao
import com.weather.core.database.entity.asEntity
import com.weather.core.database.entity.asExternalModel
import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherSnap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [WeatherSnapRepository].
 *
 * This class is STRICTLY a local persistence layer — it owns no network logic.
 * Remote synchronization is orchestrated at app-layer via [WeatherSyncWorker] +
 * WorkManager, which calls [syncPendingSnaps] on the domain use-case, keeping
 * core:database free of any core:network dependency.
 *
 * Offline-first principle: Room is the SINGLE SOURCE OF TRUTH.
 */
@Singleton
class WeatherSnapRepositoryImpl @Inject constructor(
    private val dao: WeatherSnapDao,
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

    /**
     * Phase 1 stub — actual upload logic is wired at app-layer via [WeatherSyncWorker].
     * The worker fetches pending snaps from this repo, calls the network layer, and
     * calls [updateSnapSyncStatus] to reflect results back into Room.
     */
    override suspend fun syncPendingSnaps() = withContext(dispatcherProvider.io) {
        // Intentionally empty in Phase 1 — sync coordination lives in the app module.
    }
}
