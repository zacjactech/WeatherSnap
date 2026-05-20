package com.weather.core.network

import com.weather.core.network.model.NetworkWeatherSnapRequest
import com.weather.core.network.model.NetworkWeatherTelemetry
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface WeatherSnapApi {

    @GET("weather")
    suspend fun getTelemetry(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<NetworkWeatherTelemetry>

    @POST("snaps")
    suspend fun uploadSnap(
        @Body request: NetworkWeatherSnapRequest
    ): Response<Unit>

    @Multipart
    @POST("snaps/upload-photo")
    suspend fun uploadSnapPhoto(
        @Part("snapId") snapId: RequestBody,
        @Part photoFile: MultipartBody.Part
    ): Response<Unit>
}
