package com.weather.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PhotoMetadata(
    val id: String,
    val filePath: String, // Compressed path
    val originalFilePath: String?,
    val thumbnailFilePath: String?,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val width: Int,
    val height: Int,
    val capturedAt: Long
)
