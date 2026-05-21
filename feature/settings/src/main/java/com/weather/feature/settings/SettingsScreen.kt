package com.weather.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.designsystem.component.WeatherSnapBottomNav
import com.weather.core.designsystem.component.WeatherSnapTab
import com.weather.core.designsystem.component.WeatherSnapTopBar
import com.weather.core.designsystem.responsive.*
import com.weather.core.designsystem.theme.*

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onFahrenheitChange = viewModel::setUseFahrenheit,
        onWindSpeedUnitChange = viewModel::setWindSpeedUnit,
        onPressureUnitChange = viewModel::setPressureUnit,
        onAutoCaptureTelemetryChange = viewModel::setAutoCaptureTelemetry,
        onSaveOriginalPhotosChange = viewModel::setSaveOriginalPhotos,
        onImageResolutionChange = viewModel::setImageResolution,
        onAutoSyncChange = viewModel::setAutoSyncEnabled,
        onGpsPrecisionModeChange = viewModel::setGpsPrecisionMode,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToReports = onNavigateToReports
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onFahrenheitChange: (Boolean) -> Unit,
    onWindSpeedUnitChange: (WindSpeedUnit) -> Unit,
    onPressureUnitChange: (PressureUnit) -> Unit,
    onAutoCaptureTelemetryChange: (Boolean) -> Unit,
    onSaveOriginalPhotosChange: (Boolean) -> Unit,
    onImageResolutionChange: (ImageResolution) -> Unit,
    onAutoSyncChange: (Boolean) -> Unit,
    onGpsPrecisionModeChange: (GpsPrecisionMode) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
        ?: return
    val windowSizeClass = calculateWindowSizeClass(activity)
    val responsive = calculateResponsiveValues(windowSizeClass)
    val fontScale = when {
        responsive.isExpanded -> 1.1f
        responsive.isMedium -> 1.05f
        else -> 1f
    }

    Scaffold(
        topBar = {
            WeatherSnapTopBar(title = "Settings", responsive = responsive)
        },
        bottomBar = {
            WeatherSnapBottomNav(
                selectedTab = WeatherSnapTab.Settings,
                onNavigateToHome = onNavigateToHome,
                onNavigateToCamera = onNavigateToCamera,
                onNavigateToReports = onNavigateToReports,
                onNavigateToSettings = {},
                responsive = responsive
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(responsive.screenPadding),
            verticalArrangement = Arrangement.spacedBy(responsive.sectionSpacing)
        ) {
            // User Profile Card
            UserProfileCard(responsive = responsive, fontScale = fontScale)

            // UNIT PREFERENCES
            SettingsSection(title = "UNIT PREFERENCES", responsive = responsive) {
                SettingsDropdownItem(
                    title = "Temperature",
                    selectedOption = if (uiState.useFahrenheit) "Fahrenheit (°F)" else "Celsius (°C)",
                    icon = { SettingsThermometerIcon(tint = OnSurfaceVariantColor, iconSize = responsive.iconSize) },
                    options = listOf("Celsius (°C)", "Fahrenheit (°F)"),
                    onOptionSelected = { label -> onFahrenheitChange(label == "Fahrenheit (°F)") },
                    responsive = responsive,
                    fontScale = fontScale
                )
                SectionDivider()
                SettingsDropdownItem(
                    title = "Wind Speed",
                    selectedOption = uiState.windSpeedUnit.label,
                    icon = { SettingsWindIcon(tint = OnSurfaceVariantColor, iconSize = responsive.iconSize) },
                    options = WindSpeedUnit.values().map { it.label },
                    onOptionSelected = { label -> onWindSpeedUnitChange(WindSpeedUnit.values().first { it.label == label }) },
                    responsive = responsive,
                    fontScale = fontScale
                )
                SectionDivider()
                SettingsDropdownItem(
                    title = "Pressure",
                    selectedOption = uiState.pressureUnit.label,
                    icon = { SettingsPressureIcon(tint = OnSurfaceVariantColor, iconSize = responsive.iconSize) },
                    options = PressureUnit.values().map { it.label },
                    onOptionSelected = { label -> onPressureUnitChange(PressureUnit.values().first { it.label == label }) },
                    responsive = responsive,
                    fontScale = fontScale
                )
            }

            // CAMERA & MEDIA
            SettingsSection(title = "CAMERA & MEDIA", responsive = responsive) {
                SettingsToggleItem(
                    title = "Auto-capture telemetry",
                    subtitle = "Embed data in EXIF",
                    icon = { Icon(Icons.Default.Camera, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize)) },
                    checked = uiState.autoCaptureTelemetry,
                    onCheckedChange = onAutoCaptureTelemetryChange,
                    responsive = responsive,
                    fontScale = fontScale
                )
                SectionDivider()
                SettingsToggleItem(
                    title = "Save original photos",
                    subtitle = "Keep untouched copy in gallery",
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize)) },
                    checked = uiState.saveOriginalPhotos,
                    onCheckedChange = onSaveOriginalPhotosChange,
                    responsive = responsive,
                    fontScale = fontScale
                )
                SectionDivider()
                SettingsDropdownItem(
                    title = "Image Resolution",
                    selectedOption = uiState.imageResolution.label,
                    icon = { Icon(Icons.Default.HighQuality, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize)) },
                    options = ImageResolution.values().map { it.label },
                    onOptionSelected = { label -> onImageResolutionChange(ImageResolution.values().first { it.label == label }) },
                    responsive = responsive,
                    fontScale = fontScale
                )
            }

            // FIELD DATA & SYNC
            SettingsSection(title = "FIELD DATA & SYNC", responsive = responsive) {
                SettingsToggleItem(
                    title = "Offline Report Sync",
                    subtitle = "Queue when offline",
                    icon = { Icon(Icons.Default.SyncDisabled, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize)) },
                    checked = uiState.autoSyncEnabled,
                    onCheckedChange = onAutoSyncChange,
                    responsive = responsive,
                    fontScale = fontScale
                )
                SectionDivider()
                SettingsDropdownItem(
                    title = "GPS Precision Mode",
                    selectedOption = uiState.gpsPrecisionMode.label,
                    icon = { Icon(Icons.Default.GpsFixed, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize)) },
                    options = GpsPrecisionMode.values().map { it.label },
                    onOptionSelected = { label -> onGpsPrecisionModeChange(GpsPrecisionMode.values().first { it.label == label }) },
                    responsive = responsive,
                    fontScale = fontScale
                )
            }

            // ACCOUNT & LEGAL
            SettingsSection(title = "ACCOUNT & LEGAL", responsive = responsive) {
                SettingsInfoItem(
                    title = "App Version",
                    value = "v2.4.1-field",
                    icon = { Icon(Icons.Default.Info, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize)) },
                    responsive = responsive,
                    fontScale = fontScale
                )
            }
            
            Spacer(modifier = Modifier.height(responsive.buttonHeight * 1.2f))
        }
    }
}

