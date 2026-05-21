package com.weather.core.network.model

import com.google.gson.annotations.SerializedName

/**
 * Root response from Open-Meteo v1/forecast endpoint.
 * https://open-meteo.com/en/docs
 */
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("current_weather")
    val currentWeather: CurrentWeatherDto? = null,
    val hourly: HourlyDataDto? = null,
    val daily: DailyDataDto? = null
)

/**
 * Current weather block returned when current_weather=true is set.
 */
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
    @SerializedName("is_day")
    val isDay: Int,
    val time: String
)

/**
 * Hourly data arrays — first index corresponds to the current hour.
 */
data class HourlyDataDto(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m")
    val temperature2m: List<Double> = emptyList(),
    @SerializedName("relativehumidity_2m")
    val relativehumidity2m: List<Int> = emptyList(),
    @SerializedName("windspeed_10m")
    val windspeed10m: List<Double> = emptyList(),
    @SerializedName("visibility")
    val visibility: List<Double> = emptyList(),
    @SerializedName("uv_index")
    val uvIndex: List<Double> = emptyList(),
    @SerializedName("cloudcover")
    val cloudcover: List<Int> = emptyList(),
    @SerializedName("dewpoint_2m")
    val dewpoint2m: List<Double> = emptyList(),
    @SerializedName("surface_pressure")
    val surfacePressure: List<Double> = emptyList()
)

/**
 * Daily data block providing actual high and low temperatures for the current day.
 */
data class DailyDataDto(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double> = emptyList()
)
