package com.weather.feature.weather

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.weather.core.common.Result
import com.weather.core.domain.usecase.GetWeatherTelemetryUseCase
import com.weather.core.domain.usecase.SearchCitiesUseCase
import com.weather.core.domain.usecase.SaveWeatherDraftUseCase
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherTelemetry
import com.weather.core.datastore.SettingsRepository
import com.weather.core.model.UserSettings
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private lateinit var getWeatherTelemetryUseCase: GetWeatherTelemetryUseCase
    private lateinit var searchCitiesUseCase: SearchCitiesUseCase
    private lateinit var saveWeatherDraftUseCase: SaveWeatherDraftUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: WeatherViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getWeatherTelemetryUseCase = mockk()
        searchCitiesUseCase = mockk()
        saveWeatherDraftUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf(
            "latitude" to 47.6062,
            "longitude" to -122.3321
        ))
        settingsRepository = mockk()
        coEvery { settingsRepository.settingsFlow } returns flowOf(UserSettings())
        
        viewModel = WeatherViewModel(
            getWeatherTelemetryUseCase,
            searchCitiesUseCase,
            saveWeatherDraftUseCase,
            settingsRepository,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateCoordinates fetches weather and updates uiState successfully`() = runTest(testDispatcher) {
        // Arrange
        val telemetry = WeatherTelemetry(
            temperatureCelsius = 25.0,
            humidityPercentage = 50,
            windSpeedKph = 10.0,
            windDirectionDegrees = 90.0,
            pressure = 1015.0,
            visibilityKm = 10.0,
            highTempCelsius = 30.0,
            lowTempCelsius = 20.0,
            condition = WeatherCondition.CLEAR,
            latitude = 37.7749,
            longitude = -122.4194
        )
        coEvery { getWeatherTelemetryUseCase(any(), any()) } returns flowOf(Result.Success(telemetry))
        
        // Act
        viewModel.updateCoordinates(37.7749, -122.4194)
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue("Expected Loading or Success, got $initialState", initialState is WeatherUiState.Loading || initialState is WeatherUiState.Success)
            
            val state = if (initialState is WeatherUiState.Success) initialState else awaitItem()
            assertTrue("Expected Success, got $state", state is WeatherUiState.Success)
            val successState = state as WeatherUiState.Success
            assertEquals(25.0, successState.telemetry.temperatureCelsius, 0.01)
            assertEquals(WeatherCondition.CLEAR, successState.telemetry.condition)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateCoordinates handles errors and sets Error state`() = runTest(testDispatcher) {
        // Arrange
        coEvery { getWeatherTelemetryUseCase(any(), any()) } returns flowOf(Result.Error(Exception("Network Error")))

        // Act
        viewModel.updateCoordinates(37.7749, -122.4194)
        advanceUntilIdle()

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue("Expected Loading or Error, got $initialState", initialState is WeatherUiState.Loading || initialState is WeatherUiState.Error)
            
            val state = if (initialState is WeatherUiState.Error) initialState else awaitItem()
            assertTrue("Expected Error, got $state", state is WeatherUiState.Error)
            assertEquals("Network Error", (state as WeatherUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
