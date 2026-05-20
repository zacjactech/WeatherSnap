package com.weather.core.model

/**
 * Domain-level weather condition codes, mapped from WMO codes via Open-Meteo.
 */
enum class WeatherCondition {
    CLEAR,
    CLOUDY,
    FOG,
    RAIN,
    SNOW,
    THUNDERSTORM,
    WINDY,
    UNKNOWN
}
