package com.weather.core.domain.repository

import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherSnap
import kotlinx.coroutines.flow.Flow

interface WeatherSnapRepository {
    /**
     * Continuous stream of all captured snaps sorted by date.
     */
    fun getSnapsStream(): Flow<List<WeatherSnap>>

    /**
     * Stream of a specific snap record.
     */
    fun getSnapByIdStream(id: String): Flow<WeatherSnap?>

    /**
     * Saves a weather snap record as a local persistent Room draft before remote transmission.
     */
    suspend fun saveSnapDraft(snap: WeatherSnap)

    /**
     * Atomically changes sync transmission status.
     */
    suspend fun updateSnapSyncStatus(id: String, status: SyncStatus)

    /**
     * Iterates over all pending failed/draft drafts and attempts secure remote upload synchronization.
     */
    suspend fun syncPendingSnaps()

    /**
     * Deletes a local draft or synchronized snap record.
     */
    suspend fun deleteSnap(id: String)
}
