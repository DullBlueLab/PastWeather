package com.dullbluelab.pastweather.data

import android.content.Context

class WeatherDataList(context: Context) : AssetCsv(context) {

    private val list = mutableListOf<WeatherData>()

    override fun addLine(line: String) {

        list.add(WeatherData.convert(line))
    }

    override fun checkMatches(line: String, month: Int, day: Int) {
        val data = WeatherData.convert(line)
        if (data.month == month && data.day == day) {
            list.add(data)
        }
    }

    private fun clear() {
        list.clear()
    }

    fun getList(): List<WeatherData> {
        return list.toList()
    }

    suspend fun loadMatches(point: String, month: Int, day: Int): List<WeatherData> {
        if (list.isNotEmpty()) clear()
        loadMatchesCsv("wd_$point.csv", month, day)
        return list.toList()
    }
}
