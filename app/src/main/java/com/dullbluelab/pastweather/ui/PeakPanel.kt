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
import com.dullbluelab.pastweather.data.PeakDataCsv

private const val HH = 0
private const val HL = 1
private const val LH = 2
private const val LL = 3

@Composable
fun PeakPanel(
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.peakUi.collectAsState()

    uiState.data?.let { data ->

        if (data.years.size >= 4) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = uiState.pointName,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "${data.month}/${data.day}",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                PeakData(
                    data = data,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PeakData(
    data: PeakDataCsv.Table,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.label_high_temp),
            modifier = Modifier.padding(16.dp, 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.text_high_record)
            )
            Text(
                text = data.years[HH].toString(),
                fontSize = 24.sp
            )
            Text(
                text = data.temps[HH].toString() + stringResource(id = R.string.temp_char),
                fontSize = 24.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.text_low_record)
            )
            Text(
                text = data.years[HL].toString(),
                fontSize = 24.sp
            )
            Text(
                text = data.temps[HL].toString() + stringResource(id = R.string.temp_char),
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.label_low_temp),
            modifier = Modifier.padding(16.dp, 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.text_high_record)
            )
            Text(
                text = data.years[LH].toString(),
                fontSize = 24.sp
            )
            Text(
                text = data.temps[LH].toString() + stringResource(id = R.string.temp_char),
                fontSize = 24.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.text_low_record)
            )
            Text(
                text = data.years[LL].toString(),
                fontSize = 24.sp
            )
            Text(
                text = data.temps[LL].toString() + stringResource(id = R.string.temp_char),
                fontSize = 24.sp
            )
        }
    }
}
