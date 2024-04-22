package com.dullbluelab.pastweather.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val WEATHER_DATA_FILE_NAME_TOP = "wd_"

class WeatherDataCsv(private val context: Context)
    :  CsvDataFile(context, WEATHER_DATA_FILE_NAME_TOP)
{

    class Table(
        val year: Int,
        val month: Int,
        val day: Int,
        val high: Double,
        val low: Double,
        val sky: String,
    ) {
        companion object {
            fun convert(line: String): Table {
                val items = line.split(",")
                val year = items[0].toInt()
                val month = items[1].toInt()
                val day = items[2].toInt()
                val high = items[3].toDouble()
                val low = items[4].toDouble()
                val sky = items[5]

                return Table(year, month, day, high, low, sky)
            }
        }
    }

    suspend fun loadMatches(point: String, month: Int, day: Int): List<Table> {
        val matches = mutableListOf<Table>()

        withContext(Dispatchers.IO) {
            val name = "$WEATHER_DATA_FILE_NAME_TOP$point.csv"
            val stream = File(context.filesDir, name).inputStream()
            val reader = stream.reader()
            var head = ""

            reader.forEachLine { line ->
                if (head.isEmpty()) head = line
                else {
                    val item = Table.convert(line)
                    if (item.month == month && item.day == day) {
                        matches.add(item)
                    }
                }
            }
            stream.close()
        }

        return matches.toList()
    }
}
