package com.dullbluelab.pastweather.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dullbluelab.pastweather.R
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun WeatherScreen(
    onChangeYear: (Int) -> Unit,
    onLocation: () -> Unit,
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val routeUi by viewModel.routeUi.collectAsState()

    when (routeUi.mode) {
        "point" -> {
            StartDownload(onLocation = onLocation, modifier = modifier)
        }
        "start" -> {
            StartPanel(
                modifier = modifier.fillMaxSize()
            )
        }
        else -> {
            TagPager(
                tagList = listOf(
                    stringResource(id = R.string.tag_finder),
                    stringResource(id = R.string.tag_average),
                    stringResource(id = R.string.tag_peak)
                ),
                pageList = listOf(
                    {
                        FinderPanel(
                            viewModel = viewModel,
                            onChangeYear = onChangeYear,
                            onChangeLocation = onLocation,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    {
                        AveragePanel(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    {
                        PeakPanel(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                ),
                modifier = modifier.fillMaxSize()
            )

        }
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

@Composable
private fun StartPanel(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp, 48.dp)
        )
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
