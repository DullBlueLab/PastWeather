package com.dullbluelab.pastweather.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val PEAK_DATA_FILE_NAME_TOP = "wdpeak_"

class PeakDataCsv(private val context: Context): CsvDataFile(context, PEAK_DATA_FILE_NAME_TOP) {
    class Table(
        val point: String,
        val month: Int,
        val day: Int,
        val years: List<Int>,
        val temps: List<Double>,
    ) {
        companion object {
            fun convert(line: String): Table {
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

                return Table(point, month, day, years.toList(), temps.toList())
            }
        }
    }

    suspend fun loadMatches(point: String, month: Int, day: Int): Table? {
        var matches: Table? = null

        withContext(Dispatchers.IO) {
            val name = "$PEAK_DATA_FILE_NAME_TOP$point.csv"
            val stream = File(context.filesDir, name).inputStream()
            val reader = stream.reader()
            var head = ""

            reader.forEachLine { line ->
                if (head.isEmpty()) head = line
                else {
                    val item = Table.convert(line)
                    if (item.month == month && item.day == day) {
                        matches = item
                    }
                }
            }
            stream.close()
        }

        return matches
    }
}