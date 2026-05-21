package com.weather.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.weather.core.database.dao.WeatherSnapDao
import com.weather.core.database.entity.WeatherSnapEntity
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class WeatherSnapDaoTest {
    private lateinit var weatherDatabase: WeatherDatabase
    private lateinit var weatherSnapDao: WeatherSnapDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        weatherDatabase = Room.inMemoryDatabaseBuilder(
            context, WeatherDatabase::class.java
        ).build()
        weatherSnapDao = weatherDatabase.weatherSnapDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        weatherDatabase.close()
    }

    @Test
    fun insertAndGetWeatherSnaps() = runTest {
        val entity = WeatherSnapEntity(
            id = "test-1",
            tempCelsius = 25.0,
            condition = WeatherCondition.SUNNY,
            humidity = null,
            windKph = 0.0,
            lat = 0.0,
            lon = 0.0,
            photoId = null,
            filePath = null,
            originalFilePath = null,
            thumbnailPath = null,
            width = null,
            height = null,
            photoCapturedAt = null,
            capturedAt = System.currentTimeMillis(),
            status = SyncStatus.PENDING,
            notes = "Test note"
        )
        
        weatherSnapDao.insertOrUpdateSnap(entity)

        weatherSnapDao.getAllSnaps().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("test-1", list[0].id)
            assertEquals(SyncStatus.PENDING, list[0].status)
        }
    }

    @Test
    fun updateSyncStatusUpdatesCorrectly() = runTest {
        val entity = WeatherSnapEntity(
            id = "test-1",
            tempCelsius = 25.0,
            condition = WeatherCondition.SUNNY,
            humidity = null,
            windKph = 0.0,
            lat = 0.0,
            lon = 0.0,
            photoId = null,
            filePath = null,
            originalFilePath = null,
            thumbnailPath = null,
            width = null,
            height = null,
            photoCapturedAt = null,
            capturedAt = System.currentTimeMillis(),
            status = SyncStatus.PENDING,
            notes = ""
        )
        weatherSnapDao.insertOrUpdateSnap(entity)
        
        weatherSnapDao.updateSyncStatus("test-1", SyncStatus.COMPLETED)
        
        weatherSnapDao.getSnapById("test-1").test {
            val snap = awaitItem()
            assertEquals(SyncStatus.COMPLETED, snap?.status)
        }
    }

    @Test
    fun getPendingSnapsReturnsOnlyPending() = runTest {
        val entity1 = WeatherSnapEntity("1", 25.0, WeatherCondition.SUNNY, null, 0.0, 0.0, 0.0, null, null, null, null, null, null, null, 0L, SyncStatus.PENDING, "")
        val entity2 = WeatherSnapEntity("2", 25.0, WeatherCondition.SUNNY, null, 0.0, 0.0, 0.0, null, null, null, null, null, null, null, 0L, SyncStatus.COMPLETED, "")
        
        weatherSnapDao.insertOrUpdateSnap(entity1)
        weatherSnapDao.insertOrUpdateSnap(entity2)
        
        val pending = weatherSnapDao.getPendingOrFailedSnaps()
        assertEquals(1, pending.size)
        assertEquals("1", pending[0].id)
    }
}
