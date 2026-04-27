package com.velopass.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.velopass.app.ui.navigation.RootNavigation
import com.velopass.app.ui.theme.VeloPassTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link from OAuth callback
        handleDeepLink(intent.data)
        
        setContent {
            VeloPassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigation()
                }
            }
        }
    }

    private fun handleDeepLink(uri: android.net.Uri?) {
        if (uri != null) {
            Log.d("MainActivity", "Deep link: $uri")
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            
            if (code != null) {
                Log.d("MainActivity", "OAuth code: $code")
                // The BikeIndexViewModel will handle the callback via StateFlow
                // This is just for logging/debugging
            }
        }
    }
}
