package com.weather.core.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface LocationProvider {
    fun fetchCurrentLocation(): Flow<DeviceLocation>
}

class LocationProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override fun fetchCurrentLocation(): Flow<DeviceLocation> = callbackFlow {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(
                        DeviceLocation(
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    )
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose { fusedLocationProviderClient.removeLocationUpdates(locationCallback) }
    }
}
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
)