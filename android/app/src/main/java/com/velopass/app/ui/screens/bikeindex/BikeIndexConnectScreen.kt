package com.velopass.app.ui.screens.bikeindex

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.velopass.app.BuildConfig
import com.velopass.app.models.BikeIndexConnectionState
import com.velopass.app.viewmodels.BikeIndexViewModel

@Composable
fun BikeIndexConnectScreen(
    navController: NavController,
    viewModel: BikeIndexViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(error) {
        error?.let {
            // Could show a snackbar here
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "BikeIndex",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            }
            
            // Status Section
            when (connectionState) {
                BikeIndexConnectionState.NOT_CONNECTED -> {
                    item {
                        BikeIndexNotConnectedState(
                            onSignIn = {
                                openBikeIndexOAuth(context)
                            }
                        )
                    }
                }
                BikeIndexConnectionState.CONNECTED -> {
                    if (profile != null) {
                        item {
                            BikeIndexConnectedState(
                                profile = profile!!,
                                linkedBikesCount = 0,
                                onDisconnect = {
                                    viewModel.disconnectBikeIndex()
                                },
                                onViewBike = { url ->
                                    openCustomTab(context, url)
                                }
                            )
                        }
                    }
                }
                BikeIndexConnectionState.EXPIRED -> {
                    item {
                        BikeIndexExpiredState(
                            onReconnect = {
                                openBikeIndexOAuth(context)
                            }
                        )
                    }
                }
                else -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            
            // Error message
            error?.let {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BikeIndexNotConnectedState(
    onSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Filled.DirectionsBike,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            "Not connected to BikeIndex",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        
        Text(
            "Cross-register your bikes to help recover them if stolen",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Button(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Sign in with BikeIndex")
        }
    }
}

@Composable
private fun BikeIndexConnectedState(
    profile: com.velopass.app.models.BikeIndexProfile,
    linkedBikesCount: Int,
    onDisconnect: () -> Unit,
    onViewBike: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Connected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Username: ${profile.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Email: ${profile.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Linked bikes: $linkedBikesCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Disconnect button
        OutlinedButton(
            onClick = onDisconnect,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disconnect from BikeIndex")
        }
    }
}

@Composable
private fun BikeIndexExpiredState(
    onReconnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            "Connection expired",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        
        Text(
            "Your BikeIndex token has expired",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Button(
            onClick = onReconnect,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Reconnect")
        }
    }
}

private fun openBikeIndexOAuth(context: android.content.Context) {
    val authUrl = "https://bikeindex.org/oauth/authorize" +
            "?client_id=${BuildConfig.BIKEINDEX_CLIENT_ID}" +
            "&response_type=code" +
            "&redirect_uri=${BuildConfig.OAUTH_REDIRECT_URI}"
    
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(context, Uri.parse(authUrl))
}

private fun openCustomTab(context: android.content.Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder().build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}
