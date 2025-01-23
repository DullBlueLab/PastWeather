package com.dullbluelab.pastweather.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dullbluelab.pastweather.R

@Composable
fun InfoScreen(
    openHomepage: () -> Unit,
    resetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.name_tool),
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp, 8.dp)
        )
        Text(
            text = stringResource(id = R.string.text_clear_data),
            modifier = Modifier.padding(32.dp, 8.dp)
        )
        Button(
            onClick = { resetData() },
            modifier = Modifier
                .padding(32.dp, 8.dp)
                .align(Alignment.End)
        ) {
            Text(text = stringResource(id = R.string.name_clear))
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(id = R.string.name_info),
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp, 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 8.dp)
        ) {
            Text(text = "presented by ")

            TextButton(onClick = { openHomepage() }) {
                Text(text = "Dull Blue Lab")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}