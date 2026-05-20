package com.weather.core.domain.usecase

import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.model.WeatherSnap
import javax.inject.Inject

/**
 * UseCase to save or update a WeatherSnap local draft in Room.
 */
class SaveWeatherSnapDraftUseCase @Inject constructor(
    private val repository: WeatherSnapRepository
) {
    suspend operator fun invoke(snap: WeatherSnap) {
        repository.saveSnapDraft(snap)
    }
}
