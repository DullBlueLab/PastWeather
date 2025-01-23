package com.dullbluelab.pastweather.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AssetCsv(private val context: Context) {

    abstract fun addLine(line: String)

    /*
    suspend fun loadCsv(fileName: String) {
        withContext(Dispatchers.IO) {
            val assetManager = context.assets
            val stream = assetManager.open(fileName)
            val reader = stream.reader()

            var head = ""
            reader.forEachLine { line ->
                if (head.isEmpty()) head = line
                else {
                    addLine(line)
                }
            }
        }
    }
     */

    abstract fun checkMatches(line: String, month: Int, day: Int)

    suspend fun loadMatchesCsv(fileName: String, month: Int, day: Int) {
        withContext(Dispatchers.IO) {
            val assetManager = context.assets
            val stream = assetManager.open(fileName)
            val reader = stream.reader()

            var head = ""
            reader.forEachLine { line ->
                if (head.isEmpty()) head = line
                else {
                    checkMatches(line, month, day)
                }
            }
        }
    }
}