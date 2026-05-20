package com.weather.core.domain.usecase

import com.weather.core.domain.repository.WeatherSnapRepository
import javax.inject.Inject

/**
 * UseCase to synchronize all pending DRAFT/FAILED snaps with the remote backend.
 * Invoked by [WeatherSyncWorker] via WorkManager - runs on Dispatchers.IO.
 */
class SyncWeatherSnapsUseCase @Inject constructor(
    private val repository: WeatherSnapRepository
) {
    suspend operator fun invoke() {
        repository.syncPendingSnaps()
    }
}
