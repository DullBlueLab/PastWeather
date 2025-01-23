package com.dullbluelab.pastweather.data

import android.content.Context

private const val PEAK_DATA_FILE_NAME_TOP = "wdpeak_"

class PeakDataList(context: Context) : AssetCsv(context) {

    private val list = mutableListOf<PeakData>()

    override fun addLine(line: String) {
        list.add(PeakData.convert(line))
    }

    override fun checkMatches(line: String, month: Int, day: Int) {
        val data = PeakData.convert(line)
        if (data.month == month && data.day == day) {
            list.add(data)
        }
    }

    private fun clear() {
        list.clear()
    }

    fun getList(): List<PeakData> {
        return list.toList()
    }

    suspend fun loadMatches(point: String, month: Int, day: Int): List<PeakData> {
        if (list.isNotEmpty()) clear()
        loadMatchesCsv("$PEAK_DATA_FILE_NAME_TOP$point.csv", month, day)
        return list.toList()
    }
}
