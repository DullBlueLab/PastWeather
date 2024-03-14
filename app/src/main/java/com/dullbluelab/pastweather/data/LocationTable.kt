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

@Entity(tableName = "location_table")
data class LocationTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val code: String,
    val name: String,
    val loaded: Boolean
)

@Dao
interface LocationListDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: LocationTable)

    @Update
    suspend fun update(item: LocationTable)

    @Delete
    suspend fun delete(item: LocationTable)

    @Query("SELECT * FROM location_table")
    fun getAll(): Flow<List<LocationTable>>

    @Query("SELECT * FROM location_table WHERE code = :code")
    fun getItem(code: String): Flow<LocationTable>
}