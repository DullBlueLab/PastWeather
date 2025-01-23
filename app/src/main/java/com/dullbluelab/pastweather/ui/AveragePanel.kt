package com.dullbluelab.pastweather.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dullbluelab.pastweather.R

@Composable
fun AveragePanel(
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val averageUi by viewModel.averageUi.collectAsState()
    val days = " ${averageUi.selectMonth}/${averageUi.selectDay}"
    val years = "${averageUi.minYears} - ${averageUi.maxYears}"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = averageUi.pointName + days,
            modifier = Modifier
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))

        RecentAverage(
            state = averageUi,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = years,
            modifier = Modifier.padding(8.dp)
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

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RecentAverage(
    state: PastWeatherViewModel.AverageUiState,
    modifier: Modifier
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.text_recent_temp),
            modifier = Modifier.padding(16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_high_temp),
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = state.recentHighTemp.toString() + stringResource(id = R.string.temp_char),
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_low_temp),
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = state.recentLowTemp.toString() + stringResource(id = R.string.temp_char),
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
        }
    }
}
@Composable
private fun AverageList(
    tempList: List<GraphTempItem>,
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
