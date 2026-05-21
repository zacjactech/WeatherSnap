package com.weather.core.datastore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    // SettingsRepository has @Inject constructor and doesn't explicitly need a Provide method,
    // but we can bind it or leave it as is.
    // The DataStore is a context extension, so it's handled internally in SettingsRepository.
}
