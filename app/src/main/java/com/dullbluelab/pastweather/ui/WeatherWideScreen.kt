package com.dullbluelab.pastweather.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun WeatherWideScreen(
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
                state = routeUi,
                modifier = modifier.fillMaxSize()
            )
        }
        else -> {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                AveragePanel(
                    viewModel = viewModel,
                    modifier = Modifier.padding(16.dp).weight(1f)
                )
                FinderPanel(
                    viewModel = viewModel,
                    onChangeYear = onChangeYear,
                    onChangeLocation = onLocation,
                    modifier = Modifier.padding(16.dp).weight(1f)
                )
                PeakPanel(
                    viewModel = viewModel,
                    modifier = Modifier.padding(16.dp).weight(1f)
                )
            }
        }
    }
    if (routeUi.isInputDay) {
        WeatherDatePicker(viewModel)
    }
}
