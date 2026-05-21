package com.weather.core.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.weather.core.common.DispatcherProvider
import com.weather.core.model.PhotoMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidFileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : FileStorageManager {

    private val photosDir: File by lazy {
        File(context.filesDir, "weather_snaps_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    override fun getPhotosDirectory(): File = photosDir

    override suspend fun saveAndCompressPhoto(bytes: ByteArray): CompressionResult = withContext(dispatcherProvider.io) {
        try {
            withTimeout(15_000) {
                if (bytes.isEmpty()) return@withTimeout CompressionResult.InvalidFile

                val uuid = UUID.randomUUID().toString()
                val rawFile = File(photosDir, "raw_$uuid.jpg")
                val tempCompressedFile = File(photosDir, "temp_snap_$uuid.jpg")
                val compressedFile = File(photosDir, "snap_$uuid.jpg")
                val tempThumbnailFile = File(photosDir, "temp_thumb_$uuid.jpg")
                val thumbnailFile = File(photosDir, "thumb_$uuid.jpg")

                // 1. Write raw camera bytes to storage
                FileOutputStream(rawFile).use { fos ->
                    fos.write(bytes)
                }

                // Get Exif rotation
                val exif = ExifInterface(rawFile.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }

                // 2. Compute sample size to avoid OOM memory allocations
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(rawFile.absolutePath, options)

                val srcWidth = options.outWidth
                val srcHeight = options.outHeight
                if (srcWidth <= 0 || srcHeight <= 0) {
                    return@withTimeout CompressionResult.InvalidFile
                }
                
                val maxDimension = 1920

                var sampleSize = 1
                if (srcWidth > maxDimension || srcHeight > maxDimension) {
                    val halfWidth = srcWidth / 2
                    val halfHeight = srcHeight / 2
                    while ((halfWidth / sampleSize) >= maxDimension && (halfHeight / sampleSize) >= maxDimension) {
                        sampleSize *= 2
                    }
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                }
                val bitmap = BitmapFactory.decodeFile(rawFile.absolutePath, decodeOptions) ?: return@withTimeout CompressionResult.InvalidFile

                // Apply EXIF rotation
                val rotatedBitmap = if (rotationDegrees != 0f) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees) }
                    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    if (rotated != bitmap) {
                        bitmap.recycle()
                    }
                    rotated
                } else {
                    bitmap
                }

                // 3. Perfect scale image to fit 1920 limits exactly
                val scaledBitmap = if (rotatedBitmap.width > maxDimension || rotatedBitmap.height > maxDimension) {
                    val ratio = Math.min(
                        maxDimension.toDouble() / rotatedBitmap.width,
                        maxDimension.toDouble() / rotatedBitmap.height
                    )
                    val newWidth = (rotatedBitmap.width * ratio).toInt()
                    val newHeight = (rotatedBitmap.height * ratio).toInt()
                    Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, true).also {
                        if (it != rotatedBitmap) {
                            rotatedBitmap.recycle()
                        }
                    }
                } else {
                    rotatedBitmap
                }

                // 4. Output compressed file to temp file
                FileOutputStream(tempCompressedFile).use { fos ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                }

                val finalWidth = scaledBitmap.width
                val finalHeight = scaledBitmap.height

                // 5. Generate thumbnail (128 max dimension)
                val thumbMax = 256
                val thumbRatio = Math.min(
                    thumbMax.toDouble() / scaledBitmap.width,
                    thumbMax.toDouble() / scaledBitmap.height
                )
                val thumbWidth = (scaledBitmap.width * thumbRatio).toInt().coerceAtLeast(1)
                val thumbHeight = (scaledBitmap.height * thumbRatio).toInt().coerceAtLeast(1)
                val thumbBitmap = Bitmap.createScaledBitmap(scaledBitmap, thumbWidth, thumbHeight, true)

                FileOutputStream(tempThumbnailFile).use { fos ->
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                }

                // Explicitly release memory
                scaledBitmap.recycle()
                if (thumbBitmap != scaledBitmap) {
                    thumbBitmap.recycle()
                }

                // Atomic rename
                if (!tempCompressedFile.renameTo(compressedFile)) {
                    return@withTimeout CompressionResult.CompressionFailure("Failed to atomically rename compressed file")
                }
                if (!tempThumbnailFile.renameTo(thumbnailFile)) {
                    return@withTimeout CompressionResult.CompressionFailure("Failed to atomically rename thumbnail file")
                }

                CompressionResult.Success(
                    originalFile = rawFile,
                    compressedFile = compressedFile,
                    thumbnailFile = thumbnailFile,
                    originalSizeBytes = rawFile.length(),
                    compressedSizeBytes = compressedFile.length(),
                    width = finalWidth,
                    height = finalHeight
                )
            }
        } catch (e: TimeoutCancellationException) {
            CompressionResult.Timeout
        } catch (e: Exception) {
            CompressionResult.CompressionFailure(e.message ?: "Unknown compression error", e)
        }
    }

    override suspend fun deletePhoto(filePath: String): Boolean = withContext(dispatcherProvider.io) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    override suspend fun deleteDraftFiles(photo: PhotoMetadata): Boolean = withContext(dispatcherProvider.io) {
        var success = true
        if (photo.originalFilePath != null) {
            val original = File(photo.originalFilePath!!)
            if (original.exists() && !original.delete()) success = false
        }
        val compressed = File(photo.filePath)
        if (compressed.exists() && !compressed.delete()) success = false
        
        if (photo.thumbnailFilePath != null) {
            val thumb = File(photo.thumbnailFilePath!!)
            if (thumb.exists() && !thumb.delete()) success = false
        }
        success
    }
}
