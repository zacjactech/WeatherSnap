package com.weather.core.network.di

import com.weather.core.domain.repository.WeatherRepository
import com.weather.core.domain.repository.WeatherSnapRepository
import com.weather.core.network.repository.WeatherRepositoryImpl
import com.weather.core.network.repository.WeatherSnapRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindWeatherSnapRepository(
        impl: WeatherSnapRepositoryImpl
    ): WeatherSnapRepository
}
