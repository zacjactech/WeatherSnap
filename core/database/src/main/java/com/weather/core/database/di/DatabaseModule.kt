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
import com.google.gson.Gson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

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
        .addMigrations(WeatherDatabase.MIGRATION_4_5)
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
