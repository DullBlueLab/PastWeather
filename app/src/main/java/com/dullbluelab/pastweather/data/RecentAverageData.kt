package com.dullbluelab.pastweather.data

private const val START_YEAR = 2004
private const val END_YEAR = 2023

class RecentAverageData {

    var highTemp: Double = 0.0
    var lowTemp: Double = 0.0

    fun calculate(weatherData: List<WeatherData>) {
        var high = 0.0
        var low = 0.0
        var count = 0

        weatherData.forEach { data ->
            if (data.year in START_YEAR..END_YEAR) {
                high += data.high
                low += data.low
                count ++
            }
        }
        if (count > 0) {
            highTemp = Math.round(high / count * 10) / 10.0
            lowTemp = Math.round(low / count * 10) / 10.0
        }
    }
}