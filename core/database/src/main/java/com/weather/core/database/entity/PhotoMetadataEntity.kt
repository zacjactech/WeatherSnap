package com.weather.core.database.entity

data class PhotoMetadataEntity(
    val photoId: String,
    val filePath: String,
    val originalFilePath: String? = null,
    val thumbnailFilePath: String? = null,
    val originalSizeBytes: Long = 0L,
    val compressedSizeBytes: Long = 0L,
    val width: Int,
    val height: Int,
    val capturedAt: Long
)
