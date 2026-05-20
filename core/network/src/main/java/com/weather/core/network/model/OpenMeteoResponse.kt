package com.weather.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root response from Open-Meteo v1/forecast endpoint.
 * https://open-meteo.com/en/docs
 */
@Serializable
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerialName("current_weather")
    val currentWeather: CurrentWeatherDto? = null,
    val hourly: HourlyDataDto? = null
)

/**
 * Current weather block returned when current_weather=true is set.
 */
@Serializable
data class CurrentWeatherDto(
    /** Temperature in Celsius (temperature_unit=celsius). */
    val temperature: Double,
    /** Wind speed in km/h (wind_speed_unit=kmh). */
    val windspeed: Double,
    val winddirection: Double,
    /**
     * WMO Weather interpretation code.
     * 0=Clear, 1-3=Cloudy, 45/48=Fog, 51-67=Rain,
     * 71-77=Snow, 80-82=Showers, 85-86=SnowShowers, 95=Thunderstorm
     */
    val weathercode: Int,
    @SerialName("is_day")
    val isDay: Int,
    val time: String
)

/**
 * Hourly data arrays — first index corresponds to the current hour.
 */
@Serializable
data class HourlyDataDto(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m")
    val temperature2m: List<Double> = emptyList(),
    @SerialName("relativehumidity_2m")
    val relativehumidity2m: List<Int> = emptyList(),
    @SerialName("windspeed_10m")
    val windspeed10m: List<Double> = emptyList()
)
