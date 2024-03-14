package com.dullbluelab.pastweather

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dullbluelab.pastweather.ui.PastWeatherApp
import com.dullbluelab.pastweather.ui.theme.PastWeatherTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        val homepageUrl = getString(R.string.homepage_url)

        super.onCreate(savedInstanceState)
        setContent {
            PastWeatherTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PastWeatherApp(
                        openHomepage = {
                            openWebPage(homepageUrl)
                        }
                    )
                }
            }
        }
    }

    private fun openWebPage(url: String) {
        try {
            val webpage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            startActivity(intent)
        }
        catch (e: Exception) {
            val message = getString(R.string.error_browse)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
