package com.dullbluelab.pastweather.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dullbluelab.pastweather.PastWeatherApplication
import com.dullbluelab.pastweather.data.AverageWeatherTable
import com.dullbluelab.pastweather.data.DirectoryJson
import com.dullbluelab.pastweather.data.LocationTable
import com.dullbluelab.pastweather.data.UserPreferencesData
import com.dullbluelab.pastweather.data.UserPreferencesRepository
import com.dullbluelab.pastweather.data.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

const val ERROR_NOT_LOAD_DIRECTORY = "not load directory"

private const val TAG = "PastWeatherViewModel"

class PastWeatherViewModel(
    private val preferences: UserPreferencesRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

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

    data class StartupUiState(
        val mode: String = "boot",
        val message: String = "",
        val progressCount: Int = (-1)
    )
    private val _startupUi = MutableStateFlow(StartupUiState())
    val startupUi: StateFlow<StartupUiState> = _startupUi.asStateFlow()

    data class RootUiState(
        val position: String = "Weather",
        val mode: String = "startup", // or "finder", "graph", "average"
    )
    private val _rootUi = MutableStateFlow(RootUiState())
    val rootUi: StateFlow<RootUiState> = _rootUi.asStateFlow()

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

    private val preferencesStream: StateFlow<UserPreferencesData> = preferences.data
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UserPreferencesData()
        )
    private var preferencesData: UserPreferencesData = UserPreferencesData()

    private val locationListStream = weatherRepository.getAllLocation()
    private var locationList: List<LocationTable>? = null
    private val directoryJson: DirectoryJson = DirectoryJson()

    private var currentPointCode: String = ""
    private var currentYear: Int = 0

    var selectLocationItem: LocationTable? = null
    private var downloadItem: LocationTable? = null
    private var downloadCancelFlag: Boolean = false

    var destinationListener: DestinationListener? = null

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

    init {
        checkDirectory()
        chainFlowPipe()
    }

    //
    // StartupUi

    private fun updateStartupUi(mode: String, message: String = "") {
        viewModelScope.launch {
            _startupUi.update {
                StartupUiState(
                    mode = mode,
                    message = message
                )
            }
        }
    }

    private fun updateProgress(count: Int) {
        viewModelScope.launch {
            _startupUi.update { state ->
                state.copy(
                    progressCount = count
                )
            }
        }
    }

    //
    // rootUi

    private fun updateRootUi(mode: String) {
        _rootUi.update { state ->
            state.copy(
                mode = mode
            )
        }
    }

    fun updatePositionUi(position: String) {
        _rootUi.update { state ->
            state.copy(
                position = position
            )
        }
    }

    //
    // FinderUi

    private fun updatePointCodeUi(code: String) {
        _locationUi.update { state ->
            state.copy(
                pointCode = code
            )
        }
    }

    private fun updatePointNameUi(name: String ) {
        _finderUi.update { state ->
            state.copy(
                pointName = name
            )
        }
        _graphUi.update { state ->
            state.copy(
                pointName = name
            )
        }
        _averageUi.update { state ->
            state.copy(
                pointName = name
            )
        }
    }

    //
    // GraphUi

    private fun updateGraphUi(month: Int, day: Int, list: List<GraphTempItem>) {
        viewModelScope.launch {
            _graphUi.update { state ->
                state.copy(
                    selectMonth = month,
                    selectDay = day,
                    tempList = list
                )
            }
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

    private fun checkDirectory() {
        updateRootUi("startup")
        directoryJson.load(
            success = { data ->
                viewModelScope.launch {
                    preferences.data.collect { preference ->
                        if (preference.dataVersion != data.version) {
                            updateAllData(data)
                        }
                        else {
                            updateStartupUi("start")
                            updateRootUi("finder")
                        }
                        cancel()
                    }
                }
            },
            failed = {
                updateStartupUi("error", ERROR_NOT_LOAD_DIRECTORY)
            }
        )
    }

    private fun updateAllData(data: DirectoryJson.Data) {
        updateStartupUi("update")
        val code = preferencesData.selectPoint

        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            try {
                weatherRepository.clearAllTables()
                preferences.saveInitial(data)
                weatherRepository.updateLocationList(data.points)
                updateStartupUi("download")

                weatherRepository.downloadWeatherData(
                    pointCode = code,
                    progress = { per -> updateProgress(per) },
                    cancelFlag = { downloadCancelFlag },
                    failed = { message ->
                        updateStartupUi("error", message)
                    },
                    success = {
                        setsPointName()
                        updateStartupUi("start")
                        updateRootUi("finder")
                        updateWeather()
                    }
                )
            }
            catch (e: Exception) {
                val message = e.message ?: "viewModel.updateAllData"
                updateStartupUi("error", message)
            }
        }
    }

    private fun chainFlowPipe() {
        viewModelScope.launch {
            preferencesStream.collect { item ->
                var updateFlag = false
                preferencesData = item

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
                if (item.selectPoint.isNotEmpty() && currentPointCode != item.selectPoint) {
                    currentPointCode = item.selectPoint
                    setsPointName(currentPointCode)
                    updatePointCodeUi(currentPointCode)
                    updateFlag = true
                }
                if (currentYear != item.selectYear) {
                    currentYear = item.selectYear
                    updateFlag = true
                }
                if (updateFlag) updateWeather()
            }
        }
        chainLocationListStream()
    }

    private fun setsPointName(code: String = currentPointCode) {
        if (code.isEmpty()) return
        val item = getLocationItem(code)
        val name = item?.name ?: ""
        updatePointNameUi(name)
    }

    private fun chainLocationListStream() {
        viewModelScope.launch {
            locationListStream.collect { list ->
                locationList = list
                val entries = mutableListOf<LocationTable>()
                val others = mutableListOf<LocationTable>()
                list.forEach { item ->
                    if (item.loaded) entries.add(item)
                    else others.add(item)
                }
                updateLocationListUi(entries.toList(), others.toList())
                setsPointName()
            }
        }
    }

    private fun updateWeather(
        point: String = preferencesData.selectPoint,
        year: Int = preferencesData.selectYear
    ) {
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            try {
                if (point.isNotEmpty() && year > 0) {
                    val stream = weatherRepository.getTableStream(
                        point, year, today.monthValue, today.dayOfMonth
                    )
                    stream.collect { table ->
                        if (table == null) throw Exception("not match weather data")
                        _finderUi.update { state ->
                            state.copy(
                                selectYear = table.year,
                                selectMonth = table.month,
                                selectDay = table.day,
                                highTemp = table.high,
                                lowTemp = table.low,
                                sky = table.sky
                            )
                        }
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

    fun changeMode(mode: String) {
        when (mode) {
            "graph" -> {
                updateGraphData()
            }
            "average" -> {
                updateAverage()
            }
        }
        _rootUi.update { state ->
            state.copy(
                mode = mode
            )
        }
    }

    private fun updateGraphData() {
        val list = mutableListOf<GraphTempItem>()
        val point = preferencesData.selectPoint
        val min = preferencesData.minYear
        val max = preferencesData.maxYear
        val size = max - min + 1
        val month = today.monthValue
        val day = today.dayOfMonth
        var count = 0

        for (year in min..max) {
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                try {
                    val stream = weatherRepository.getTableStream(point, year, month, day)
                    stream.collect { table ->
                        table?.let {
                            appendGraphItem(list, GraphTempItem(year, "", it.high, it.low))
                        }
                        count ++
                        if (count >= size) {
                            updateGraphUi(month, day, list.toList())
                        }
                        cancel()
                    }
                }
                catch (e:Exception) {
                    e.message?.let { Log.e(TAG, it) }
                }
            }
        }
    }

    private fun appendGraphItem(list: MutableList<GraphTempItem>, item: GraphTempItem) {
        var count = 0
        while (count < list.size) {
            if (list[count].year > item.year) break
            count ++
        }
        list.add(count, item)
    }

    fun changeYear(year: Int) {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            preferences.saveYear(year)
        }
    }

    private fun updateAverage() {
        val month = today.monthValue
        val day = today.dayOfMonth

        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            try {
                val pointCode = preferencesData.selectPoint
                val stream = weatherRepository.getAverageStream(pointCode, month, day)

                stream.collect { list ->
                    if (list.isNotEmpty()) {
                        val sorted = list.sortedBy { it.years }
                        val start = sorted[0].years
                        val end = sorted[sorted.size - 1].years
                        updateAverageUi(sorted, start, end, month, day)
                    }
                    cancel()
                }
            }
            catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    private fun updateAverageUi(
        list: List<AverageWeatherTable>,
        start: String, end: String, month: Int, day: Int
    ) {
        val tempList = mutableListOf<GraphTempItem>()
        list.forEach { item ->
            tempList.add(GraphTempItem(0, item.years, item.high, item.low))
        }
        _averageUi.update { state ->
            state.copy(
                tempList = tempList,
                minYears = start,
                maxYears = end,
                selectMonth = month,
                selectDay = day
            )
        }
    }

    //
    // LocationScreen

    fun changeSelectPoint(code: String) {
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            preferences.savePoint(code)
        }
    }

    fun deleteDataAt(item: LocationTable) {
        weatherRepository.deleteDataAt(item.code)
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