@Composable
private fun UserProfileCard(responsive: ResponsiveValues, fontScale: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = WeatherSnapColors.SurfaceContainer),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(responsive.cardPadding).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
            ) {
                coil.compose.AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBd4X6xCdhz8cQhUzKkjXfHV4fHWzjzViMuonMvFEP9UtVs0O2sbnVeF6zv4CXiWtZS-9x8FPszNYz63A5oB7f0aOIq102liqwa9YmAblBIY2A_U4ovPzd2OiYnKbd08MOZq4tICsoBiPS8WNZG37KRKE6v9zw06jp5WfysYC7QvIZeVqNZzuNA8u57AOA4mEZpWj8YFpthRllR8RqIZqrn-HRpPyZqB7mWRGaGjWNP04cZU2HAss1wz1ruPLD3cuy7ioUwXi2xWeM",
                    contentDescription = "Profile",
                    modifier = Modifier.size(responsive.avatarSize).clip(CircleShape).border(2.dp, PrimaryColor, CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alex Rivera", fontSize = (20 * fontScale).sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(responsive.itemSpacing / 4))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize * 0.7f))
                        Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
                        Text("Field Station 04", fontSize = (14 * fontScale).sp, color = OnSurfaceVariantColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.height(responsive.itemSpacing / 8))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terrain, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize * 0.7f))
                        Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
                        Text("Lead Meteorologist", fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.5f))
}

