package com.velopass.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.velopass.app.ui.screens.onboarding.OnboardingScreen
import com.velopass.app.ui.screens.home.HomeScreen
import com.velopass.app.ui.screens.MyBikesScreen
import com.velopass.app.ui.screens.MaintenanceScreen
import com.velopass.app.ui.screens.DocumentsScreen
import com.velopass.app.ui.screens.ProfileScreen

@Composable
fun RootNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen()
        }

        composable("my_bikes") {
            MyBikesScreen()
        }

        composable("maintenance") {
            MaintenanceScreen()
        }

        composable("documents") {
            DocumentsScreen()
        }

        composable("profile") {
            ProfileScreen()
        }
    }
}
