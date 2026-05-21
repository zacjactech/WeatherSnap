package com.weather.core.network.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.domain.repository.WeatherRepository
import com.weather.core.model.WeatherTelemetry
import com.weather.core.model.WeatherCondition
import com.weather.core.network.OpenMeteoApi
import com.weather.core.network.model.CurrentWeatherDto
import com.weather.core.network.model.DailyDataDto
import com.weather.core.network.model.HourlyDataDto
import com.weather.core.network.model.OpenMeteoResponse
import com.weather.core.network.GeocodingApi
import com.weather.core.database.repository.CitySuggestionCacheRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.dropWhile
import retrofit2.Response
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlinx.coroutines.flow.first
import com.weather.core.common.Result

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {

    private lateinit var api: OpenMeteoApi
    private lateinit var geocodingApi: GeocodingApi
    private lateinit var citySuggestionCacheRepository: CitySuggestionCacheRepository
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var repository: WeatherRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        api = mockk()
        geocodingApi = mockk()
        citySuggestionCacheRepository = mockk()
        dispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
            override val default: CoroutineDispatcher = testDispatcher
            override val unconfined: CoroutineDispatcher = testDispatcher
        }
        repository = WeatherRepositoryImpl(api, geocodingApi, citySuggestionCacheRepository, dispatcherProvider)
    }

    @Test
    fun `getCurrentWeather maps OpenMeteoResponse to WeatherTelemetry correctly`() = runTest(testDispatcher) {
        // Arrange
        val lat = 37.7749
        val lon = -122.4194
        val mockResponse = OpenMeteoResponse(
            latitude = lat,
            longitude = lon,
            currentWeather = CurrentWeatherDto(
                time = "2024-05-20T12:00",
                temperature = 22.5,
                windspeed = 12.0,
                winddirection = 180.0,
                weathercode = 0,
                isDay = 1
            ),
            hourly = HourlyDataDto(
                time = listOf("2024-05-20T12:00"),
                surfacePressure = listOf(1013.25),
                visibility = listOf(10000.0),
                relativehumidity2m = listOf(45)
            ),
            daily = DailyDataDto(
                time = listOf("2024-05-20"),
                temperature2mMax = listOf(28.0),
                temperature2mMin = listOf(15.0)
            )
        )

        coEvery { api.getCurrentWeather(latitude = lat, longitude = lon) } returns Response.success(mockResponse)

        // Act
        val result = repository.getCurrentWeather(lat, lon).dropWhile { it is Result.Loading }.first()
        
        // Assert
        assertTrue("Expected Result.Success but was $result", result is Result.Success)
        val telemetry = (result as Result.Success).data
        assertEquals(22.5, telemetry.temperatureCelsius, 0.01)
        assertEquals(45, telemetry.humidityPercentage)
        assertEquals(12.0, telemetry.windSpeedKph, 0.01)
        assertEquals(180.0, telemetry.windDirectionDegrees!!, 0.01)
        assertEquals(1013.25, telemetry.pressure!!, 0.01)
        assertEquals(10.0, telemetry.visibilityKm!!, 0.01)
        assertEquals(28.0, telemetry.highTempCelsius!!, 0.01)
        assertEquals(15.0, telemetry.lowTempCelsius!!, 0.01)
        assertEquals(WeatherCondition.CLEAR, telemetry.condition)
    }
}
