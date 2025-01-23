package com.dullbluelab.pastweather

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dullbluelab.pastweather.data.WeatherRepository
import com.dullbluelab.pastweather.data.UserPreferencesRepository
import com.dullbluelab.pastweather.data.WeatherDatabase

private const val PAST_WEATHER_PREFERENCE_NAME = "past_weather_preferences"
private val Context.datastore: DataStore<Preferences>
    by preferencesDataStore(name = PAST_WEATHER_PREFERENCE_NAME)

class PastWeatherApplication : Application() {
    lateinit var container: AppContainer
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        userPreferencesRepository = UserPreferencesRepository(datastore)
    }
}

interface AppContainer {
    val weatherRepository: WeatherRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val weatherRepository: WeatherRepository by lazy {
        val database = WeatherDatabase.getDatabase(context)
        WeatherRepository(database, context)
    }
}
