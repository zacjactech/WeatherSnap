package com.weather.core.network

import com.weather.core.network.model.OpenMeteoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Open-Meteo public weather API.
 *
 * Base URL: https://api.open-meteo.com/
 * No API key required.
 *
 * Example call:
 *   GET /v1/forecast?latitude=51.5&longitude=-0.12&current_weather=true
 *       &hourly=temperature_2m,relativehumidity_2m,windspeed_10m
 */
interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,relativehumidity_2m,windspeed_10m,visibility,uv_index,cloudcover,dewpoint_2m",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("timezone") timezone: String = "auto"
    ): Response<OpenMeteoResponse>
}
