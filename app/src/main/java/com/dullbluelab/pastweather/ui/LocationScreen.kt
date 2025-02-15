package com.dullbluelab.pastweather.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun LocationScreen(
    selectLocation: (String) -> Unit,
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val locationUi by viewModel.locationUi.collectAsState()

    Column(

        modifier = modifier
            .selectableGroup()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ){
        Spacer(modifier = Modifier.size(16.dp))

        locationUi.list.forEach { item ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = item.code == locationUi.pointCode,
                        onClick = { selectLocation(item.code) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                RadioButton(
                    selected = item.code == locationUi.pointCode,
                    onClick = null,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.size(16.dp))
    }
}
