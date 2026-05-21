package com.weather.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherSnapDao(): WeatherSnapDao
    abstract fun citySuggestionCacheDao(): CitySuggestionCacheDao
    abstract fun weatherDraftDao(): WeatherDraftDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weather_snaps ADD COLUMN photo_originalFilePath TEXT")
                db.execSQL("ALTER TABLE weather_snaps ADD COLUMN photo_thumbnailFilePath TEXT")
                db.execSQL("ALTER TABLE weather_snaps ADD COLUMN photo_originalSizeBytes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE weather_snaps ADD COLUMN photo_compressedSizeBytes INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
