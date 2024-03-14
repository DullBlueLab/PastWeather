package com.dullbluelab.pastweather.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dullbluelab.pastweather.R

enum class PastWeatherScreen {
    Weather, Info, Location, Download
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastWeatherApp(
    openHomepage: () -> Unit,
    viewModel: PastWeatherViewModel = viewModel(factory = PastWeatherViewModel.Factory)
) {
    val navController: NavHostController = rememberNavController()
    val state by viewModel.rootUi.collectAsState()
    val context = LocalContext.current

    if (viewModel.destinationListener == null) {
        viewModel.destinationListener = DestinationListener(context, viewModel)
        navController.addOnDestinationChangedListener(viewModel.destinationListener!!)
    }

    Scaffold(
        topBar = {
            PastWeatherTopAppBar(
                currentScreen = state.position,
                canNavigateBack = (navController.currentBackStackEntry != null && state.position != "Weather"),
                navigateUp = {
                    navController.navigateUp()
                },
                onInfoButtonClicked = {
                    navController.navigate(PastWeatherScreen.Info.name)
                },
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PastWeatherScreen.Weather.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(route = PastWeatherScreen.Weather.name) {
                WeatherScreen(
                    onChangeYear = { value -> viewModel.changeYear(value) },
                    onChangeLocation = {
                        navController.navigate(PastWeatherScreen.Location.name)
                    },
                    viewModel = viewModel,
                )
            }
            composable(route = PastWeatherScreen.Location.name) {
                LocationScreen(
                    showDownload = {
                        val route = PastWeatherScreen.Download.name
                        navController.navigate(route)
                    },
                    viewModel = viewModel
                )
            }
            composable(route = PastWeatherScreen.Info.name) {
                InfoScreen(
                    openHomepage = openHomepage
                )
            }
            composable(route = PastWeatherScreen.Download.name) {
                DownloadScreen(
                    finish = { navController.navigateUp() },
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastWeatherTopAppBar(
    currentScreen: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    onInfoButtonClicked: () -> Unit
)
{
    val title = when(currentScreen) {
        PastWeatherScreen.Weather.name -> stringResource(id = R.string.app_name)
        else -> currentScreen
    }

    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.button_back)
                    )
                }
            }
        },
        actions = {
            if (currentScreen == PastWeatherScreen.Weather.name) {
                IconButton(
                    onClick = {
                        onInfoButtonClicked()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = stringResource(id = R.string.button_info)
                    )
                }
            }
        }
    )
}

class DestinationListener(
    private val context: Context,
    private val viewModel: PastWeatherViewModel
)
    : NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val route = destination.route ?: "Weather"
        val prev = viewModel.rootUi.value.position
        val now = viewModel.downloadUi.value.status

        if (prev == "Download" && route != "Download" && now == "download") {
            viewModel.checkAndCancelDownload()
            Toast.makeText(context, R.string.text_cancel_download, Toast.LENGTH_SHORT)
                .show()
        }
        viewModel.updatePositionUi(route)
    }
}
