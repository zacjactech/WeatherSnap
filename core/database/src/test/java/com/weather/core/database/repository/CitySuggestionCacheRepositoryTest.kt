package com.weather.core.database.repository

import com.weather.core.common.DispatcherProvider
import com.weather.core.common.Result
import com.weather.core.database.dao.CitySuggestionCacheDao
import com.weather.core.database.entity.CitySuggestionCacheEntity
import com.weather.core.model.LocationSearchResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CitySuggestionCacheRepositoryTest {

    private lateinit var dao: CitySuggestionCacheDao
    private lateinit var json: Json
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var repository: CitySuggestionCacheRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        json = Json { ignoreUnknownKeys = true }
        dispatcherProvider = object : DispatcherProvider {
            override val main: CoroutineDispatcher = testDispatcher
            override val io: CoroutineDispatcher = testDispatcher
            override val default: CoroutineDispatcher = testDispatcher
            override val unconfined: CoroutineDispatcher = testDispatcher
        }

        repository = CitySuggestionCacheRepository(dao, dispatcherProvider, json)
    }

    @Test
    fun `normalizeQuery converts to lowercase and trims spaces`() {
        val input = "  SeAttle   Wa  "
        val expected = "seattle wa"
        val actual = repository.normalizeQuery(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `getCachedSuggestions returns success if cache exists and is not expired`() = runTest(testDispatcher) {
        val query = "seattle"
        val results = listOf(
            LocationSearchResult(1, "Seattle", 47.6062, -122.3321, "USA", "Washington")
        )
        val cache = CitySuggestionCacheEntity(
            normalizedQuery = query,
            resultsJson = json.encodeToString(results),
            cachedAt = System.currentTimeMillis() - 5000 // 5 seconds ago
        )

        coEvery { dao.getCachedSuggestion(query) } returns cache

        val result = repository.getCachedSuggestions(query)

        assertTrue("Expected Success but got $result", result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("Seattle", data[0].name)
    }

    @Test
    fun `getCachedSuggestions returns error if cache is expired`() = runTest(testDispatcher) {
        val query = "seattle"
        val cache = CitySuggestionCacheEntity(
            normalizedQuery = query,
            resultsJson = "[]",
            cachedAt = System.currentTimeMillis() - (40 * 60 * 1000L) // 40 minutes ago (expired)
        )

        coEvery { dao.getCachedSuggestion(query) } returns cache

        val result = repository.getCachedSuggestions(query)

        assertTrue("Expected Error but got $result", result is Result.Error)
        val errorMsg = (result as Result.Error).exception.message
        assertTrue(errorMsg?.contains("expired") == true)
    }
}
