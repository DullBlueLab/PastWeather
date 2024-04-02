package com.dullbluelab.pastweather.data

import android.content.res.Resources
import android.util.Log
import com.dullbluelab.pastweather.R
import com.google.gson.Gson

private const val TAG = "DirectoryData"

class DirectoryData {

    data class Table(
        val version: String,
        val update: String,
        val maxyear: String,
        val minyear: String,
        val points: List<PointTable>
    )

    data class PointTable(
        val code: String,
        val name: String
    )

    fun load(resources: Resources): Table? {
        var data: Table? = null
        try {
            val inputStream = resources.openRawResource(R.raw.directory)
            val jsonText = inputStream.bufferedReader().use { it.readText() }

            data = Gson().fromJson(jsonText, Table::class.java)
        }
        catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
        return data
    }
}
