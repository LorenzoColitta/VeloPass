package com.velopass.app.ui.screens.bikeindex

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavController
import com.velopass.app.models.BikeIndexConnectionState
import com.velopass.app.models.BikeIndexProfile
import com.velopass.app.ui.theme.VeloPassTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BikeIndexConnectScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavController

    @Before
    fun setUp() {
        navController = TestNavController(MainActivity())
    }

    @Test
    fun testNotConnectedStateDisplays() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Not connected to BikeIndex").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in with BikeIndex").assertIsDisplayed()
    }

    @Test
    fun testConnectedStateDisplaysProfile() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("john_doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }

    @Test
    fun testSignInButtonClickable() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Sign in with BikeIndex")
            .performClick()
    }

    @Test
    fun testDisconnectButtonVisible() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Disconnect from BikeIndex")
            .assertIsDisplayed()
    }

    @Test
    fun testErrorMessageDisplays() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Connection error").assertIsDisplayed()
    }

    @Test
    fun testExpiredTokenStateDisplays() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Connection expired").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reconnect").assertIsDisplayed()
    }

    @Test
    fun testLoadingIndicatorDisplays() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..100f)))
            .assertIsDisplayed()
    }

    @Test
    fun testBackNavigationWorks() {
        val navController = TestNavController(MainActivity())
        navController.navigate("profile/bikeindex")

        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexConnectScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        assert(navController.previousBackStackEntry != null)
    }
}
