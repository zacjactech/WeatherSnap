package com.weather.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationSearchResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)