@Composable
private fun SettingsSection(
    title: String,
    responsive: ResponsiveValues,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurfaceVariantColor,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = responsive.itemSpacing / 2)
        )
        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius)),
            colors = CardDefaults.cardColors(containerColor = WeatherSnapColors.SurfaceContainer),
            shape = RoundedCornerShape(responsive.cardCornerRadius)
        ) {
            Column(
                modifier = Modifier.padding(vertical = responsive.itemSpacing / 4),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(responsive.itemSpacing))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = (15 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = WeatherSnapColors.SecondaryContainer,
                uncheckedThumbColor = OnSurfaceVariantColor,
                uncheckedTrackColor = WeatherSnapColors.SurfaceContainerHigh,
                uncheckedBorderColor = OutlineVariantColor
            )
        )
    }
}

@Composable
private fun SettingsDropdownItem(
    title: String,
    selectedOption: String,
    icon: @Composable () -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            icon()
            Spacer(modifier = Modifier.width(responsive.itemSpacing))
            Text(
                text = title,
                fontSize = (15 * fontScale).sp,
                color = OnSurfaceColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(WeatherSnapColors.SurfaceContainerHigh, RoundedCornerShape(6.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = responsive.cardPadding / 2, vertical = responsive.itemSpacing / 2)
        ) {
            Text(
                text = selectedOption,
                fontSize = (13 * fontScale).sp,
                color = OnSurfaceColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 112.dp)
            )
            Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize * 0.9f))
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(WeatherSnapColors.SurfaceContainerHigh)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = OnSurfaceColor) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(responsive.itemSpacing))
        Text(text = title, fontSize = (15 * fontScale).sp, color = OnSurfaceColor, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize))
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(responsive.itemSpacing))
        Text(text = title, fontSize = (15 * fontScale).sp, color = OnSurfaceColor, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor)
    }
}

@Composable
private fun SettingsThermometerIcon(tint: Color, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 20.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.5.dp.toPx()
        val bulbRadius = this.size.width * 0.25f
        val tubeRadius = this.size.width * 0.12f
        drawCircle(color = tint, radius = bulbRadius, center = androidx.compose.ui.geometry.Offset(this.size.width / 2, this.size.height - bulbRadius - 2.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width / 2 - tubeRadius, this.size.height - bulbRadius * 1.5f - 2.dp.toPx()), end = androidx.compose.ui.geometry.Offset(this.size.width / 2 - tubeRadius, bulbRadius + 2.dp.toPx()), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width / 2 + tubeRadius, this.size.height - bulbRadius * 1.5f - 2.dp.toPx()), end = androidx.compose.ui.geometry.Offset(this.size.width / 2 + tubeRadius, bulbRadius + 2.dp.toPx()), strokeWidth = strokeWidth)
        drawArc(color = tint, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(this.size.width / 2 - tubeRadius, bulbRadius + 2.dp.toPx() - tubeRadius), size = androidx.compose.ui.geometry.Size(tubeRadius * 2, tubeRadius * 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
        drawCircle(color = tint, radius = bulbRadius * 0.5f, center = androidx.compose.ui.geometry.Offset(this.size.width / 2, this.size.height - bulbRadius - 2.dp.toPx()))
    }
}

@Composable
private fun SettingsWindIcon(tint: Color, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 20.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.5.dp.toPx()
        val y1 = this.size.height * 0.3f
        val y2 = this.size.height * 0.5f
        val y3 = this.size.height * 0.7f
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width * 0.1f, y1), end = androidx.compose.ui.geometry.Offset(this.size.width * 0.9f, y1), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width * 0.2f, y2), end = androidx.compose.ui.geometry.Offset(this.size.width * 0.8f, y2), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width * 0.15f, y3), end = androidx.compose.ui.geometry.Offset(this.size.width * 0.75f, y3), strokeWidth = strokeWidth)
    }
}

@Composable
private fun SettingsPressureIcon(tint: Color, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 20.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.5.dp.toPx()
        val cx = this.size.width / 2
        val cy = this.size.height / 2
        val r = this.size.width * 0.4f
        drawCircle(color = tint, radius = r, center = androidx.compose.ui.geometry.Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx, cy - r + 2.dp.toPx()), end = androidx.compose.ui.geometry.Offset(cx, cy), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx, cy), end = androidx.compose.ui.geometry.Offset(cx + r * 0.5f, cy + r * 0.5f), strokeWidth = strokeWidth)
    }
}
