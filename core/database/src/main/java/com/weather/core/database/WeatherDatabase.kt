package com.weather.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.weather.core.database.dao.CitySuggestionCacheDao
import com.weather.core.database.dao.WeatherDraftDao
import com.weather.core.database.dao.WeatherSnapDao
import com.weather.core.database.entity.CitySuggestionCacheEntity
import com.weather.core.database.entity.WeatherDraftEntity
import com.weather.core.database.entity.WeatherSnapEntity

@Database(
    entities = [
        WeatherSnapEntity::class,
        CitySuggestionCacheEntity::class,
        WeatherDraftEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherSnapDao(): WeatherSnapDao
    abstract fun citySuggestionCacheDao(): CitySuggestionCacheDao
    abstract fun weatherDraftDao(): WeatherDraftDao
}
