package com.weather.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PhotoMetadata(
    val id: String,
    val filePath: String,
    val width: Int,
    val height: Int,
    val capturedAt: Long
)
