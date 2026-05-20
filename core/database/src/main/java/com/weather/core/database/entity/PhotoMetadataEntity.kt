package com.weather.core.database.entity

data class PhotoMetadataEntity(
    val photoId: String,
    val filePath: String,
    val width: Int,
    val height: Int,
    val capturedAt: Long
)
