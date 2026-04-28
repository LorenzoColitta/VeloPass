package com.velopass.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.velopass.app.ui.screens.onboarding.OnboardingScreen
import com.velopass.app.ui.screens.home.HomeScreen
import com.velopass.app.ui.screens.MyBikesScreen
import com.velopass.app.ui.screens.MaintenanceScreen
import com.velopass.app.ui.screens.DocumentsScreen
import com.velopass.app.ui.screens.ProfileScreen
import com.velopass.app.ui.screens.registration.RegistrationWizardScreen
import com.velopass.app.ui.screens.bikes.BikeDetailScreen
import com.velopass.app.ui.screens.bikeindex.BikeIndexConnectScreen
import com.velopass.app.ui.screens.bikeindex.BikeIndexVerifyScreen
import com.velopass.app.ui.screens.account.AccountScreen
import com.velopass.app.ui.screens.settings.SettingsScreen

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
            MyBikesScreen(navController = navController)
        }

        composable("bikes/register") {
            // TODO: Get userId, nationalityCode, legalResidenceCode from auth/session
            RegistrationWizardScreen(
                navController = navController,
                userId = "test-user-id",
                nationalityCode = "NL",
                legalResidenceCode = "NL"
            )
        }

        composable(
            "bikes/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val bikeId = backStackEntry.arguments?.getString("id") ?: ""
            BikeDetailScreen(
                bikeId = bikeId,
                navController = navController
            )
        }

        composable("maintenance") {
            MaintenanceScreen()
        }

        composable("documents") {
            DocumentsScreen()
        }

        composable("profile") {
            AccountScreen(navController = navController)
        }

        composable("profile/bikeindex") {
            BikeIndexConnectScreen(navController = navController)
        }

        composable("bikes/verify") {
            BikeIndexVerifyScreen(navController = navController)
        }

        composable("settings") {
            SettingsScreen(navController = navController)
        }
