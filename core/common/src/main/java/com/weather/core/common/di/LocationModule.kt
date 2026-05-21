package com.weather.core.common.di

import com.weather.core.common.LocationProvider
import com.weather.core.common.LocationProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(
        locationProviderImpl: LocationProviderImpl,
    ): LocationProvider
}
