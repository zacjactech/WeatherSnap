package com.weather.core.network.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.common.Result
import com.weather.core.common.asResult
import com.weather.core.database.repository.CitySuggestionCacheRepository
import com.weather.core.domain.repository.WeatherRepository
import com.weather.core.model.LocationSearchResult
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherTelemetry
import com.weather.core.network.GeocodingApi
import com.weather.core.network.OpenMeteoApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val openMeteoApi: OpenMeteoApi,
    private val geocodingApi: GeocodingApi,
    private val citySuggestionCacheRepository: CitySuggestionCacheRepository,
    private val dispatcherProvider: DispatcherProvider
) : WeatherRepository {

    override fun getCurrentWeather(lat: Double, lon: Double): Flow<Result<WeatherTelemetry>> =
        flow {
            val response = openMeteoApi.getCurrentWeather(latitude = lat, longitude = lon)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val current = body.currentWeather
                    ?: throw IllegalStateException("current_weather block missing from Open-Meteo response")

                val condition = wmoCodeToCondition(current.weathercode)
                val humidity = body.hourly?.relativehumidity2m?.firstOrNull()

                // Real surface pressure from API hourly block (first index = current hour)
                val pressureVal = body.hourly?.surfacePressure?.firstOrNull()

                // Extra hourly fields — Open-Meteo returns metres for visibility; convert to km
                val visibilityKm = body.hourly?.visibility?.firstOrNull()?.let { it / 1000.0 }
                val uvIndex = body.hourly?.uvIndex?.firstOrNull()?.toInt()
                val cloudCover = body.hourly?.cloudcover?.firstOrNull()
                val dewPoint = body.hourly?.dewpoint2m?.firstOrNull()

                // Real daily high/low temperatures from the daily block (index 0 = today)
                val highTemp = body.daily?.temperature2mMax?.firstOrNull()
                val lowTemp = body.daily?.temperature2mMin?.firstOrNull()

                emit(
                    WeatherTelemetry(
                        temperatureCelsius = current.temperature,
                        condition = condition,
                        humidityPercentage = humidity,
                        windSpeedKph = current.windspeed,
                        windDirectionDegrees = current.winddirection,
                        pressure = pressureVal,
                        latitude = body.latitude,
                        longitude = body.longitude,
                        visibilityKm = visibilityKm,
                        uvIndex = uvIndex,
                        cloudCoverPercent = cloudCover,
                        dewPointCelsius = dewPoint,
                        highTempCelsius = highTemp,
                        lowTempCelsius = lowTemp
                    )
                )
            } else {
                throw Exception("Open-Meteo request failed: HTTP ${response.code()} — ${response.message()}")
            }
        }
        .asResult()
        .flowOn(dispatcherProvider.io)

    override fun searchCities(query: String): Flow<Result<List<LocationSearchResult>>> = flow {
            val cachedResult = citySuggestionCacheRepository.getCachedSuggestions(query)
            if (cachedResult is Result.Success) {
                emit(Result.Success(cachedResult.data))
                return@flow
            }

            try {
                val response = geocodingApi.searchCity(name = query)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val results = body.results.map {
                        LocationSearchResult(
                            id = it.id,
                            name = it.name,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            country = it.country,
                            admin1 = it.admin1
                        )
                    }
                    
                    citySuggestionCacheRepository.cacheSuggestions(query, results)
                    emit(Result.Success(results))
                } else {
                    emit(Result.Error(Exception("Geocoding request failed: HTTP ${response.code()}")))
                }
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }.flowOn(dispatcherProvider.io)

    private fun wmoCodeToCondition(code: Int): WeatherCondition = when (code) {
        0 -> WeatherCondition.CLEAR
        in 1..3 -> WeatherCondition.CLOUDY
        45, 48 -> WeatherCondition.FOG
        in 51..67 -> WeatherCondition.RAIN
        in 71..77 -> WeatherCondition.SNOW
        in 80..82 -> WeatherCondition.RAIN
        85, 86 -> WeatherCondition.SNOW
        in 95..99 -> WeatherCondition.THUNDERSTORM
        else -> WeatherCondition.UNKNOWN
    }
}