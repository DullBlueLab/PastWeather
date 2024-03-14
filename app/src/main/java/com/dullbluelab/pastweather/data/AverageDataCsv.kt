package com.dullbluelab.pastweather.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

private const val BASE_URL = "https://dullbluelab.nobushi.jp"
private const val DATA_PATH = "/pastweather/data/"
private const val AVERAGE_DATA_FILE_NAME_TOP = "wdavg_"

class AverageDataCsv {

    data class Table(
        val years: String,
        val month: Int,
        val day: Int,
        val high: Double,
        val low: Double
    )

    fun updateAverageData(
        point: String,
        repository: WeatherRepository,
        progress: (Int) -> Unit,
        success: () -> Unit,
        cancelFlag: () -> Boolean,
        failed: (String) -> Unit
    ) {
        var percent = 0
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            try {
                val url = URL("$BASE_URL$DATA_PATH$AVERAGE_DATA_FILE_NAME_TOP$point.csv")
                val reader = withContext(Dispatchers.IO) {
                    url.openConnection().getInputStream()
                }.reader()
                val lines = reader.readLines()

                for (count in 1..< lines.size) {
                    storeToDatabase(point, repository, convertLine(lines[count]))

                    val value = count * 50 / lines.size + 50
                    if (percent != value) {
                        progress(value)
                        percent = value
                    }
                    if (cancelFlag()) throw Exception("cancel")
                }
                success()
            } catch (e: Exception) {
                failed("${e.message}")
            }
        }
    }

    private suspend fun storeToDatabase(
        point: String,
        repository: WeatherRepository,
        table: Table
    ) {
        val item = AverageWeatherTable(
            0, point, table.years, table.month, table.day,
            table.high, table.low
        )
        repository.insertAverage(item)

    }

    private fun convertLine(line: String): Table {
        val items = line.split(",")
        val year = items[0] + "s"
        val month = items[1].toInt()
        val day = items[2].toInt()
        val high = items[3].toDouble()
        val low = items[4].toDouble()

        return Table(year, month, day, high, low)
    }
}