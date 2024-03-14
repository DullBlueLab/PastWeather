package com.dullbluelab.pastweather.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_weather_table")
data class DailyWeatherTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val point: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val high: Double,
    val low: Double,
    val sky: String
)

@Dao
interface DailyWeatherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: DailyWeatherTable)

    @Update
    suspend fun update(item: DailyWeatherTable)

    @Delete
    suspend fun delete(item: DailyWeatherTable)

    @Query("SELECT * FROM daily_weather_table WHERE point = :point AND year = :year AND month = :month AND day = :day")
    fun getItem(point: String, year: Int, month: Int, day: Int): Flow<DailyWeatherTable?>

    @Query("DELETE FROM daily_weather_table")
    fun deleteAll()

    @Query("DELETE FROM daily_weather_table WHERE point = :point")
    suspend fun deleteAt(point: String)
}
