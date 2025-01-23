package com.dullbluelab.pastweather.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DirectoryData"
private const val ASSET_NAME = "directory.json"

class DirectoryData(private val context: Context) {

    data class Table(
        val version: String,
        val update: String,
        val maxyear: String,
        val minyear: String,
        val points: List<LocationData>
    )

    var item: Table? = null

    suspend fun load() {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open(ASSET_NAME)
                val jsonText = inputStream.bufferedReader().use { it.readText() }

                item = Gson().fromJson(jsonText, Table::class.java)
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    fun getLocationList(): List<LocationData> = item?.points ?: listOf()

    fun getLocationItem(code: String): LocationData? {
        var result: LocationData? = null
        for (item in getLocationList()) {
            if (item.code == code) {
                result = item
                break
            }
        }
        return result
    }
}
