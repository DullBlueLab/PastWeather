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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dullbluelab.pastweather.data.LocationTable
import com.dullbluelab.pastweather.R

@Composable
fun LocationScreen(
    showDownload: () -> Unit,
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val locationUi by viewModel.locationUi.collectAsState()
    var isDialogVisible by remember { mutableStateOf(false) }

    Column(

        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ){
        Text(
            text = stringResource(id = R.string.label_getting),
            modifier = Modifier.padding(8.dp)
        )
        EntryLocationList(
            select = { code -> viewModel.changeSelectPoint(code) },
            onDelete = { item -> viewModel.deleteDataAt(item) },
            locationUi = locationUi,
            modifier = Modifier.padding(8.dp)
        )
        Spacer(
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(id = R.string.label_other),
            modifier = Modifier.padding(8.dp)
        )
        OtherLocationList(
            select = { item ->
                viewModel.selectLocationItem = item
                isDialogVisible = true
            },
            locationUi = locationUi,
            modifier = Modifier.padding(8.dp)
        )
    }
    if (isDialogVisible && viewModel.selectLocationItem != null) {
        AskDownloadDialog(
            item = viewModel.selectLocationItem!!,
            onCancel = {
                viewModel.selectLocationItem = null
                isDialogVisible = false
            },
            onSubmit = {
                showDownload()
                viewModel.downloadWeatherData(item = viewModel.selectLocationItem!!)
                viewModel.selectLocationItem = null
                isDialogVisible = false
            }
        )
    }
}

@Composable
private fun EntryLocationList(
    select: (String) -> Unit,
    onDelete: (LocationTable) -> Unit,
    locationUi: PastWeatherViewModel.LocationUiState,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .fillMaxWidth()
    ) {
        locationUi.entryList.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = item.code == locationUi.pointCode,
                        onClick = { select(item.code) },
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
                if (item.code != locationUi.pointCode) {
                    IconButton(
                        onClick = { onDelete(item) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.button_delete_data)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherLocationList(
    select: (LocationTable) -> Unit,
    locationUi: PastWeatherViewModel.LocationUiState,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .fillMaxWidth()
    ) {
        locationUi.otherList.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = item.code == locationUi.pointCode,
                        onClick = { select(item) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 24.dp, 16.dp)
            ) {
                Text(text = item.name)
            }
        }
    }
}

@Composable
private fun AskDownloadDialog(
    item: LocationTable,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    val text = stringResource(id = R.string.text_ask_download_f) +
            item.name +
            stringResource(id = R.string.text_ask_download_r)

    AlertDialog(
        text = {
            Text(text = text)
        },
        onDismissRequest = { onCancel() },
        dismissButton = {
                        TextButton(onClick = { onCancel() }) {
                            Text(stringResource(id = R.string.button_cancel))
                        }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit() }) {
                Text(stringResource(id = R.string.button_download))
            }
        }
    )
}
