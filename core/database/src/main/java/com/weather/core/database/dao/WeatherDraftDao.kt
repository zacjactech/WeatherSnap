package com.weather.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.weather.core.database.entity.WeatherDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDraftDao {

    @Query("SELECT * FROM weather_drafts WHERE id = 'current_draft'")
    fun getCurrentDraftFlow(): Flow<WeatherDraftEntity?>

    @Query("SELECT * FROM weather_drafts WHERE id = 'current_draft'")
    suspend fun getCurrentDraft(): WeatherDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: WeatherDraftEntity)

    @Update
    suspend fun updateDraft(draft: WeatherDraftEntity)

    @Query("DELETE FROM weather_drafts WHERE id = 'current_draft'")
    suspend fun deleteCurrentDraft()

    @Query("SELECT EXISTS(SELECT 1 FROM weather_drafts WHERE id = 'current_draft')")
    suspend fun hasDraft(): Boolean
}