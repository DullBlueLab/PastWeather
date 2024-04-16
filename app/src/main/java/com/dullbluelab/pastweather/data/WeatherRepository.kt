package com.dullbluelab.pastweather.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherRepository(database: WeatherDatabase, context: Context) {
    private val dailyWeatherDao: DailyWeatherDao = database.dailyWeatherDao()
    private val averageWeatherDao: AverageWeatherDao = database.averageWeatherDao()
    private val locationDao: LocationListDao = database.locationListDao()

    private val weatherData: WeatherDataCsv = WeatherDataCsv(context)
    private val averageData: AverageDataCsv = AverageDataCsv(context)

    var weatherList: List<WeatherDataCsv.Table> = listOf()
    var averageList: List<AverageDataCsv.Table> = listOf()

    suspend fun download(point: String) {
        weatherData.download(point)
        averageData.download(point)
        updateDownloadFlag(point, true)

    }

    suspend fun loadData(point: String, month: Int, day: Int) {
        weatherList = weatherData.loadMatches(point, month, day)
        averageList = averageData.loadMatches(point, month, day)
    }

    fun getWeatherItem(year: Int): WeatherDataCsv.Table? {
        var result: WeatherDataCsv.Table? = null
        for (item in weatherList) {
            if (item.year == year) {
                result = item
                break
            }
        }
        return result
    }

    suspend fun deleteAt(point: String) {
        averageData.deleteFile(point)
        weatherData.deleteFile(point)
        updateDownloadFlag(point, false)
    }

    private suspend fun insertLocation(item: LocationTable) = locationDao.insert(item)
    private suspend fun updateLocation(item: LocationTable) = locationDao.update(item)
    fun getAllLocation(): Flow<List<LocationTable>> = locationDao.getAll()
    private fun getLocationItem(code: String): Flow<LocationTable?> = locationDao.getItem(code)

    fun clearLocationList() { locationDao.deleteAll() }

    private fun updateDownloadFlag(code: String, flag: Boolean) {
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

    suspend fun updateLocationList(tables: List<DirectoryData.PointTable>) {
        tables.forEach { src ->
            val item = LocationTable(0, src.code, src.name, false)
            insertLocation(item)
        }
    }

    suspend fun cleanupPrevData() {
        withContext(Dispatchers.Default) {
            dailyWeatherDao.deleteAll()
            averageWeatherDao.deleteAll()
            locationDao.deleteAll()
        }
    }
}
