package com.weather.feature.weather

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.weather.core.common.Result
import com.weather.core.model.UserSettings
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherTelemetry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class WeatherHomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weatherHomeScreen_showsLoadingState() {
        composeTestRule.setContent {
            WeatherHomeScreen(
                uiState = WeatherUiState.Loading,
                searchQuery = "",
                searchResults = Result.Success(emptyList()),
                locationName = "Loading...",
                selectedTab = 0,
                onSearchQueryChange = {},
                onCitySelected = { _, _, _ -> },
                onCreateReportClicked = {},
                onTabSelected = {}
            )
        }

        // Search location placeholder should be visible
        composeTestRule.onNodeWithText("Search location...").assertIsDisplayed()
    }

    @Test
    fun weatherHomeScreen_showsSuccessState() {
        val telemetry = WeatherTelemetry(
            temperatureCelsius = 25.0,
            condition = WeatherCondition.CLEAR,
            humidityPercentage = 50,
            windSpeedKph = 15.0,
            latitude = 47.6,
            longitude = -122.3
        )
        val userSettings = UserSettings()

        composeTestRule.setContent {
            WeatherHomeScreen(
                uiState = WeatherUiState.Success(telemetry, userSettings),
                searchQuery = "",
                searchResults = Result.Success(emptyList()),
                locationName = "Seattle",
                selectedTab = 0,
                onSearchQueryChange = {},
                onCitySelected = { _, _, _ -> },
                onCreateReportClicked = {},
                onTabSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Seattle").assertIsDisplayed()
        // Default unit is Celsius
        composeTestRule.onNodeWithText("25").assertIsDisplayed()
        composeTestRule.onNodeWithText("CLEAR").assertIsDisplayed()
    }
}
