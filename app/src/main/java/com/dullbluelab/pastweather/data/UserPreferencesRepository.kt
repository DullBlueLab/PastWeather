package com.dullbluelab.pastweather.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

private const val INITIAL_POINT = "tky"
private const val INITIAL_YEAR = 2024
private const val INITIAL_VERSION = "0"

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SELECT_POINT = stringPreferencesKey("select_point")
        val SELECT_YEAR = intPreferencesKey("select_year")
        val DATA_VERSION = stringPreferencesKey("data_version")
        const val TAG = "UserPreferences"
    }

    val data: Flow<UserPreferencesData> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            }
        }
        .map { preferences ->
            UserPreferencesData(
                selectPoint = preferences[SELECT_POINT] ?: INITIAL_POINT,
                selectYear =  preferences[SELECT_YEAR] ?: INITIAL_YEAR,
                dataVersion = preferences[DATA_VERSION] ?: INITIAL_VERSION
            )
        }

    suspend fun savePoint(point: String) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[SELECT_POINT] = point
            }
        }
    }

    suspend fun saveYear(year: Int) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[SELECT_YEAR] = year
            }
        }
    }

    suspend fun saveInitial(data: DirectoryData.Table) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[DATA_VERSION] = data.version
            }
        }
    }

    suspend fun clearData() {
        withContext(Dispatchers.IO) {
            dataStore.edit {
                it.clear()
            }
        }
    }
}

data class UserPreferencesData(
    val selectPoint: String = INITIAL_POINT,
    val selectYear: Int = INITIAL_YEAR,
    val dataVersion: String = INITIAL_VERSION
)