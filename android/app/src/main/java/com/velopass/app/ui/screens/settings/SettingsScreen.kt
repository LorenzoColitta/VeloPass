package com.velopass.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    var selectedSection by remember { mutableStateOf<String?>(null) }

    when (selectedSection) {
        "notifications" -> NotificationSettingsScreen(onBack = { selectedSection = null })
        "security" -> SecuritySettingsScreen(onBack = { selectedSection = null })
        "preferences" -> PreferencesSettingsScreen(onBack = { selectedSection = null })
        "about" -> AboutSettingsScreen(onBack = { selectedSection = null })
        else -> MainSettingsScreen(navController = navController, onSectionClick = { selectedSection = it })
    }
}

@Composable
fun MainSettingsScreen(
    navController: NavController,
    onSectionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Divider()
        }

        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            SettingsCard(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Push alerts and preferences",
                onClick = { onSectionClick("notifications") }
            )
        }

        item {
            SettingsCard(
                icon = Icons.Filled.Tune,
                title = "Preferences",
                subtitle = "Theme, language, accessibility",
                onClick = { onSectionClick("preferences") }
            )
        }

        item {
            Divider()
        }

        item {
            Text(
                text = "Account & Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            SettingsCard(
                icon = Icons.Filled.Security,
                title = "Security",
                subtitle = "Password, 2FA, sessions",
                onClick = { onSectionClick("security") }
            )
        }

        item {
            Divider()
        }

        item {
            Text(
                text = "Other",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            SettingsCard(
                icon = Icons.Filled.Info,
                title = "About VeloPass",
                subtitle = "Version, licenses, support",
                onClick = { onSectionClick("about") }
            )
        }
    }
}

@Composable
fun SettingsCard(
    icon: androidx.compose.material.icons.twotone.Notifications,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    var pushEnabled by remember { mutableStateOf(true) }
    var bikeAlertsEnabled by remember { mutableStateOf(true) }
    var securityAlertsEnabled by remember { mutableStateOf(true) }
    var dailyDigest by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Divider() }

        item {
            Text(
                text = "Push Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingToggle(
                title = "Enable Push Notifications",
                subtitle = "Receive notifications on your device",
                isChecked = pushEnabled,
                onCheckedChange = { pushEnabled = it }
            )
        }

        item {
            SettingToggle(
                title = "Bike Alerts",
                subtitle = "Notifications about your bikes",
                isChecked = bikeAlertsEnabled,
                onCheckedChange = { bikeAlertsEnabled = it },
                enabled = pushEnabled
            )
        }

        item {
            SettingToggle(
                title = "Security Alerts",
                subtitle = "Account security and login alerts",
                isChecked = securityAlertsEnabled,
                onCheckedChange = { securityAlertsEnabled = it },
                enabled = pushEnabled
            )
        }

        item { Divider() }

        item {
            Text(
                text = "Digest",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingToggle(
                title = "Daily Digest",
                subtitle = "Daily summary of activities",
                isChecked = dailyDigest,
                onCheckedChange = { dailyDigest = it }
            )
        }
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun SecuritySettingsScreen(onBack: () -> Unit) {
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Divider() }

        item {
            Text(
                text = "Authentication",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingToggle(
                title = "Biometric Authentication",
                subtitle = "Use fingerprint or face to unlock",
                isChecked = biometricEnabled,
                onCheckedChange = { biometricEnabled = it }
            )
        }

        item {
            SettingToggle(
                title = "Two-Factor Authentication",
                subtitle = "Add extra security to your account",
                isChecked = twoFactorEnabled,
                onCheckedChange = { twoFactorEnabled = it }
            )
        }

        item { Divider() }

        item {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingsButton(
                title = "Change Password",
                icon = Icons.Filled.VpnKey,
                onClick = { }
            )
        }

        item {
            SettingsButton(
                title = "Active Sessions",
                icon = Icons.Filled.Devices,
                onClick = { }
            )
        }

        item {
            SettingsButton(
                title = "Privacy Policy",
                icon = Icons.Filled.Description,
                onClick = { }
            )
        }
    }
}

@Composable
fun SettingsButton(
    title: String,
    icon: androidx.compose.material.icons.twotone.VpnKey,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun PreferencesSettingsScreen(onBack: () -> Unit) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var dynamicColorEnabled by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("English") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Divider() }

        item {
            Text(
                text = "Display",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingToggle(
                title = "Dark Mode",
                subtitle = "Use dark color scheme",
                isChecked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )
        }

        item {
            SettingToggle(
                title = "Dynamic Colors",
                subtitle = "Use wallpaper-based colors",
                isChecked = dynamicColorEnabled,
                onCheckedChange = { dynamicColorEnabled = it }
            )
        }

        item { Divider() }

        item {
            Text(
                text = "Language & Region",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingDropdown(
                title = "Language",
                currentValue = language,
                options = listOf("English", "Dutch", "German", "French"),
                onValueChange = { language = it }
            )
        }

        item { Divider() }

        item {
            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingsButton(
                title = "Clear Cache",
                icon = Icons.Filled.DeleteSweep,
                onClick = { }
            )
        }
    }
}

@Composable
fun SettingDropdown(
    title: String,
    currentValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(currentValue)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSettingsScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "About VeloPass",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Icon(
                Icons.Filled.DirectionsBike,
                contentDescription = "VeloPass",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        item {
            Text(
                text = "VeloPass",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

        item {
            Text(
                text = "Cross-register your bikes with BikeIndex to help recover them if stolen.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

        item {
            Text(
                text = "Links",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            SettingsButton(
                title = "Visit Website",
                icon = Icons.Filled.Language,
                onClick = { }
            )
        }

        item {
            SettingsButton(
                title = "Open Source Licenses",
                icon = Icons.Filled.Description,
                onClick = { }
            )
        }

        item {
            SettingsButton(
                title = "Contact Support",
                icon = Icons.Filled.Email,
                onClick = { }
            )
        }

        item {
            Text(
                text = "© 2026 VeloPass. All rights reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}
