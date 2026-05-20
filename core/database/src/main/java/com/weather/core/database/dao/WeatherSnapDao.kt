package com.weather.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weather.core.database.entity.WeatherSnapEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherSnapDao {

    @Query("SELECT * FROM weather_snaps ORDER BY capturedAt DESC")
    fun getSnapsStream(): Flow<List<WeatherSnapEntity>>

    @Query("SELECT * FROM weather_snaps WHERE id = :id")
    fun getSnapByIdStream(id: String): Flow<WeatherSnapEntity?>

    @Query("SELECT * FROM weather_snaps WHERE id = :id")
    suspend fun getSnapById(id: String): WeatherSnapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnap(snap: WeatherSnapEntity)

    @Query("UPDATE weather_snaps SET status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("SELECT * FROM weather_snaps WHERE status = 'DRAFT' OR status = 'FAILED'")
    suspend fun getPendingSnaps(): List<WeatherSnapEntity>

    @Query("DELETE FROM weather_snaps WHERE id = :id")
    suspend fun deleteSnapById(id: String)
}
