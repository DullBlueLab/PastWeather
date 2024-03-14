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

@Entity(tableName = "average_weather_table")
data class AverageWeatherTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val point: String,
    val years: String,
    val month: Int,
    val day: Int,
    val high: Double,
    val low: Double
)

@Dao
interface AverageWeatherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: AverageWeatherTable)

    @Update
    suspend fun update(item: AverageWeatherTable)

    @Delete
    suspend fun delete(item: AverageWeatherTable)

    @Query("SELECT * FROM average_weather_table WHERE point = :point AND month = :month AND day = :day")
    fun getItems(point: String, month: Int, day: Int): Flow<List<AverageWeatherTable>>

    @Query("DELETE FROM average_weather_table")
    fun deleteAll()

    @Query("DELETE FROM average_weather_table WHERE point = :point")
    suspend fun deleteAt(point: String)
}
