package com.dullbluelab.pastweather.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dullbluelab.pastweather.PastWeatherApplication
import com.dullbluelab.pastweather.R
import com.dullbluelab.pastweather.data.AverageData
import com.dullbluelab.pastweather.data.LocationData
import com.dullbluelab.pastweather.data.PeakData
import com.dullbluelab.pastweather.data.SkyCount
import com.dullbluelab.pastweather.data.UserPreferencesData
import com.dullbluelab.pastweather.data.UserPreferencesRepository
import com.dullbluelab.pastweather.data.WeatherData
import com.dullbluelab.pastweather.data.WeatherRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date

private const val TAG = "PastWeatherViewModel"

class PastWeatherViewModel(
    private val preferences: UserPreferencesRepository,
    private val repositories: WeatherRepository,
) : ViewModel() {

    data class RouteUiState(
        val route: String = "Weather",
        val mode: String = "start", // or "point", "finder", "graph", "average"
        val point: String = "",
        val messageId: Int = 0,
        val isInputDay: Boolean = false
    )
    private val _routeUi = MutableStateFlow(RouteUiState())
    val routeUi: StateFlow<RouteUiState> = _routeUi.asStateFlow()

    data class FinderUiState(
        val pointName: String = "",
        val selectYear: Int = 0,
        val selectMonth: Int = 1,
        val selectDay: Int = 1,
        val maxYear: Int = 0,
        val minYear: Int = 0,
        val lowTemp: Double = 0.0,
        val highTemp: Double = 0.0,
        val sky: String = "",
        val tempList: List<GraphTempItem> = listOf(),
        val sunnyCount: Int = 0,
        val rainyCount: Int = 0,
        val clearSkyCount: Int = 0
    )
    private val _finderUi = MutableStateFlow(FinderUiState())
    val finderUi: StateFlow<FinderUiState> = _finderUi.asStateFlow()

    data class AverageUiState(
        val pointName: String = "",
        val minYears: String = "",
        val maxYears: String = "",
        val tempList: List<GraphTempItem> = listOf(),
        val selectMonth: Int = 0,
        val selectDay: Int = 0,
        val recentHighTemp: Double = 0.0,
        val recentLowTemp: Double = 0.0
    )
    private val _averageUi = MutableStateFlow(AverageUiState())
    val averageUi: StateFlow<AverageUiState> = _averageUi.asStateFlow()

    data class PeakUiState(
        val pointName: String = "",
        val data: List<PeakData> = listOf()
    )
    private val _peakUi = MutableStateFlow(PeakUiState())
    val peakUi: StateFlow<PeakUiState> = _peakUi.asStateFlow()

    data class LocationUiState(
        val pointCode: String = "",
        val list: List<LocationData> = listOf()
    )
    private val _locationUi = MutableStateFlow(LocationUiState())
    val locationUi: StateFlow<LocationUiState> = _locationUi.asStateFlow()

    private val preferenceStream: Flow<UserPreferencesData> = preferences.data
    private var preferencesData: UserPreferencesData? = null

    private var currentPointCode: String = ""
    private var currentYear: Int = 0

    private var graphList: MutableList<GraphTempItem> = mutableListOf()

    private var today: LocalDate = LocalDate.now()

    companion object {
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PastWeatherApplication)
                PastWeatherViewModel(
                    application.userPreferencesRepository,
                    application.container.weatherRepository
                )
            }
        }
    }

    // init

    init {
        viewModelScope.launch {
            try {
                repositories.loadDirectory()
                preferenceStream.collect { data ->
                    when (data.dataVersion) {
                        "0" -> {
                            updateMode("start", R.string.text_startup)
                            setupPreference()
                        }

                        "1", "2", "3" -> {
                            updateMode("start", R.string.text_update_data)
                            repositories.clearRecentData()
                            preferences.clearData()
                            setupPreference()
                        }
                    }
                    updateLocation(repositories.getAllLocation())
                    cancel()
                }

            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
        chainFlowPipe()
    }

    private suspend fun setupPreference() {
        val data = repositories.getDirectory()
        data?.let {
            preferences.saveInitial(data)
        }
    }

    private fun chainFlowPipe() {
        viewModelScope.launch {
            preferenceStream.collect { data ->
                try {
                    if (data.dataVersion == "4"
                        && data.selectPoint.isNotEmpty() && data.selectYear != 0) {
                        var changed = false
                        preferencesData = data

                        if (currentPointCode != data.selectPoint) {
                            currentPointCode = data.selectPoint
                            repositories.loadMatches(currentPointCode, today.monthValue, today.dayOfMonth)
                            setsPointName(currentPointCode)
                            updatePointCodeUi(currentPointCode)

                            updateGraphData()
                            updateAverage()
                            updatePeakData()
                            changed = true
                        }
                        if (currentYear != data.selectYear) {
                            currentYear = data.selectYear
                            changed = true
                        }
                        if (changed) {
                            updateWeather(currentYear)
                            val mode = routeUi.value.mode
                            if (mode == "start" || mode == "point") updateMode("finder")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    private fun updateLocation(list: List<LocationData>) {
        updateLocationListUi(list)
        if (currentPointCode.isNotEmpty() && finderUi.value.pointName.isEmpty()) {
            setsPointName(currentPointCode)
        }
    }

    private fun updatePeakData() {
        _peakUi.update { state ->
            state.copy(
                data = repositories.getPeakData()
            )
        }
    }

    private fun setsPointName(code: String = currentPointCode) {
        if (code.isEmpty()) return
        val item = repositories.getLocationItem(code)
        val name = item?.name ?: ""
        updatePointNameUi(name)
    }

    //
    // routeUi

    private fun updateMode(mode: String, messageId: Int = 0) {
        _routeUi.update { state ->
            state.copy(
                mode = mode,
                messageId = messageId
            )
        }
    }

    fun updateRoute(route: String) {
        _routeUi.update { state ->
            state.copy(
                route = route
            )
        }
    }

    //
    // FinderUi

    private fun updateWeather(year: Int) {
        if (year == 0) return
        try {
            val item = repositories.getWeatherItem(year)
            item?.let { updateFinderUi(currentPointCode, item, repositories.skyCount) }
        }
        catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun updatePointNameUi(name: String ) {
        _finderUi.update { state ->
            state.copy(
                pointName = name
            )
        }
        _averageUi.update { state ->
            state.copy(
                pointName = name
            )
        }
        _peakUi.update { state ->
            state.copy(
                pointName = name
            )
        }
    }

    private fun updateFinderUi(point: String, table: WeatherData, count: SkyCount) {
        val directories = repositories.getDirectory()
        val location = repositories.getLocationItem(point)
        _finderUi.update { state ->
            state.copy(
                pointName = location?.name ?: "",
                selectYear = table.year,
                selectMonth = table.month,
                selectDay = table.day,
                highTemp = table.high,
                lowTemp = table.low,
                sky = table.sky,
                sunnyCount = count.sunny,
                rainyCount = count.rainy,
                clearSkyCount = count.clearSkies,
                maxYear = directories?.maxyear?.toInt() ?: state.maxYear,
                minYear = directories?.minyear?.toInt() ?: state.minYear,
            )
        }
    }

    private fun updateGraphData() {
        graphList = mutableListOf()

        viewModelScope.launch {
            val list = repositories.getWeatherData()
            for (item in list) {
                appendGraphItem(
                    graphList,
                    GraphTempItem(item.year, "", item.high, item.low)
                )
            }
            if (graphList.isNotEmpty()) {
                updateGraphUi(graphList.toList())
            }
        }
    }

    private fun appendGraphItem(list: MutableList<GraphTempItem>, item: GraphTempItem) {
        var count = 0
        while (count < list.size) {
            if (list[count].year == item.year) return
            else if (list[count].year > item.year) break
            count ++
        }
        if (count < list.size) list.add(count, item) else list.add(item)
    }

    private fun updateGraphUi(list: List<GraphTempItem>) {
        _finderUi.update { state ->
            state.copy(
                tempList = list
            )
        }
    }

    //
    // AverageUi
    private fun updateAverage() {
        val month = today.monthValue
        val day = today.dayOfMonth
        val highTemp = repositories.recentAverage.highTemp
        val lowTemp = repositories.recentAverage.lowTemp

        val point = currentPointCode
        val list = repositories.getAverageData()
        if (list.isNotEmpty()) {
            val sorted = list.sortedBy { it.years }
            val start = sorted[0].years
            val end = sorted[sorted.size - 1].years
            updateAverageUi(sorted, start, end, point, month, day, highTemp, lowTemp)
        }
    }

    private fun updateAverageUi(
        list: List<AverageData>,
        start: String, end: String, point: String, month: Int, day: Int,
        highTemp: Double, lowTemp: Double
    ) {
        val tempList = mutableListOf<GraphTempItem>()
        list.forEach { item ->
            tempList.add(GraphTempItem(0, item.years, item.high, item.low))
        }
        val location = repositories.getLocationItem(point)
        _averageUi.update { state ->
            state.copy(
                tempList = tempList,
                minYears = start,
                maxYears = end,
                pointName = location?.name ?: "",
                selectMonth = month,
                selectDay = day,
                recentHighTemp = highTemp,
                recentLowTemp = lowTemp
            )
        }
    }

    fun resetData(done: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repositories.clearRecentData()
                preferences.clearData()
                setupPreference()
                done("success")
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                done("failed")
            }
        }
    }

    //
    // LocationUi

    private fun updateLocationListUi(list: List<LocationData>) {
        _locationUi.update { state ->
            state.copy(
                list = list
            )
        }
    }

    private fun updatePointCodeUi(code: String) {
        _locationUi.update { state ->
            state.copy(
                pointCode = code
            )
        }
        _routeUi.update { state ->
            state.copy(
                point = code
            )
        }
    }

    //
    // WeatherScreen

    fun changeYear(year: Int) {
        val directory = repositories.getDirectory()
        val minYear = directory?.minyear?.toInt() ?: 0
        val maxYear = directory?.maxyear?.toInt() ?: 0

        if (year in minYear..maxYear) {
            viewModelScope.launch {
                try {
                    preferences.saveYear(year)
                } catch (e: Exception) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun inputDay(flag: Boolean) {
        _routeUi.update { state ->
            state.copy(
                isInputDay = flag
            )
        }
    }

    fun changeDate(dateMillis: Long?) {
        try {
            inputDay(false)
            if (dateMillis != null) {
                val year = LocalDate.now().year
                val date = Date(dateMillis).toString()
                val month = monthValue(date.substring(4, 7)) ?: 1
                val day = date.substring(8, 10).toInt()
                today = LocalDate.of(year, month, day)

                updateMode("start", R.string.text_collect_data)

                viewModelScope.launch {
                    repositories.loadMatches(currentPointCode, today.monthValue, today.dayOfMonth)

                    updateGraphData()
                    updateAverage()
                    updatePeakData()
                    updateWeather(currentYear)

                    updateMode("finder")
                }
            }
        } catch(e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun monthValue(tag: String): Int? = monthValueMap[tag]

    private val monthValueMap: Map<String, Int> = mapOf(
        "Jan" to 1,
        "Feb" to 2,
        "Mar" to 3,
        "Apr" to 4,
        "May" to 5,
        "Jun" to 6,
        "Jul" to 7,
        "Aug" to 8,
        "Sep" to 9,
        "Oct" to 10,
        "Nov" to 11,
        "Dec" to 12
    )

    //
    // LocationScreen

    fun changeLocation(code: String) {
        viewModelScope.launch {
            try {
                preferences.savePoint(code)
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }
}
