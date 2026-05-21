package com.weather.core.testing.repository

import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherSnap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWeatherSnapRepository : WeatherSnapRepository {
    private val snapsFlow = MutableStateFlow<List<WeatherSnap>>(emptyList())
    var throwErrorOnSync = false

    override fun getSnapsStream(): Flow<List<WeatherSnap>> = snapsFlow

    override fun getSnapByIdStream(id: String): Flow<WeatherSnap?> =
        snapsFlow.map { snaps -> snaps.find { it.id == id } }

    override suspend fun saveSnapDraft(snap: WeatherSnap) {
        snapsFlow.update { current ->
            val mutableList = current.toMutableList()
            val index = mutableList.indexOfFirst { it.id == snap.id }
            if (index != -1) {
                mutableList[index] = snap
            } else {
                mutableList.add(snap)
            }
            mutableList
        }
    }

    override suspend fun updateSnapSyncStatus(id: String, status: SyncStatus) {
        snapsFlow.update { current ->
            current.map {
                if (it.id == id) it.copy(status = status) else it
            }
        }
    }

    override suspend fun syncPendingSnaps() {
        if (throwErrorOnSync) throw Exception("Test Sync Error")
        snapsFlow.update { current ->
            current.map {
                if (it.status == SyncStatus.DRAFT || it.status == SyncStatus.FAILED) {
                    it.copy(status = SyncStatus.COMPLETED)
                } else {
                    it
                }
            }
        }
    }

    override suspend fun deleteSnap(id: String) {
        snapsFlow.update { current ->
            current.filterNot { it.id == id }
        }
    }

    override suspend fun getDiscardedSnaps(): List<WeatherSnap> {
        return snapsFlow.value.filter { it.status == SyncStatus.DISCARDED }
    }
}
