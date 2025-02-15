package com.dullbluelab.pastweather.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dullbluelab.pastweather.MainActivity
import com.dullbluelab.pastweather.R

enum class PastWeatherScreen {
    Weather, Aside, Location
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PastWeatherApp(
    activity: MainActivity,
    openHomepage: () -> Unit,
    viewModel: PastWeatherViewModel = viewModel(factory = PastWeatherViewModel.Factory)
) {
    val navController: NavHostController = rememberNavController()
    val state by viewModel.routeUi.collectAsState()
    val windowSizeClass = calculateWindowSizeClass(activity = activity)
    val textClearData = stringResource(id = R.string.text_success_reset)

    if (activity.destinationListener == null) {
        activity.destinationListener = DestinationListener(viewModel)
        navController.addOnDestinationChangedListener(activity.destinationListener!!)
    }

    Scaffold(
        topBar = {
            PastWeatherTopAppBar(
                currentScreen = state.route,
                canNavigateBack = (navController.currentBackStackEntry != null && state.route != "Weather"),
                navigateUp = {
                    navController.navigateUp()
                },
                onInfoButtonClicked = {
                    navController.navigate(PastWeatherScreen.Aside.name)
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = PastWeatherScreen.Weather.name,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                composable(route = PastWeatherScreen.Weather.name) {
                    when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Expanded -> {
                            WeatherWideScreen(
                                onChangeYear = { value -> viewModel.changeYear(value) },
                                onLocation = {
                                    navController.navigate(PastWeatherScreen.Location.name)
                                },
                                viewModel = viewModel,
                            )
                        }
                        else -> {
                            WeatherScreen(
                                onChangeYear = { value -> viewModel.changeYear(value) },
                                onLocation = {
                                    navController.navigate(PastWeatherScreen.Location.name)
                                },
                                viewModel = viewModel,
                            )
                        }
                    }
                }
                composable(route = PastWeatherScreen.Location.name) {
                    LocationScreen(
                        selectLocation = { code -> viewModel.changeLocation(code) },
                        viewModel = viewModel
                    )
                }
                composable(route = PastWeatherScreen.Aside.name) {
                    InfoScreen(
                        openHomepage = openHomepage,
                        resetData = { viewModel.resetData { message ->
                            if (message == "success") {
                                Toast.makeText(activity, textClearData, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } }
                    )
                }
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
) {
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(id = R.string.button_info)
                    )
                }
            }
        }
    )
}

class DestinationListener(
    private val viewModel: PastWeatherViewModel
)
    : NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val route = destination.route ?: "Weather"
        viewModel.updateRoute(route)
    }
}
