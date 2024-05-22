package com.dullbluelab.pastweather.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dullbluelab.pastweather.R

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun FinderPanel(
    viewModel: PastWeatherViewModel,
    onChangeYear: (Int) -> Unit,
    onChangeLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.finderUi.collectAsState()
    val today = "${uiState.selectMonth}/${uiState.selectDay}"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

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
            TextButton(
                onClick = { viewModel.inputDay(true) },
                modifier = Modifier.height(48.dp).padding(8.dp)
            ) {
                Text(
                    text = today,
                    fontSize = 18.sp
                )
            }
            FindYearPicker(viewModel) { value ->
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

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "${uiState.minYear} - ${uiState.maxYear}"
        )
        GraphCanvas(
            tempList = uiState.tempList,
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.clearSkyCount > 0) {
            Text(
                text = stringResource(id = R.string.text_clear_skies_count),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = uiState.clearSkyCount.toString(),
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        Text(
            text = stringResource(id = R.string.text_sunny_count),
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = uiState.sunnyCount.toString(),
            fontSize = 36.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.text_rainy_count),
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = uiState.rainyCount.toString(),
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
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
private fun NumberCounter(
    value: Int,
    maxValue: Int,
    minValue: Int,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    onValueChangedListener: (Int) -> Unit,
) {
    var count = value
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
                if (count > minValue) {
                    count --
                    onValueChangedListener(count)
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
            text = "$count",
            color = textColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier
        )
        IconButton(
            onClick = {
                if (count < maxValue) {
                    count ++
                    onValueChangedListener(count)
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
