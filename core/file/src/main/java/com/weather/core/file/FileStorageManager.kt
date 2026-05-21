package com.weather.core.file

import com.weather.core.model.PhotoMetadata
import java.io.File

sealed class CompressionResult {
    data class Success(
        val originalFile: File,
        val compressedFile: File,
        val thumbnailFile: File,
        val originalSizeBytes: Long,
        val compressedSizeBytes: Long,
        val width: Int,
        val height: Int
    ) : CompressionResult()
    data class PartialFailure(val message: String) : CompressionResult()
    data class CompressionFailure(val message: String, val cause: Throwable? = null) : CompressionResult()
    object InvalidFile : CompressionResult()
    object Timeout : CompressionResult()
}

interface FileStorageManager {
    /**
     * Takes raw camera snapshot bytes, downscales/compresses the pixels on a background thread,
     * and saves it to a secure, private sandbox folder.
     */
    suspend fun saveAndCompressPhoto(bytes: ByteArray): CompressionResult

    /**
     * Deletes a captured photo file from storage.
     */
    suspend fun deletePhoto(filePath: String): Boolean

    /**
     * Safely cleans up all generated files associated with a draft.
     */
    suspend fun deleteDraftFiles(photo: PhotoMetadata): Boolean

    /**
     * Returns the file directory containing snapped photo archives.
     */
    fun getPhotosDirectory(): File
}
