package com.dullbluelab.pastweather.data

class AverageData(
    val point: String = "",
    val years: String = "",
    val month: Int = 0,
    val day: Int = 0,
    val high: Double = 0.0,
    val low: Double = 0.0
) {
    companion object {
        fun convert(line: String): AverageData {
            val items = line.split(",")
            val point = items[0]
            val year = items[1] + "s"
            val month = items[2].toInt()
            val day = items[3].toInt()
            val high = items[4].toDouble()
            val low = items[5].toDouble()

            return AverageData(point, year, month, day, high, low)
        }
    }
}
