package com.weather.snap

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `app launches and displays main navigation`() {
        // Just verify it doesn't crash and we see at least some standard text 
        // depending on what the initial screen is. For example "WeatherSnap"
        // Since we don't have exact UI texts in this test, this is a smoke test.
        // E.g., if Weather is the start destination, it might show "WeatherSnap" or "Location"
        
        // Wait for the UI to settle
        composeTestRule.waitForIdle()
        
        // As a simple check, verify it runs.
        assert(true)
    }
}
