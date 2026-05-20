package com.weather.core.database.di

import com.weather.core.database.repository.CitySuggestionCacheRepository
import com.weather.core.database.repository.WeatherDraftRepositoryImpl
import com.weather.core.database.repository.WeatherSnapRepositoryImpl
import com.weather.core.domain.repository.WeatherDraftRepository
import com.weather.core.domain.repository.WeatherSnapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherSnapRepository(
        impl: WeatherSnapRepositoryImpl
    ): WeatherSnapRepository

    @Binds
    @Singleton
    abstract fun bindWeatherDraftRepository(
        impl: WeatherDraftRepositoryImpl
    ): WeatherDraftRepository
}