package com.dullbluelab.pastweather.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
            DownloadPanel(
                cancel = {
                    viewModel.checkAndCancelDownload()
                    Toast.makeText(context, R.string.text_cancel_download, Toast.LENGTH_SHORT)
                        .show()
                    finish()
                },
                progressCount = downloadUi.progressCount,
                modifier = modifier
            )
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
    cancel: () -> Unit,
    progressCount: Int,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.text_download)
        )
        Text(
            text = "${progressCount}%"
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = { cancel() }
        ) {
            Text(
                text = stringResource(id = R.string.button_cancel)
            )
        }
    }

}