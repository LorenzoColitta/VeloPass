package com.velopass.app.ui.screens.bikeindex

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavController
import com.velopass.app.ui.theme.VeloPassTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BikeIndexVerifyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavController

    @Before
    fun setUp() {
        navController = TestNavController(MainActivity())
    }

    @Test
    fun testSearchInputAcceptsSerial() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithTag("serial_input")
            .performTextInput("ABC123XYZ")
        
        composeTestRule.onNodeWithTag("serial_input")
            .assertTextEquals("ABC123XYZ")
    }

    @Test
    fun testSearchButtonEnabledWithInput() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithTag("serial_input")
            .performTextInput("ABC123XYZ")

        composeTestRule.onNodeWithText("Search")
            .assertIsEnabled()
    }

    @Test
    fun testSearchButtonDisabledWithoutInput() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Search")
            .assertIsNotEnabled()
    }

    @Test
    fun testEmptyStateDisplays() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("No bikes found")
            .assertIsDisplayed()
    }

    @Test
    fun testResultsDisplayCorrectly() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Trek FX 3 2023")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Trek")
            .assertIsDisplayed()
    }

    @Test
    fun testViewOnBikeIndexButtonWorks() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("View on BikeIndex")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun testLoadingIndicatorShowsDuringSearch() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithTag("serial_input")
            .performTextInput("TEST123")
        
        composeTestRule.onNodeWithText("Search")
            .performClick()

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..100f)))
            .assertIsDisplayed()
    }

    @Test
    fun testErrorMessageDisplays() {
        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Search failed")
            .assertIsDisplayed()
    }

    @Test
    fun testBackNavigationWorks() {
        navController.navigate("bikes/verify")

        composeTestRule.setContent {
            VeloPassTheme {
                BikeIndexVerifyScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        assert(navController.previousBackStackEntry != null)
    }
}
