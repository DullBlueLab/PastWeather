package com.dullbluelab.pastweather.data

class PeakData(
    val point: String = "",
    val month: Int = 0,
    val day: Int = 0,
    val years: List<Int> = listOf(),
    val temps: List<Double> = listOf()
) {
    companion object {
        fun convert(line: String): PeakData {
            val items = line.split(",")
            val point = items[0]
            val month = items[1].toInt()
            val day = items[2].toInt()

            val years = mutableListOf<Int>()
            val temps = mutableListOf<Double>()
            var cnt = 3
            for (index in 0..3) {
                years.add(items[cnt].toInt())
                cnt ++
                temps.add(items[cnt].toDouble())
                cnt ++
            }

            return PeakData(point, month, day, years.toList(), temps.toList())
        }
    }
}
