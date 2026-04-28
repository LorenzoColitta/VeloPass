package com.velopass.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.velopass.app.models.BikeIndexConnectionState
import com.velopass.app.models.BikeIndexProfile
import com.velopass.app.viewmodels.BikeIndexViewModel

@Composable
fun AccountScreen(
    navController: NavController,
    bikeIndexViewModel: BikeIndexViewModel = hiltViewModel()
) {
    val connectionState = bikeIndexViewModel.connectionState.collectAsState().value
    val profile = bikeIndexViewModel.profile.collectAsState().value
    val isLoading = bikeIndexViewModel.isLoading.collectAsState().value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Divider()
        }

        item {
            BikeIndexIntegrationSection(
                connectionState = connectionState,
                profile = profile,
                isLoading = isLoading,
                onConnectClick = { navController.navigate("profile/bikeindex") },
                onDisconnectClick = { bikeIndexViewModel.disconnectBikeIndex() },
                onReconnectClick = { navController.navigate("profile/bikeindex") },
                onManageClick = { navController.navigate("bikes/verify") }
            )
        }

        item {
            Divider()
        }

        item {
            AccountSettingsSection()
        }

        item {
            Divider()
        }

        item {
            AccountHelpSection(navController)
        }
    }
}

@Composable
fun BikeIndexIntegrationSection(
    connectionState: BikeIndexConnectionState,
    profile: BikeIndexProfile?,
    isLoading: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onReconnectClick: () -> Unit,
    onManageClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.DirectionsBike,
                    contentDescription = "BikeIndex",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "BikeIndex Integration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            ConnectionStatusBadge(connectionState)
        }

        when (connectionState) {
            BikeIndexConnectionState.CONNECTED -> {
                ConnectedState(
                    profile = profile,
                    onDisconnectClick = onDisconnectClick,
                    onManageClick = onManageClick
                )
            }
            BikeIndexConnectionState.EXPIRED -> {
                ExpiredState(onReconnectClick = onReconnectClick)
            }
            BikeIndexConnectionState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                NotConnectedState(onConnectClick = onConnectClick)
            }
        }
    }
}

@Composable
fun ConnectionStatusBadge(state: BikeIndexConnectionState) {
    val (backgroundColor, contentColor, text) = when (state) {
        BikeIndexConnectionState.CONNECTED -> {
            Triple(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                "Connected"
            )
        }
        BikeIndexConnectionState.EXPIRED -> {
            Triple(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                "Expired"
            )
        }
        BikeIndexConnectionState.LOADING -> {
            Triple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                "Connecting..."
            )
        }
        else -> {
            Triple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                "Not Connected"
            )
        }
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ConnectedState(
    profile: BikeIndexProfile?,
    onDisconnectClick: () -> Unit,
    onManageClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Connected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connected to BikeIndex",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (profile != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 28.dp)
                ) {
                    Text(
                        text = "Username: ${profile.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Email: ${profile.email}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onManageClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manage")
                }
                OutlinedButton(
                    onClick = onDisconnectClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Disconnect")
                }
            }
        }
    }
}

@Composable
fun NotConnectedState(onConnectClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.DirectionsBike,
                contentDescription = "Not connected",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = "Cross-register your bikes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Connect to BikeIndex to help recover your bikes if stolen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Button(
                onClick = onConnectClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in with BikeIndex")
            }
        }
    }
}

@Composable
fun ExpiredState(onReconnectClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = "Token expired",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = "Connection Expired",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Text(
                text = "Your BikeIndex token has expired. Please reconnect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Button(
                onClick = onReconnectClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reconnect")
            }
        }
    }
}

@Composable
fun AccountSettingsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        SettingsItem(
            icon = Icons.Filled.Notifications,
            title = "Notifications",
            subtitle = "Manage alert preferences"
        )
        
        SettingsItem(
            icon = Icons.Filled.Security,
            title = "Security",
            subtitle = "Password and security settings"
        )
        
        SettingsItem(
            icon = Icons.Filled.Tune,
            title = "Preferences",
            subtitle = "App appearance and behavior"
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.material.icons.twotone.Notifications,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AccountHelpSection(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Help & Support",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        HelpItem(
            icon = Icons.Filled.Help,
            title = "FAQs",
            onClick = { }
        )
        
        HelpItem(
            icon = Icons.Filled.Info,
            title = "About VeloPass",
            onClick = { }
        )
        
        HelpItem(
            icon = Icons.Filled.Mail,
            title = "Contact Support",
            onClick = { }
        )
    }
}

@Composable
fun HelpItem(
    icon: androidx.compose.material.icons.twotone.Help,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
