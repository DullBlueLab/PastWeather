package com.dullbluelab.pastweather.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val database: WeatherDatabase, private val context: Context) {
    private val directory: DirectoryData = DirectoryData(context)

    private val weatherList: WeatherDataList = WeatherDataList(context)
    private val averageList: AverageDataList = AverageDataList(context)
    private val peakDataList: PeakDataList = PeakDataList(context)
    val recentAverage: RecentAverageData = RecentAverageData()
    val skyCount: SkyCount = SkyCount()

    fun getWeatherData(): List<WeatherData> = weatherList.getList()
    fun getAverageData(): List<AverageData> = averageList.getList()
    fun getPeakData(): List<PeakData> = peakDataList.getList()

    suspend fun loadMatches(point: String, month: Int, day: Int) {
        val data = weatherList.loadMatches(point, month, day)
        averageList.loadMatches(point, month, day)
        peakDataList.loadMatches(point, month, day)

        recentAverage.calculate(data)
        skyCount.calculate(data)
    }

    fun getWeatherItem(year: Int): WeatherData? {
        var result: WeatherData? = null
        for (item in getWeatherData()) {
            if (item.year == year) {
                result = item
                break
            }
        }
        return result
    }

    fun getAllLocation(): List<LocationData> = directory.getLocationList()
    fun getLocationItem(code: String): LocationData? = directory.getLocationItem(code)

    suspend fun loadDirectory() : DirectoryData.Table? {
        directory.load()
        return directory.item
    }

    fun getDirectory(): DirectoryData.Table? = directory.item

    suspend fun clearRecentData() {
        clearDatabase()
        deleteAllLocalFiles()
    }

    private suspend fun clearDatabase() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }

    private suspend fun deleteAllLocalFiles() {
        withContext(Dispatchers.IO) {
            context.filesDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }
}
