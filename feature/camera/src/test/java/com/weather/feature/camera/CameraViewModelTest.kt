package com.weather.feature.camera

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.weather.core.common.DispatcherProvider
import com.weather.core.file.CompressionResult
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import com.weather.core.testing.file.FakeFileStorageManager
import com.weather.core.testing.repository.FakeWeatherSnapRepository
import com.weather.core.testing.util.TestDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val dispatcherRule = TestDispatcherRule(testDispatcher)

    private val weatherSnapRepository = FakeWeatherSnapRepository()
    private val fileStorageManager = FakeFileStorageManager()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val unconfined = testDispatcher
        override val default = testDispatcher
    }
    
    @Test
    fun `processCapturedPhoto updates draft with photo metadata on success`() = runTest {
        val draftId = "draft-1"
        val savedStateHandle = SavedStateHandle(mapOf("draftId" to draftId))
        
        val initialSnap = WeatherSnap(
            id = draftId,
            telemetry = WeatherTelemetry(temperatureCelsius = 20.0, condition = WeatherCondition.CLOUDY, humidityPercentage = null, windSpeedKph = 10.0, latitude = 0.0, longitude = 0.0),
            photo = null,
            capturedAt = 0L,
            status = SyncStatus.DRAFT,
            notes = ""
        )
        weatherSnapRepository.saveSnapDraft(initialSnap)
        
        fileStorageManager.compressionResultToReturn = CompressionResult.Success(
            originalFile = File("original.jpg"),
            compressedFile = File("compressed.jpg"),
            thumbnailFile = File("thumb.jpg"),
            originalSizeBytes = 1000L,
            compressedSizeBytes = 500L,
            width = 1920,
            height = 1080
        )
        
        val viewModel = CameraViewModel(
            fileStorageManager,
            weatherSnapRepository,
            dispatcherProvider,
            savedStateHandle
        )
        
        advanceUntilIdle() // let init finish
        
        viewModel.processCapturedPhoto(byteArrayOf(1, 2, 3))
        
        viewModel.uiState.test {
            assertEquals(CameraUiState.Ready, awaitItem())
            assertEquals(CameraUiState.Capturing, awaitItem())
            val successState = awaitItem() as CameraUiState.Success
            assertEquals(File("compressed.jpg").absolutePath, successState.filePath)
            assertEquals(1920, successState.width)
            assertEquals(1080, successState.height)
        }
        
        // Verify repository is updated
        weatherSnapRepository.getSnapByIdStream(draftId).test {
            val snap = awaitItem()
            assertTrue(snap?.photo != null)
            assertEquals(File("compressed.jpg").absolutePath, snap?.photo?.filePath)
        }
    }
    
    @Test
    fun `processCapturedPhoto emits error on failure`() = runTest {
        val draftId = "draft-1"
        val savedStateHandle = SavedStateHandle(mapOf("draftId" to draftId))
        
        val initialSnap = WeatherSnap(
            id = draftId,
            telemetry = WeatherTelemetry(temperatureCelsius = 20.0, condition = WeatherCondition.CLOUDY, humidityPercentage = null, windSpeedKph = 10.0, latitude = 0.0, longitude = 0.0),
            photo = null,
            capturedAt = 0L,
            status = SyncStatus.DRAFT,
            notes = ""
        )
        weatherSnapRepository.saveSnapDraft(initialSnap)
        
        fileStorageManager.compressionResultToReturn = CompressionResult.InvalidFile
        
        val viewModel = CameraViewModel(
            fileStorageManager,
            weatherSnapRepository,
            dispatcherProvider,
            savedStateHandle
        )
        
        advanceUntilIdle() // let init finish
        
        viewModel.processCapturedPhoto(byteArrayOf())
        
        viewModel.uiState.test {
            assertEquals(CameraUiState.Ready, awaitItem())
            assertEquals(CameraUiState.Capturing, awaitItem())
            val errorState = awaitItem() as CameraUiState.Error
            assertTrue(errorState.exception.message?.contains("Invalid image") == true)
        }
    }
}
