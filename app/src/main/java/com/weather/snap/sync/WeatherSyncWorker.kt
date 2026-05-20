package com.weather.snap.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.weather.core.domain.usecase.SyncWeatherSnapsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncWeatherSnapsUseCase: SyncWeatherSnapsUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncWeatherSnapsUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
