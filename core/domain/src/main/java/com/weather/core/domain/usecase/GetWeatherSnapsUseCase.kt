package com.weather.core.domain.usecase

import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.model.WeatherSnap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to observe all persisted WeatherSnap records as a live Room stream.
 * Returns results sorted by capturedAt DESC (most recent first).
 */
class GetWeatherSnapsUseCase @Inject constructor(
    private val repository: WeatherSnapRepository
) {
    operator fun invoke(): Flow<List<WeatherSnap>> = repository.getSnapsStream()
}
