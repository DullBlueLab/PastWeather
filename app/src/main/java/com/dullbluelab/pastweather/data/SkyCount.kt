package com.dullbluelab.pastweather.data

private const val START_YEAR = 2004
private const val END_YEAR = 2023
private const val SUNNY_CHAR = "晴"
private const val RAINY_CHAR = "雨"
private const val CLEAR_SKIES_STRING = "快晴"

class SkyCount {
    var sunny: Int = 0
    var rainy: Int = 0
    var clearSkies: Int = 0

    private fun clear() {
        sunny = 0
        rainy = 0
        clearSkies = 0
    }

    fun calculate(weatherList: List<WeatherDataCsv.Table>) {
        clear()
        weatherList.forEach { data ->
            if (data.year in START_YEAR..END_YEAR) {
                if (data.sky.contains(SUNNY_CHAR)) {
                    sunny ++
                }
                if (data.sky.contains(RAINY_CHAR)) {
                    rainy ++
                }
                if (data.sky == CLEAR_SKIES_STRING) {
                    clearSkies ++
                }
            }
        }
    }

}