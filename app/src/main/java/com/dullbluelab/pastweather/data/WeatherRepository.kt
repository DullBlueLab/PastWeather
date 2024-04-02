package com.dullbluelab.pastweather.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface WeatherRepository {
    fun getTableStream(point: String, year: Int, month: Int, day: Int): Flow<DailyWeatherTable?>
    suspend fun insertTable(table: DailyWeatherTable)
    suspend fun updateTable(table: DailyWeatherTable)
    suspend fun deleteTable(table: DailyWeatherTable)

    suspend fun insertLocation(item: LocationTable)
    suspend fun updateLocation(item: LocationTable)
    suspend fun deleteLocation(item: LocationTable)
    fun getAllLocation(): Flow<List<LocationTable>>
    fun getLocationItem(code: String): Flow<LocationTable?>

    fun getAverageStream(point: String, month: Int, day: Int): Flow<List<AverageWeatherTable>>
    suspend fun insertAverage(table: AverageWeatherTable)
    suspend fun updateAverage(table: AverageWeatherTable)
    suspend fun deleteAverage(table: AverageWeatherTable)

    suspend fun deleteAt(code: String)
    suspend fun clearAllTables()
    suspend fun clearDailyWeatherTables()

    suspend fun clearLocationList()

    fun downloadWeatherData(
        pointCode: String,
        progress: (Int) -> Unit,
        cancelFlag: () -> Boolean,
        failed: (String) -> Unit = {},
        success: () -> Unit = {},
    )

    fun deleteDataAt(code: String, success: () -> Unit)
    fun updateDownloadFlag(item: LocationTable, flag: Boolean)
    fun updateDownloadFlag(code: String, flag: Boolean)
    suspend fun updateLocationList(tables: List<DirectoryData.PointTable>)
}

class OfflineWeatherRepository(
    private val database: WeatherDatabase
) : WeatherRepository {
    private val weatherDao: DailyWeatherDao = database.dailyWeatherDao()
    private val locationDao: LocationListDao = database.locationListDao()
    private val averageDao: AverageWeatherDao = database.averageWeatherDao()

    private val weatherDataCsv: WeatherDataCsv = WeatherDataCsv()
    private val averageDataCsv: AverageDataCsv = AverageDataCsv()

    override fun getTableStream(point: String, year: Int, month: Int, day: Int)
    : Flow<DailyWeatherTable?> = weatherDao.getItem(point, year, month, day)

    override suspend fun insertTable(table: DailyWeatherTable) = weatherDao.insert(table)
    override suspend fun updateTable(table: DailyWeatherTable) = weatherDao.update(table)
    override suspend fun deleteTable(table: DailyWeatherTable) = weatherDao.delete(table)

    override suspend fun insertLocation(item: LocationTable) = locationDao.insert(item)
    override suspend fun updateLocation(item: LocationTable) = locationDao.update(item)
    override suspend fun deleteLocation(item: LocationTable) = locationDao.delete(item)
    override fun getAllLocation(): Flow<List<LocationTable>> = locationDao.getAll()
    override fun getLocationItem(code: String): Flow<LocationTable?> = locationDao.getItem(code)

    override fun getAverageStream(point: String, month: Int, day: Int): Flow<List<AverageWeatherTable>>
    = averageDao.getItems(point, month, day)

    override suspend fun insertAverage(table: AverageWeatherTable) = averageDao.insert(table)
    override suspend fun updateAverage(table: AverageWeatherTable) = averageDao.update(table)
    override suspend fun deleteAverage(table: AverageWeatherTable) = averageDao.delete(table)

    override suspend fun deleteAt(code: String) {
        averageDao.deleteAt(code)
        weatherDao.deleteAt(code)
    }
    override suspend fun clearAllTables() { database.clearAllTables() }
    override suspend fun clearDailyWeatherTables() { weatherDao.deleteAll() }
    override suspend fun clearLocationList() { locationDao.deleteAll() }

    override fun downloadWeatherData(
        pointCode: String,
        progress: (Int) -> Unit,
        cancelFlag: () -> Boolean,
        failed: (String) -> Unit,
        success: () -> Unit,
    ) {
        val repository = this
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            weatherDataCsv.updateWeatherData(
                point = pointCode,
                repository = repository,
                progress = { per -> progress(per) },
                success = {
                    averageDataCsv.updateAverageData(
                        point = pointCode,
                        repository = repository,
                        progress = { per -> progress(per) },
                        success = {
                            updateDownloadFlag(pointCode, true)
                            success()
                        },
                        cancelFlag = { cancelFlag() },
                        failed = { message ->
                            deleteDataAt(pointCode, {})
                            failed(message)
                        }
                    )
                },
                cancelFlag = { cancelFlag() },
                failed = { message ->
                    deleteDataAt(pointCode, {})
                    failed(message)
                }
            )
        }
    }

    override fun deleteDataAt(code: String, success: () -> Unit) {
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            deleteAt(code)
            val stream = getLocationItem(code)
            stream.collect { item ->
                item?.let {
                    val newItem = item.copy(loaded = false)
                    updateLocation(newItem)
                    success()
                }
                cancel()
            }
        }
    }

    override fun updateDownloadFlag(item: LocationTable, flag: Boolean) {
        val newItem = item.copy(loaded = flag)
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            updateLocation(newItem)
        }
    }

    override fun updateDownloadFlag(code: String, flag: Boolean) {
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            val stream = getLocationItem(code)
            stream.collect { item ->
                item?.let {
                    val newItem = item.copy(loaded = flag)
                    updateLocation(newItem)
                }
                cancel()
            }
        }
    }

    override suspend fun updateLocationList(tables: List<DirectoryData.PointTable>) {
        tables.forEach { src ->
            val item = LocationTable(0, src.code, src.name, false)
            insertLocation(item)
        }
    }

}
