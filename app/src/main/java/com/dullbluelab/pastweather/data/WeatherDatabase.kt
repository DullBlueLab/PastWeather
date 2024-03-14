package com.dullbluelab.pastweather.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyWeatherTable::class, LocationTable::class, AverageWeatherTable::class], version = 1, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun dailyWeatherDao(): DailyWeatherDao
    abstract fun locationListDao(): LocationListDao
    abstract fun averageWeatherDao(): AverageWeatherDao

    companion object {
        @Volatile
        private var Instance: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, WeatherDatabase::class.java, "weather_database")
                    .build()
            }
        }
    }
}