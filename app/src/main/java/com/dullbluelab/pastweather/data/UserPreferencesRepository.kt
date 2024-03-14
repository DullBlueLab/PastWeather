package com.dullbluelab.pastweather.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val INITIAL_POINT = "tky"
private const val INITIAL_YEAR = 2023
private const val INITIAL_MAX_YEAR = 0
private const val INITIAL_MIN_YEAR = 0
private const val INITIAL_VERSION = "0"

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SELECT_POINT = stringPreferencesKey("select_point")
        val SELECT_YEAR = intPreferencesKey("select_year")
        val MAX_YEAR = intPreferencesKey("max_year")
        val MIN_YEAR = intPreferencesKey("min_year")
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
                maxYear = preferences[MAX_YEAR] ?: INITIAL_MAX_YEAR,
                minYear = preferences[MIN_YEAR] ?: INITIAL_MIN_YEAR,
                dataVersion = preferences[DATA_VERSION] ?: INITIAL_VERSION
            )
        }

    suspend fun savePoint(point: String) {
        dataStore.edit { preferences ->
            preferences[SELECT_POINT] = point
        }
    }

    suspend fun saveYear(year: Int) {
        dataStore.edit { preferences ->
            preferences[SELECT_YEAR] = year
        }
    }

    suspend fun saveInitial(data: DirectoryJson.Data) {
        dataStore.edit { preferences ->
            preferences[DATA_VERSION] = data.version
            preferences[MAX_YEAR] = data.maxyear.toInt()
            preferences[MIN_YEAR] = data.minyear.toInt()
        }
    }

    suspend fun clearData() {
        dataStore.edit {
            it.clear()
        }
    }
}

data class UserPreferencesData(
    val selectPoint: String = INITIAL_POINT,
    val selectYear: Int = INITIAL_YEAR,
    val maxYear: Int = INITIAL_MAX_YEAR,
    val minYear: Int = INITIAL_MIN_YEAR,
    val dataVersion: String = INITIAL_VERSION
)