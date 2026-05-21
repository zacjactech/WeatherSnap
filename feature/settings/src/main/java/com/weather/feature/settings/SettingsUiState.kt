package com.weather.feature.settings

import com.weather.core.common.UiState

/**
 * UI state for the Settings screen.
 *
 * All preferences are stored via [SavedStateHandle] in [SettingsViewModel]
 * so they survive process death.
 */
data class SettingsUiState(
    // Display / units
    val useFahrenheit: Boolean = false,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,

    // Camera & media
    val autoCaptureTelemetry: Boolean = true,
    val saveOriginalPhotos: Boolean = true,
    val imageResolution: ImageResolution = ImageResolution.STANDARD,

    // Sync
    val autoSyncEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 15,
    val gpsPrecisionMode: GpsPrecisionMode = GpsPrecisionMode.STANDARD
) : UiState

enum class WindSpeedUnit(val label: String) {
    KMH("km/h"),
    MPH("mph"),
    MS("m/s")
}

enum class PressureUnit(val label: String) {
    HPA("hPa"),
    INHG("inHg"),
    MMHG("mmHg")
}

enum class ImageResolution(val label: String) {
    STANDARD("Standard (JPEG)"),
    PRO("Pro (RAW + JPG)")
}

enum class GpsPrecisionMode(val label: String) {
    STANDARD("Standard"),
    HIGH_ACCURACY("High Accuracy")
}
