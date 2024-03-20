package com.dullbluelab.pastweather.ui

import android.os.Build
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.dullbluelab.pastweather.R

@Composable
fun WeatherScreen(
    onChangeYear: (Int) -> Unit,
    onChangeLocation: () -> Unit,
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val rootUi by viewModel.rootUi.collectAsState()

    if (rootUi.mode == "startup") {
        StartupPanel(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
        )
    }
    else {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
        ) {
            ContentsSelectBar(
                onClickFinder = { viewModel.changeMode("finder") },
                onClickGraph = { viewModel.changeMode("graph") },
                onClickAverage = {
                    viewModel.changeMode("average")
                                 },
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            when (rootUi.mode) {
               "graph" -> {
                    GraphPanel(
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(8f)
                            .fillMaxWidth()
                    )
                }
                "finder" -> {
                    FinderPanel(
                        viewModel = viewModel,
                        onChangeYear = onChangeYear,
                        onChangeLocation = onChangeLocation,
                        modifier = Modifier
                            .weight(8f)
                            .fillMaxWidth()
                    )
                }
                "average" -> {
                    AveragePanel(
                        viewModel = viewModel,
                        modifier = Modifier
                            .weight(8f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun StartupPanel(
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val startupUi by viewModel.startupUi.collectAsState()

    val message = when (startupUi.mode) {
        "boot" -> stringResource(id = R.string.text_boot)
        "update" -> stringResource(id = R.string.text_update)
        "download" -> stringResource(id = R.string.text_download)
        "setting" -> stringResource(id = R.string.text_setting)
        "start" -> ""
        "error" -> startupUi.message
        else -> ""
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = message
        )
        if (startupUi.mode == "download" || startupUi.mode == "setting") {
            Text(
                text = "${startupUi.progressCount}%"
            )
        }
    }
    if (startupUi.mode == "error") {
        Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()
        viewModel.changeMode("finder")
    }
}

@Composable
private fun FinderPanel(
    viewModel: PastWeatherViewModel,
    onChangeYear: (Int) -> Unit,
    onChangeLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.finderUi.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        if (uiState.pointName == "") {
            Button(
                onClick = { onChangeLocation() }
            ) {
                Text(text = stringResource(id = R.string.button_point))
            }
        }
        else {
            TextButton(onClick = { onChangeLocation() }) {
                Text(
                    text = uiState.pointName,
                    fontSize = 24.sp,
                    modifier = Modifier
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            Text(
                text = stringResource(id = R.string.label_today),
                modifier = Modifier.padding(8.dp)
            )
            FindYearPicker(uiState) { value ->
                onChangeYear(value)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_high_temp),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = uiState.highTemp.toString() + stringResource(id = R.string.temp_char),
                fontSize = 36.sp
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_low_temp),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = uiState.lowTemp.toString() + stringResource(id = R.string.temp_char),
                fontSize = 36.sp
            )
        }
        Text(
            text = uiState.sky,
            modifier = Modifier.padding(8.dp),
            fontSize = 18.sp
        )
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun FindYearPicker(
    uiState: PastWeatherViewModel.FinderUiState,
    changeValue: (Int) -> Unit
) {
    val color = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                value = uiState.selectYear
                maxValue = uiState.maxYear
                minValue = uiState.minYear
                textColor = color
                setOnValueChangedListener { _, _, newValue ->
                    changeValue(newValue)
                }
            }
        },
        update = { view ->
            view.value = uiState.selectYear
        }
    )
}

@Composable
private fun ContentsSelectBar(
    onClickFinder: () -> Unit,
    onClickGraph: () -> Unit,
    onClickAverage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
    ) {
        TextButton(onClick = onClickFinder) {
            Text(text = stringResource(id = R.string.tag_finder))
        }
        TextButton(onClick = onClickGraph) {
            Text(text = stringResource(id = R.string.tag_graph))
        }
        TextButton(onClick = onClickAverage) {
            Text(text = stringResource(id = R.string.tag_average))
        }
    }
}

@Composable
private fun GraphPanel(
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val graphUi by viewModel.graphUi.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = graphUi.pointName,
            modifier = Modifier
                .padding(8.dp)
        )
        Text(
            text = "${graphUi.selectMonth}/${graphUi.selectDay} in ${graphUi.minYear} - ${graphUi.maxYear}",
            modifier = Modifier
                .padding(8.dp)
        )
        GraphCanvas(
            tempList = graphUi.tempList,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun GraphCanvas(
    tempList: List<PastWeatherViewModel.GraphTempItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = MaterialTheme.colorScheme.secondary)

    Canvas(
        modifier = modifier
    ) {
        val scale = GraphScale(
            paddingX = 20f,
            paddingY = 20f,
            range = 100f,
            maxTemp = 60f,
            itemCount = tempList.size,
            width = size.width,
            height = size.height
        )

        // draw frame
        var pointY = scale.pointY(40f)
        val startX = scale.pointX(0)
        val endX = scale.pointX(tempList.size)
        drawLine(
            color = Color.Yellow,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 1f
        )
        drawText(
            textMeasurer = textMeasurer,
            text = "40℃",
            topLeft = Offset(0f, pointY),
            style = textStyle,
        )
        pointY = scale.pointY(0f)
        drawLine(
            color = Color.Gray,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 2f
        )
        drawText(
            textMeasurer, "0℃",
            style = textStyle,
            topLeft = Offset(0f, pointY)
        )
        pointY = scale.pointY(20f)
        drawLine(
            color = Color.Green,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 2f
        )
        drawText(
            textMeasurer, "20℃",
            style = textStyle,
            topLeft = Offset(0f, pointY)
        )
        pointY = scale.pointY(-20f)
        drawLine(
            color = Color.Cyan,
            start = Offset(startX, pointY),
            end = Offset(endX, pointY),
            strokeWidth = 2f
        )
        drawText(
            textMeasurer, "-20℃",
            style = textStyle,
            topLeft = Offset(0f, pointY)
        )

        var prev: GraphItem? = null
        var pointX = scale.paddingX

        for (item in tempList) {
            val point = scale.graphItem(item, pointX)
            prev?.let {
                drawLine(
                    color = Color.Red,
                    start = Offset(it.highX, it.highY),
                    end = Offset(point.highX, point.highY),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.Blue,
                    start = Offset(it.lowX, it.lowY),
                    end = Offset(point.lowX, point.lowY),
                    strokeWidth = 3f
                )
            }
            prev = point
            pointX += scale.pitchX
        }
    }
}

private data class GraphItem(
    val highX: Float,
    val highY: Float,
    val lowX: Float,
    val lowY: Float
)

private class GraphScale(
    val paddingX: Float = 20f,
    val paddingY: Float = 20f,
    val range: Float = 100f,
    val maxTemp: Float = 60f,
    val itemCount: Int,
    val width: Float,
    val height: Float
) {
    val pitchX = (width - paddingX * 2f) / (itemCount - 1).toFloat()
    val pitchY = (height - paddingY * 2f) / range

    fun graphItem(item:PastWeatherViewModel.GraphTempItem, pointX: Float): GraphItem
    = GraphItem(
        highX = pointX,
        highY = pointY(item.high.toFloat()),
        lowX = pointX,
        lowY = pointY(item.low.toFloat())
    )

    fun pointY(temp: Float): Float = paddingY + (maxTemp - temp) * pitchY

    fun pointX(count: Int) = paddingX + count.toFloat() * pitchX
}

@Composable
private fun AveragePanel(
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val averageUi by viewModel.averageUi.collectAsState()
    val days = "${averageUi.selectMonth}/${averageUi.selectDay} in " +
            "${averageUi.minYears} - ${averageUi.maxYears}"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = averageUi.pointName,
            modifier = Modifier
                .padding(8.dp)
        )
        Text(
            text = days,
            modifier = Modifier
                .padding(8.dp)
        )
        if (averageUi.tempList.isNotEmpty()) {
            GraphCanvas(
                tempList = averageUi.tempList,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
            )
            AverageList(
                tempList = averageUi.tempList,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AverageList(
    tempList: List<PastWeatherViewModel.GraphTempItem>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(
            items = tempList,
            key = { tempItem -> tempItem.years }
        ) { item ->
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item.years
                )
                Text(
                    text = item.high.toString()
                )
                Text(
                    text = item.low.toString()
                )
            }
        }
    }
}