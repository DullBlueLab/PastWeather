package com.dullbluelab.pastweather.data

class WeatherData(
    val point: String = "",
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val sky: String = ""
) {
    companion object {
        fun convert(line: String): WeatherData {
            val items = line.split(",")
            val point = items[0]
            val year = items[1].toInt()
            val month = items[2].toInt()
            val day = items[3].toInt()
            val high = items[4].toDouble()
            val low = items[5].toDouble()
            val sky = items[6]

            return WeatherData(point, year, month, day, high, low, sky)
        }
    }
}
