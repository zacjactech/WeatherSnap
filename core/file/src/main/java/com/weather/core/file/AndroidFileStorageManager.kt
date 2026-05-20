package com.weather.core.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.weather.core.common.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
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

    override suspend fun saveAndCompressPhoto(bytes: ByteArray): File = withContext(dispatcherProvider.io) {
        val rawFile = File(photosDir, "raw_${UUID.randomUUID()}.jpg")
        val compressedFile = File(photosDir, "snap_${UUID.randomUUID()}.jpg")

        // 1. Write raw camera bytes to temp storage
        FileOutputStream(rawFile).use { fos ->
            fos.write(bytes)
        }

        // 2. Compute sample size to avoid OOM memory allocations
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(rawFile.absolutePath, options)

        val srcWidth = options.outWidth
        val srcHeight = options.outHeight
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
        val bitmap = BitmapFactory.decodeFile(rawFile.absolutePath, decodeOptions)

        // 3. Perfect scale image to fit 1920 limits exactly
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = Math.min(
                maxDimension.toDouble() / bitmap.width,
                maxDimension.toDouble() / bitmap.height
            )
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true).also {
                if (it != bitmap) {
                    bitmap.recycle()
                }
            }
        } else {
            bitmap
        }

        // 4. Output compressed file to sandbox cache folder
        FileOutputStream(compressedFile).use { fos ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
        }

        // 5. Explicitly release memory allocations and temporary artifacts
        scaledBitmap.recycle()
        if (rawFile.exists()) {
            rawFile.delete()
        }

        compressedFile
    }

    override suspend fun deletePhoto(filePath: String): Boolean = withContext(dispatcherProvider.io) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
