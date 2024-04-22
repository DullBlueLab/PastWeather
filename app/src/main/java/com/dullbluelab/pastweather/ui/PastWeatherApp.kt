package com.dullbluelab.pastweather.ui

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dullbluelab.pastweather.MainActivity
import com.dullbluelab.pastweather.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

enum class PastWeatherScreen {
    Weather, Info, Location, Download
}

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastWeatherApp(
    activity: MainActivity,
    openHomepage: () -> Unit,
    viewModel: PastWeatherViewModel = viewModel(factory = PastWeatherViewModel.Factory)
) {
    val navController: NavHostController = rememberNavController()
    val state by viewModel.routeUi.collectAsState()

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
                    navController.navigate(PastWeatherScreen.Info.name)
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
                    WeatherScreen(
                        onChangeYear = { value -> viewModel.changeYear(value) },
                        onLocation = {
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
            AdmobBanner(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
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
                        imageVector = Icons.Filled.Info,
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

@Composable
fun  AdmobBanner(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            AdView(context).apply {
                // 下の行で広告ビューを指定します。広告サイズ
                //adSize = AdSize.BANNER
                // 下の行で広告ユニット ID を指定
                // 現在追加されているテスト広告ユニット ID。
                setAdSize(AdSize.BANNER)
                // adUnitId = "ca-app-pub-3940256099942544/9214589741" // test
                adUnitId = "ca-app-pub-5155739412996974/5785915157"
                // 呼び出し広告を読み込んで広告を読み込みます。
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}