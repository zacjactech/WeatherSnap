package com.weather.core.file

import android.content.Context
import com.weather.core.common.DispatcherProvider
import com.weather.core.model.PhotoMetadata
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidFileStorageManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()
    
    private val testDispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val unconfined = testDispatcher
        override val default = testDispatcher
    }

    private lateinit var context: Context
    private lateinit var storageManager: AndroidFileStorageManager

    @Before
    fun setup() {
        context = mockk()
        every { context.filesDir } returns tempFolder.root
        storageManager = AndroidFileStorageManager(context, dispatcherProvider)
    }

    @Test
    fun `deletePhoto deletes file and returns true if exists`() = runTest(testDispatcher) {
        val file = tempFolder.newFile("test.jpg")
        assertTrue(file.exists())

        val result = storageManager.deletePhoto(file.absolutePath)
        assertTrue(result)
        assertFalse(file.exists())
    }
    
    @Test
    fun `deletePhoto returns false if file does not exist`() = runTest(testDispatcher) {
        val result = storageManager.deletePhoto("non_existent_path.jpg")
        assertFalse(result)
    }

    @Test
    fun `deleteDraftFiles deletes all associated files`() = runTest(testDispatcher) {
        val original = tempFolder.newFile("original.jpg")
        val compressed = tempFolder.newFile("compressed.jpg")
        val thumb = tempFolder.newFile("thumb.jpg")
        
        val metadata = PhotoMetadata(
            id = "1",
            filePath = compressed.absolutePath,
            originalFilePath = original.absolutePath,
            thumbnailFilePath = thumb.absolutePath,
            originalSizeBytes = 0,
            compressedSizeBytes = 0,
            width = 0,
            height = 0,
            capturedAt = 0
        )
        
        val result = storageManager.deleteDraftFiles(metadata)
        
        assertTrue(result)
        assertFalse(original.exists())
        assertFalse(compressed.exists())
        assertFalse(thumb.exists())
    }
}
