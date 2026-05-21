package com.weather.core.database.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.common.Result
import com.weather.core.database.dao.CitySuggestionCacheDao
import com.weather.core.database.entity.CitySuggestionCacheEntity
import com.weather.core.model.LocationSearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitySuggestionCacheRepository @Inject constructor(
    private val citySuggestionCacheDao: CitySuggestionCacheDao,
    private val dispatcherProvider: DispatcherProvider,
    private val gson: Gson
) {
    companion object {
        private const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 minutes
    }

    fun normalizeQuery(query: String): String {
        return query.lowercase().trim().replace(Regex("\\s+"), " ")
    }

    suspend fun getCachedSuggestions(query: String): Result<List<LocationSearchResult>> {
        return try {
            val normalizedQuery = normalizeQuery(query)
            val cache = citySuggestionCacheDao.getCachedSuggestion(normalizedQuery)
            
            if (cache != null && !isCacheExpired(cache.cachedAt)) {
                val type = object : TypeToken<List<LocationSearchResult>>() {}.type
                val results = gson.fromJson<List<LocationSearchResult>>(cache.resultsJson, type)
                Result.Success(results)
            } else {
                Result.Error(Exception("Cache miss or expired"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun cacheSuggestions(query: String, results: List<LocationSearchResult>) {
        try {
            val normalizedQuery = normalizeQuery(query)
            val resultsJson = gson.toJson(results)
            val cache = CitySuggestionCacheEntity(
                normalizedQuery = normalizedQuery,
                resultsJson = resultsJson,
                cachedAt = System.currentTimeMillis()
            )
            citySuggestionCacheDao.insertCache(cache)
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }

    suspend fun clearExpiredCache() {
        val expirationTime = System.currentTimeMillis() - CACHE_TTL_MS
        citySuggestionCacheDao.deleteExpiredCache(expirationTime)
    }

    private fun isCacheExpired(cachedAt: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > CACHE_TTL_MS
    }
}