package com.weather.core.testing.file

import com.weather.core.file.CompressionResult
import com.weather.core.file.FileStorageManager
import com.weather.core.model.PhotoMetadata
import java.io.File

class FakeFileStorageManager : FileStorageManager {
    var compressionResultToReturn: CompressionResult? = null
    val deletedFiles = mutableListOf<String>()

    override suspend fun saveAndCompressPhoto(bytes: ByteArray): CompressionResult {
        return compressionResultToReturn ?: CompressionResult.InvalidFile
    }

    override suspend fun deletePhoto(filePath: String): Boolean {
        deletedFiles.add(filePath)
        return true
    }

    override suspend fun deleteDraftFiles(photo: PhotoMetadata): Boolean {
        photo.filePath?.let { deletedFiles.add(it) }
        photo.thumbnailFilePath?.let { deletedFiles.add(it) }
        return true
    }

    override fun getPhotosDirectory(): File {
        return File("test_photos_dir")
    }
}
