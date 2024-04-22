package com.dullbluelab.pastweather.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

private const val BASE_URL = "https://dullbluelab.nobushi.jp"
private const val DATA_PATH = "/pastweather/data/"

open class CsvDataFile(
    private val context: Context,
    private val fileNameTop: String
) {

    fun download(point: String) {
        val name = "$fileNameTop$point.csv"
        val url = URL("$BASE_URL$DATA_PATH$name")
        val input = url.openConnection().getInputStream()
        val output = File(context.filesDir, name).outputStream()

        output.write(input.readBytes())

        output.close()
        input.close()
    }

    fun deleteFile(point: String) {
        val name = "$fileNameTop$point.csv"
        File(context.filesDir, name).delete()
    }
}