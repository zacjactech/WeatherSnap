package com.weather.feature.report

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.weather.core.common.DispatcherProvider
import com.weather.core.domain.usecase.GetWeatherTelemetryDraftUseCase
import com.weather.core.domain.usecase.SaveWeatherSnapDraftUseCase
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import com.weather.core.testing.file.FakeFileStorageManager
import com.weather.core.testing.repository.FakeWeatherSnapRepository
import com.weather.core.testing.util.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    private val saveWeatherSnapDraftUseCase: SaveWeatherSnapDraftUseCase = mockk(relaxed = true)
    private val getWeatherTelemetryDraftUseCase: GetWeatherTelemetryDraftUseCase = mockk()
    private val weatherSnapRepository = FakeWeatherSnapRepository()
    private val fileStorageManager = FakeFileStorageManager()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val unconfined = testDispatcher
        override val default = testDispatcher
    }
    private val savedStateHandle = SavedStateHandle()

    @Test
    fun `init starts new draft when telemetry exists`() = runTest {
        val telemetry = WeatherTelemetry(temperatureCelsius = 25.0, condition = WeatherCondition.CLEAR, humidityPercentage = null, windSpeedKph = 10.0, latitude = 0.0, longitude = 0.0)
        coEvery { getWeatherTelemetryDraftUseCase() } returns Pair(telemetry, "London")
        coEvery { saveWeatherSnapDraftUseCase(any()) } coAnswers { weatherSnapRepository.saveSnapDraft(firstArg()) }

        val viewModel = ReportViewModel(
            saveWeatherSnapDraftUseCase,
            getWeatherTelemetryDraftUseCase,
            weatherSnapRepository,
            fileStorageManager,
            dispatcherProvider,
            savedStateHandle
        )

        viewModel.uiState.test {
            assertEquals(ReportUiState.Idle, awaitItem())
            val draftingState = awaitItem() as ReportUiState.Drafting
            assertEquals("London", draftingState.locationName)
            assertEquals(telemetry, draftingState.draft.telemetry)
            
            // Verify draft is saved
            coVerify(exactly = 1) { saveWeatherSnapDraftUseCase(any()) }
        }
    }
    
    @Test
    fun `updateNotes updates draft and calls save use case`() = runTest {
        val telemetry = WeatherTelemetry(temperatureCelsius = 25.0, condition = WeatherCondition.CLEAR, humidityPercentage = null, windSpeedKph = 10.0, latitude = 0.0, longitude = 0.0)
        coEvery { getWeatherTelemetryDraftUseCase() } returns Pair(telemetry, "London")
        coEvery { saveWeatherSnapDraftUseCase(any()) } coAnswers { weatherSnapRepository.saveSnapDraft(firstArg()) }
        
        val viewModel = ReportViewModel(
            saveWeatherSnapDraftUseCase,
            getWeatherTelemetryDraftUseCase,
            weatherSnapRepository,
            fileStorageManager,
            dispatcherProvider,
            savedStateHandle
        )
        
        advanceUntilIdle() // let init finish
        
        viewModel.updateNotes("New Note")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = expectMostRecentItem() as ReportUiState.Drafting
            assertEquals("New Note", state.draft.notes)
            
            // Should be called again when notes update
            coVerify(atLeast = 2) { saveWeatherSnapDraftUseCase(any()) }
        }
    }

    @Test
    fun `submitSnap saves as completed and triggers success`() = runTest {
        val telemetry = WeatherTelemetry(temperatureCelsius = 25.0, condition = WeatherCondition.CLEAR, humidityPercentage = null, windSpeedKph = 10.0, latitude = 0.0, longitude = 0.0)
        coEvery { getWeatherTelemetryDraftUseCase() } returns Pair(telemetry, "London")
        coEvery { saveWeatherSnapDraftUseCase(any()) } coAnswers { weatherSnapRepository.saveSnapDraft(firstArg()) }
        
        val viewModel = ReportViewModel(
            saveWeatherSnapDraftUseCase,
            getWeatherTelemetryDraftUseCase,
            weatherSnapRepository,
            fileStorageManager,
            dispatcherProvider,
            savedStateHandle
        )
        
        advanceUntilIdle()
        viewModel.submitSnap()
        advanceUntilIdle()

        viewModel.uiState.test {
            assertEquals(ReportUiState.Success, expectMostRecentItem())
        }

        // Verify status changed to COMPLETED in save call
        coVerify { saveWeatherSnapDraftUseCase(match { it.status == SyncStatus.COMPLETED }) }
    }
}
