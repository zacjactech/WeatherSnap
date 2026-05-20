package com.weather.core.file

import java.io.File

interface FileStorageManager {
    /**
     * Takes raw camera snapshot bytes, downscales/compresses the pixels on a background thread,
     * and saves it to a secure, private sandbox folder.
     */
    suspend fun saveAndCompressPhoto(bytes: ByteArray): File

    /**
     * Deletes a captured photo file from storage.
     */
    suspend fun deletePhoto(filePath: String): Boolean

    /**
     * Returns the file directory containing snapped photo archives.
     */
    fun getPhotosDirectory(): File
}
