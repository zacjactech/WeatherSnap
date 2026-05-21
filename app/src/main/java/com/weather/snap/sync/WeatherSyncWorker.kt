package com.weather.snap.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weather.core.database.repository.CitySuggestionCacheRepository
import com.weather.core.domain.repository.WeatherSnapRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic background worker responsible for:
 * 1. Uploading all DRAFT/FAILED weather snaps to the remote backend.
 * 2. Pruning expired city suggestion cache entries.
 *
 * Scheduled by [WeatherSnapApplication] using WorkManager's
 * [androidx.work.PeriodicWorkRequest]. WorkManager's built-in retry
 * policy (exponential back-off) handles transient network failures.
 */
@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherSnapRepository: WeatherSnapRepository,
    private val citySuggestionCacheRepository: CitySuggestionCacheRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "WeatherSnapPeriodicSync"
    }


    override suspend fun doWork(): Result {
        return try {
            // 1. Upload pending snaps
            weatherSnapRepository.syncPendingSnaps()

            // 2. Prune expired city cache to prevent stale search results
            citySuggestionCacheRepository.clearExpiredCache()

            Result.success()
        } catch (e: Exception) {
            // Return retry() so WorkManager applies exponential back-off
            Result.retry()
        }
    }
}
