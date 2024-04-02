package com.dullbluelab.pastweather.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dullbluelab.pastweather.MainActivity
import com.dullbluelab.pastweather.R
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun WeatherScreen(
    activity: MainActivity,
    onChangeYear: (Int) -> Unit,
    onLocation: () -> Unit,
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val routeUi by viewModel.routeUi.collectAsState()
    val finderUi by viewModel.finderUi.collectAsState()

    if (routeUi.mode == "start") {
        if (finderUi.pointName == "") {
            StartDownload(onLocation = onLocation)
        }
        else {
            viewModel.updateMode("finder")
        }
    }
    else {
        TagPager(
            tagList = listOf(
                stringResource(id = R.string.tag_finder),
                stringResource(id = R.string.tag_graph),
                stringResource(id = R.string.tag_average)
            ),
            pageList = listOf(
                {
                    FinderPanel(
                        viewModel = viewModel,
                        onChangeYear = onChangeYear,
                        onChangeLocation = onLocation,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                },
                {
                    GraphPanel(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                },
                {
                    AveragePanel(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            ),
            modifier = modifier.fillMaxSize()
        )

    }
}


@Composable
private fun StartDownload(
    onLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Button(
            onClick = { onLocation() }
        ) {
            Text(text = stringResource(id = R.string.button_point))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
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
            FindYearPicker(viewModel) { value ->
                viewModel.changeYear(value)
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

@Composable
private fun FindYearPicker(
    viewModel: PastWeatherViewModel,
    changeValue: (Int) -> Unit
) {
    val uiState by viewModel.finderUi.collectAsState()
    val color = MaterialTheme.colorScheme.primary

    NumberCounter(
        value = uiState.selectYear,
        maxValue = uiState.maxYear,
        minValue = uiState.minYear,
        textColor = color,
        onValueChangedListener =  { newValue ->
            changeValue(newValue)
        }
    )
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
            .verticalScroll(rememberScrollState())
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
                    .height(400.dp)
                    .fillMaxWidth()
            )
            AverageList(
                tempList = averageUi.tempList,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AverageList(
    tempList: List<PastWeatherViewModel.GraphTempItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(8.dp),
    ) {
        for (item in tempList) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagPager(
    tagList: List<String>,
    pageList: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { pageList.size })
    var position by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
        ) {
            tagList.forEachIndexed { cnt, tag ->
                Tab(
                    selected = cnt == position,
                    onClick = {
                        scope.launch {
                            position = cnt
                            pagerState.animateScrollToPage(cnt)
                        }
                    },
                    text = { Text(text = tag) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            pageList[page]()
        }
    }
}

@Composable
fun NumberCounter(
    value: Int,
    maxValue: Int,
    minValue: Int,
    textColor: Color,
    fontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier,
    onValueChangedListener: (Int) -> Unit,
) {
    var count by remember { mutableIntStateOf(value - minValue) }
    val limit = maxValue - minValue
    val lastModifier = Modifier
        .size(80.dp, 184.dp)
        .padding(8.dp, 16.dp)
        .then(modifier)

    Column(
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = lastModifier
    ) {
        IconButton(
            onClick = {
                if (count > 0) {
                    count --
                    onValueChangedListener(minValue + count)
                }
            },
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "count down",
                modifier = Modifier.size(48.dp)
            )
        }
        Text(
            text = "${minValue + count}",
            color = textColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
        )
        IconButton(
            onClick = {
                if (count < limit) {
                    count ++
                    onValueChangedListener(minValue + count)
                }
            },
            modifier = Modifier.size(52.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "count up",
                modifier = Modifier.size(48.dp)
            )
        }

    }
}