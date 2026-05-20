package com.weather.core.domain.usecase

import com.weather.core.common.Result
import com.weather.core.domain.repository.WeatherRepository
import com.weather.core.model.LocationSearchResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    operator fun invoke(query: String): Flow<Result<List<LocationSearchResult>>> {
        return repository.searchCities(query)
    }
}
