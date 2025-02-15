package com.dullbluelab.pastweather.data

import android.content.Context

class AverageDataList(context: Context) : AssetCsv(context) {

    private val list = mutableListOf<AverageData>()

    override fun addLine(line: String) {
        list.add(AverageData.convert(line))
    }

    override fun checkMatches(line: String, month: Int, day: Int) {
        val data = AverageData.convert(line)
        if (data.month == month && data.day == day) {
            list.add(data)
        }
    }

    private fun clear() {
        list.clear()
    }

    fun getList(): List<AverageData> {
        return list.toList()
    }

    suspend fun loadMatches(point: String, month: Int, day: Int) : List<AverageData> {
        if (list.isNotEmpty()) clear()
        loadMatchesCsv("wdavg_$point.csv", month, day)
        return list.toList()
    }
}
