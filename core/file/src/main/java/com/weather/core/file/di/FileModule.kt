package com.weather.core.file.di

import com.weather.core.file.AndroidFileStorageManager
import com.weather.core.file.FileStorageManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileModule {

    @Binds
    @Singleton
    abstract fun bindFileStorageManager(
        impl: AndroidFileStorageManager
    ): FileStorageManager
}
