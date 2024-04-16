package com.dullbluelab.pastweather.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dullbluelab.pastweather.R

@Composable
fun DownloadScreen(
    finish: () -> Unit,
    viewModel: PastWeatherViewModel,
    modifier: Modifier = Modifier
) {
    val downloadUi by viewModel.downloadUi.collectAsState()
    val context = LocalContext.current

    when (downloadUi.status) {
        "download" -> {
            DownloadPanel(modifier = modifier)
        }

        "success" -> {
            viewModel.clearDownloadStatus()
            Toast.makeText(context, R.string.text_success_download, Toast.LENGTH_SHORT).show()
            finish()
        }

        "failed" -> {
            viewModel.clearDownloadStatus()
            Toast.makeText(context, downloadUi.message, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}


@Composable
private fun DownloadPanel(
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp, 48.dp)
        )
    }
}