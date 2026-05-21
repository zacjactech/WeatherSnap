package com.weather.core.network.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.database.dao.WeatherSnapDao
import com.weather.core.database.entity.WeatherSnapEntity
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.network.WeatherSnapApi
import com.weather.core.database.entity.EmbeddedWeatherTelemetry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherSnapRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val unconfined = testDispatcher
        override val default = testDispatcher
    }

    private val dao: WeatherSnapDao = mockk(relaxed = true)
    private val api: WeatherSnapApi = mockk()

    @Test
    fun `syncPendingSnaps marks as COMPLETED on success`() = runTest(testDispatcher) {
        val entity = WeatherSnapEntity(
            id = "1",
            telemetry = EmbeddedWeatherTelemetry(
                temperatureCelsius = 20.0,
                condition = WeatherCondition.CLEAR.name,
                humidityPercentage = null,
                windSpeedKph = 0.0,
                windDirectionDegrees = null,
                pressure = null,
                latitude = 0.0,
                longitude = 0.0,
                visibilityKm = null,
                uvIndex = null,
                cloudCoverPercent = null,
                dewPointCelsius = null,
                highTempCelsius = null,
                lowTempCelsius = null
            ),
            photo = null,
            capturedAt = 0L,
            status = SyncStatus.DRAFT.name,
            notes = ""
        )
        coEvery { dao.getPendingSnaps() } returns listOf(entity)
        coEvery { api.uploadSnap(any()) } returns Response.success(Unit)

        val repository = WeatherSnapRepositoryImpl(dao, api, dispatcherProvider)
        repository.syncPendingSnaps()

        coVerify(exactly = 1) { dao.updateSyncStatus("1", SyncStatus.COMPLETED.name) }
    }

    @Test
    fun `syncPendingSnaps marks as FAILED on network error`() = runTest(testDispatcher) {
        val entity = WeatherSnapEntity(
            id = "1",
            telemetry = EmbeddedWeatherTelemetry(
                temperatureCelsius = 20.0,
                condition = WeatherCondition.CLEAR.name,
                humidityPercentage = null,
                windSpeedKph = 0.0,
                windDirectionDegrees = null,
                pressure = null,
                latitude = 0.0,
                longitude = 0.0,
                visibilityKm = null,
                uvIndex = null,
                cloudCoverPercent = null,
                dewPointCelsius = null,
                highTempCelsius = null,
                lowTempCelsius = null
            ),
            photo = null,
            capturedAt = 0L,
            status = SyncStatus.DRAFT.name,
            notes = ""
        )
        coEvery { dao.getPendingSnaps() } returns listOf(entity)
        coEvery { api.uploadSnap(any()) } returns Response.error(500, "Error".toResponseBody("application/json".toMediaTypeOrNull()))

        val repository = WeatherSnapRepositoryImpl(dao, api, dispatcherProvider)
        repository.syncPendingSnaps()

        coVerify(exactly = 1) { dao.updateSyncStatus("1", SyncStatus.FAILED.name) }
    }
}
