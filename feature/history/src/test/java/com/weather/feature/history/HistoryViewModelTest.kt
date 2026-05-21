package com.weather.feature.history

import app.cash.turbine.test
import com.weather.core.common.DispatcherProvider
import com.weather.core.domain.usecase.GetWeatherSnapsUseCase
import com.weather.core.domain.usecase.SyncWeatherSnapsUseCase
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import com.weather.core.testing.util.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    private val getWeatherSnapsUseCase: GetWeatherSnapsUseCase = mockk()
    private val syncWeatherSnapsUseCase: SyncWeatherSnapsUseCase = mockk(relaxed = true)
    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val unconfined = testDispatcher
        override val default = testDispatcher
    }
    
    @Test
    fun `uiState emits Empty when no snaps exist`() = runTest {
        every { getWeatherSnapsUseCase() } returns flowOf(emptyList())
        
        val viewModel = HistoryViewModel(
            getWeatherSnapsUseCase,
            syncWeatherSnapsUseCase,
            dispatcherProvider
        )
        
        viewModel.uiState.test {
            assertEquals(HistoryUiState.Loading, awaitItem())
            assertEquals(HistoryUiState.Empty, awaitItem())
        }
    }
    
    @Test
    fun `uiState emits Success when snaps exist`() = runTest {
        val snap = WeatherSnap(
            id = "1",
            telemetry = WeatherTelemetry(temperatureCelsius = 20.0, condition = WeatherCondition.CLOUDY, humidityPercentage = null, windSpeedKph = 10.0, latitude = 0.0, longitude = 0.0),
            photo = null,
            capturedAt = 0L,
            status = SyncStatus.COMPLETED,
            notes = "Test"
        )
        every { getWeatherSnapsUseCase() } returns flowOf(listOf(snap))
        
        val viewModel = HistoryViewModel(
            getWeatherSnapsUseCase,
            syncWeatherSnapsUseCase,
            dispatcherProvider
        )
        
        viewModel.uiState.test {
            assertEquals(HistoryUiState.Loading, awaitItem())
            val successState = awaitItem() as HistoryUiState.Success
            assertEquals(1, successState.snaps.size)
            assertEquals("1", successState.snaps[0].id)
        }
    }
    
    @Test
    fun `forceSync calls syncWeatherSnapsUseCase`() = runTest {
        every { getWeatherSnapsUseCase() } returns flowOf(emptyList())
        
        val viewModel = HistoryViewModel(
            getWeatherSnapsUseCase,
            syncWeatherSnapsUseCase,
            dispatcherProvider
        )
        
        viewModel.forceSync()
        advanceUntilIdle()
        
        coVerify(exactly = 1) { syncWeatherSnapsUseCase() }
    }
}
