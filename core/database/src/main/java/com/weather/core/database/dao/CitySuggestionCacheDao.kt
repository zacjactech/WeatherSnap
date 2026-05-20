package com.weather.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weather.core.database.entity.CitySuggestionCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitySuggestionCacheDao {

    @Query("SELECT * FROM city_suggestion_cache WHERE normalizedQuery = :normalizedQuery")
    suspend fun getCachedSuggestion(normalizedQuery: String): CitySuggestionCacheEntity?

    @Query("SELECT * FROM city_suggestion_cache WHERE normalizedQuery = :normalizedQuery")
    fun getCachedSuggestionFlow(normalizedQuery: String): Flow<CitySuggestionCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CitySuggestionCacheEntity)

    @Query("DELETE FROM city_suggestion_cache WHERE cachedAt < :expirationTime")
    suspend fun deleteExpiredCache(expirationTime: Long)

    @Query("DELETE FROM city_suggestion_cache")
    suspend fun clearCache()
}