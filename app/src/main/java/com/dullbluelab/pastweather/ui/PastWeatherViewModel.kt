package com.dullbluelab.pastweather.ui

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dullbluelab.pastweather.PastWeatherApplication
import com.dullbluelab.pastweather.data.AverageWeatherTable
import com.dullbluelab.pastweather.data.DailyWeatherTable
import com.dullbluelab.pastweather.data.DirectoryData
import com.dullbluelab.pastweather.data.LocationTable
import com.dullbluelab.pastweather.data.UserPreferencesData
import com.dullbluelab.pastweather.data.UserPreferencesRepository
import com.dullbluelab.pastweather.data.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val TAG = "PastWeatherViewModel"

class PastWeatherViewModel(
    private val preferences: UserPreferencesRepository,
    private val weatherRepository: WeatherRepository,
    private val resources: Resources
) : ViewModel() {

    data class RouteUiState(
        val route: String = "Weather",
        val mode: String = "start" // or "finder", "graph", "average"
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
        val sky: String = ""
    )
    private val _finderUi = MutableStateFlow(FinderUiState())
    val finderUi: StateFlow<FinderUiState> = _finderUi.asStateFlow()

    data class GraphTempItem(
        val year: Int = 0,
        val years: String = "",
        var high: Double = 0.0,
        var low: Double = 0.0
    )

    data class GraphUiState(
        val pointName: String = "",
        val minYear: Int = 0,
        val maxYear: Int = 0,
        val selectMonth: Int = 0,
        val selectDay: Int = 0,
        val tempList: List<GraphTempItem> = listOf()
    )
    private val _graphUi = MutableStateFlow(GraphUiState())
    val graphUi: StateFlow<GraphUiState> = _graphUi.asStateFlow()

    data class AverageUiState(
        val pointName: String = "",
        val minYears: String = "",
        val maxYears: String = "",
        val tempList: List<GraphTempItem> = listOf(),
        val selectMonth: Int = 0,
        val selectDay: Int = 0,
    )
    private val _averageUi = MutableStateFlow(AverageUiState())
    val averageUi: StateFlow<AverageUiState> = _averageUi.asStateFlow()

    data class LocationUiState(
        val pointCode: String = "",
        val entryList: List<LocationTable> = listOf(),
        val otherList: List<LocationTable> = listOf(),
    )
    private val _locationUi = MutableStateFlow(LocationUiState())
    val locationUi: StateFlow<LocationUiState> = _locationUi.asStateFlow()

    data class DownloadUiState(
        val status: String = "standby", // or "download", "failed", "cancel", "success"
        val progressCount: Int = 0,
        val message: String = ""
    )
    private val _downloadUi = MutableStateFlow(DownloadUiState())
    val downloadUi: StateFlow<DownloadUiState> = _downloadUi.asStateFlow()

    private val locationStream: Flow<List<LocationTable>> = weatherRepository.getAllLocation()
    private val preferenceStream: Flow<UserPreferencesData> = preferences.data

    private var preferencesData: UserPreferencesData? = null
    private var locationList: List<LocationTable>? = null
    private val directory: DirectoryData = DirectoryData()

    var currentPointCode: String = ""
    private var currentYear: Int = 0

    var selectLocationItem: LocationTable? = null
    private var downloadItem: LocationTable? = null
    private var downloadCancelFlag: Boolean = false
    private var graphList: MutableList<GraphTempItem> = mutableListOf()

    private var today: LocalDate = LocalDate.now()

    companion object {
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PastWeatherApplication)
                PastWeatherViewModel(
                    application.userPreferencesRepository,
                    application.container.weatherRepository,
                    application.resources
                )
            }
        }
    }

    // init

    init {
        viewModelScope.launch {
            locationStream.collect { list ->
                updateLocation(list)
            }
        }
        viewModelScope.launch {
            preferenceStream.collect { data ->
                if (data.dataVersion == "0") {
                    if (preferencesData == null) setupDirectory()
                }
                else {
                    updatePreference(data)
                    catchPreferences(data)
                }
            }
        }
    }

    private fun updateLocation(list: List<LocationTable>) {
        locationList = list
        val entries = mutableListOf<LocationTable>()
        val others = mutableListOf<LocationTable>()

        list.forEach { item ->
            if (item.loaded) entries.add(item)
            else others.add(item)
        }
        updateLocationListUi(entries.toList(), others.toList())
    }

    private fun catchPreferences(data: UserPreferencesData) {
        var flag = false
        preferencesData = data

        if (data.selectPoint.isNotEmpty() && currentPointCode != data.selectPoint) {
            currentPointCode = data.selectPoint
            setsPointName(currentPointCode)
            updatePointCodeUi(currentPointCode)

            updateGraphData()
            updateAverage()
            flag = true
        }
        if (data.selectYear != 0 && currentYear != data.selectYear) {
            currentYear = data.selectYear
            flag = true
        }

        if (flag) {
            updateWeather(currentPointCode, currentYear)
        }
    }

    private fun setupDirectory() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { directory.load(resources) }
            data?.let {
                withContext(Dispatchers.Default) {
                    preferences.clearData()
                    preferences.saveInitial(data)
                    weatherRepository.clearLocationList()
                    weatherRepository.updateLocationList(data.points)
                    cancel()
                }
            }
        }
    }

    private fun updatePreference(item: UserPreferencesData) {
        _finderUi.update { state ->
            state.copy(
                maxYear = item.maxYear,
                minYear = item.minYear
            )
        }
        _graphUi.update { state ->
            state.copy(
                maxYear = item.maxYear,
                minYear = item.minYear
            )
        }
    }

    //
    // routeUi

    fun updateMode(mode: String) {
        _routeUi.update { state ->
            state.copy(
                mode = mode
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

    private fun updatePointNameUi(name: String ) {
        _finderUi.update { state ->
            state.copy(
                pointName = name
            )
        }
    }

    private fun updateFinderUi(point: String, table: DailyWeatherTable) {
        val location = getLocationItem(point)
        _finderUi.update { state ->
            state.copy(
                pointName = location?.name ?: "",
                selectYear = table.year,
                selectMonth = table.month,
                selectDay = table.day,
                highTemp = table.high,
                lowTemp = table.low,
                sky = table.sky
            )
        }
    }

    //
    // GraphUi

    private fun updateGraphUi(point: String, month: Int, day: Int, list: List<GraphTempItem>) {
        val location = getLocationItem(point)
        _graphUi.update { state ->
            state.copy(
                pointName = location?.name ?: "",
                selectMonth = month,
                selectDay = day,
                tempList = list
            )
        }
    }

    //
    // LocationUi

    private fun updateLocationListUi(
        entries: List<LocationTable>, others: List<LocationTable>) {
        _locationUi.update { state ->
            state.copy(
                entryList = entries,
                otherList = others
            )
        }
    }

    private fun updatePointCodeUi(code: String) {
        _locationUi.update { state ->
            state.copy(
                pointCode = code
            )
        }
    }

    //
    // DownloadUi

    private fun updateDownloadStatusUi(mode: String, message: String = "") {
        _downloadUi.update { state ->
            state.copy(
                status = mode,
                message = message
            )
        }
    }

    private fun updateDownloadProgress(count: Int) {
        viewModelScope.launch {
            _downloadUi.update { state ->
                state.copy(
                    progressCount = count
                )
            }
        }
    }

    //
    // private var

    private fun getLocationItem(code: String = currentPointCode): LocationTable? {
        var result: LocationTable? = null
        locationList?.let { list ->
            for (item in list) {
                if (item.code == code) {
                    result = item
                    break
                }
            }
        }
        return result
    }

    //
    // init

    private fun setsPointName(code: String = currentPointCode) {
        if (code.isEmpty()) return
        val item = getLocationItem(code)
        val name = item?.name ?: ""
        updatePointNameUi(name)
    }

    private fun updateWeather(point: String, year: Int) {
        if (point == "" || year == 0) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val stream = weatherRepository.getTableStream(
                        point, year, today.monthValue, today.dayOfMonth
                    )
                    stream.collect { table ->
                        table?.let { updateFinderUi(point, it) }
                        cancel()
                    }
                }
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    //
    // WeatherScreen

    private fun updateGraphData() {
        preferencesData?.let { data ->
            val point = currentPointCode
            val min = data.minYear
            val max = data.maxYear
            val size = max - min + 1
            val month = today.monthValue
            val day = today.dayOfMonth
            var count = 0
            graphList = mutableListOf()

            for (year in min..max) {
                viewModelScope.launch {
                    try {
                        withContext(Dispatchers.Default) {
                            val stream = weatherRepository.getTableStream(point, year, month, day)
                            stream.collect { table ->
                                table?.let {
                                    appendGraphItem(
                                        graphList,
                                        GraphTempItem(year, "", it.high, it.low)
                                    )
                                }
                                count++
                                if (count >= size) {
                                    updateGraphUi(point, month, day, graphList.toList())
                                }
                                cancel()
                            }
                        }
                    } catch (e: Exception) {
                        e.message?.let { Log.e(TAG, it) }
                    }
                }
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

    fun changeYear(year: Int) {
        preferencesData?.let { preference ->
            if (preference.minYear <= year && year <= preference.maxYear) {
                viewModelScope.launch {
                    preferences.saveYear(year)
                }
            }
        }
    }

    private fun updateAverage() {
        val month = today.monthValue
        val day = today.dayOfMonth

        viewModelScope.launch {
            try {
                val point = currentPointCode
                withContext(Dispatchers.Default) {
                    val stream = weatherRepository.getAverageStream(point, month, day)
                    stream.collect { list ->
                        if (list.isNotEmpty()) {
                            val sorted = list.sortedBy { it.years }
                            val start = sorted[0].years
                            val end = sorted[sorted.size - 1].years
                            updateAverageUi(sorted, start, end, point, month, day)
                        }
                        cancel()
                    }
                }
            }
            catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    private fun updateAverageUi(
        list: List<AverageWeatherTable>,
        start: String, end: String, point: String, month: Int, day: Int
    ) {
        val tempList = mutableListOf<GraphTempItem>()
        list.forEach { item ->
            tempList.add(GraphTempItem(0, item.years, item.high, item.low))
        }
        val location = getLocationItem(point)
        _averageUi.update { state ->
            state.copy(
                tempList = tempList,
                minYears = start,
                maxYears = end,
                pointName = location?.name ?: "",
                selectMonth = month,
                selectDay = day
            )
        }
    }

    //
    // LocationScreen

    fun changeSelectPoint(code: String) {
        viewModelScope.launch {
            preferences.savePoint(code)
        }
    }

    fun deleteDataAt(item: LocationTable) {
        weatherRepository.deleteDataAt(item.code) {}
    }

    //
    // DownloadScreen

    fun downloadWeatherData(
        item: LocationTable,
        failed: (String) -> Unit = {},
        success: () -> Unit = {},
    ) {
        updateDownloadStatusUi("download")
        downloadItem = item
        downloadCancelFlag = false

        weatherRepository.downloadWeatherData(
            pointCode = item.code,
            progress = { per -> updateDownloadProgress(per) },
            cancelFlag = { downloadCancelFlag },
            failed = { message ->
                val mode = if (message == "cancel") "cancel" else "failed"
                updateDownloadStatusUi(mode, message)
                viewModelScope.launch {
                    failed(message)
                }
            },
            success = {
                changeSelectPoint(item.code)
                updateDownloadStatusUi("success")
                viewModelScope.launch {
                    success()
                }
            }
        )
    }

    fun checkAndCancelDownload() {
        downloadCancelFlag = true
        updateDownloadStatusUi("cancel")
    }

    fun clearDownloadStatus() {
        updateDownloadStatusUi("standby")
    }
}
