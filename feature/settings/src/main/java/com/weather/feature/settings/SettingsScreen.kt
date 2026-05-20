package com.weather.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.designsystem.theme.*

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onFahrenheitChange = viewModel::setUseFahrenheit,
        onAutoSyncChange = viewModel::setAutoSyncEnabled,
        onSyncIntervalChange = viewModel::setSyncIntervalMinutes
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onFahrenheitChange: (Boolean) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onSyncIntervalChange: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = OnSurfaceColor) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Temperature Unit Section
            SettingsSection(title = "DISPLAY") {
                SettingsToggleItem(
                    title = "Temperature Unit",
                    subtitle = if (uiState.useFahrenheit) "Fahrenheit (°F)" else "Celsius (°C)",
                    icon = Icons.Default.Favorite,
                    checked = uiState.useFahrenheit,
                    onCheckedChange = onFahrenheitChange
                )
            }

            // Sync Section
            SettingsSection(title = "SYNC") {
                SettingsToggleItem(
                    title = "Auto Sync",
                    subtitle = "Automatically sync pending reports",
                    icon = Icons.Default.Refresh,
                    checked = uiState.autoSyncEnabled,
                    onCheckedChange = onAutoSyncChange
                )

                if (uiState.autoSyncEnabled) {
                    SettingsSliderItem(
                        title = "Sync Interval",
                        subtitle = "${uiState.syncIntervalMinutes} minutes",
                        icon = Icons.Default.DateRange,
                        value = uiState.syncIntervalMinutes.toFloat(),
                        valueRange = 5f..60f,
                        steps = 10,
                        onValueChange = { onSyncIntervalChange(it.toInt()) }
                    )
                }
            }

            // About Section
            SettingsSection(title = "ABOUT") {
                SettingsInfoItem(
                    title = "Version",
                    value = "1.0.0",
                    icon = Icons.Default.Info
                )
                SettingsInfoItem(
                    title = "Build",
                    value = "Debug",
                    icon = Icons.Default.Build
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariantColor,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PrimaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = OnSurfaceColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = OnSurfaceVariantColor
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryColor,
                checkedTrackColor = PrimaryColor.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PrimaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = OnSurfaceColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = OnSurfaceVariantColor
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryColor,
                    activeTrackColor = PrimaryColor
                )
            )
        }
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = OnSurfaceVariantColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = OnSurfaceColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = OnSurfaceVariantColor
        )
    }
}