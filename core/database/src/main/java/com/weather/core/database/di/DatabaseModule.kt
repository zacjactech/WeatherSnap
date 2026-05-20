package com.weather.core.database.di

import android.content.Context
import androidx.room.Room
import com.weather.core.database.WeatherDatabase
import com.weather.core.database.dao.CitySuggestionCacheDao
import com.weather.core.database.dao.WeatherDraftDao
import com.weather.core.database.dao.WeatherSnapDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideWeatherDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather-snap-database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideWeatherSnapDao(database: WeatherDatabase): WeatherSnapDao {
        return database.weatherSnapDao()
    }

    @Provides
    @Singleton
    fun provideCitySuggestionCacheDao(database: WeatherDatabase): CitySuggestionCacheDao {
        return database.citySuggestionCacheDao()
    }

    @Provides
    @Singleton
    fun provideWeatherDraftDao(database: WeatherDatabase): WeatherDraftDao {
        return database.weatherDraftDao()
    }
}
