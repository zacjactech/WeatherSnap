package com.weather.feature.weather

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate as modifierRotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate as drawScopeRotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.common.Result
import com.weather.core.designsystem.component.WeatherSnapBottomNav
import com.weather.core.designsystem.component.WeatherSnapTopBar
import com.weather.core.designsystem.component.WeatherSnapTab
import com.weather.core.designsystem.responsive.*
import com.weather.core.designsystem.theme.*
import com.weather.core.model.LocationSearchResult
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherTelemetry
import coil.compose.AsyncImage

@Composable
fun WeatherHomeRoute(
    viewModel: WeatherViewModel = hiltViewModel(),
    onCreateReportClicked: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val locationName by viewModel.locationName.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    WeatherHomeScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        searchResults = searchResults,
        locationName = locationName,
        selectedTab = selectedTab,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onCitySelected = { lat, lon, name -> viewModel.updateCoordinates(lat, lon, name) },
        onCreateReportClicked = {
            viewModel.createWeatherDraft()
            onCreateReportClicked()
        },
        onTabSelected = viewModel::selectTab,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToReports = onNavigateToReports,
        onNavigateToSettings = onNavigateToSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WeatherHomeScreen(
    uiState: WeatherUiState,
    searchQuery: String,
    searchResults: Result<List<LocationSearchResult>>,
    locationName: String,
    selectedTab: Int,
    onSearchQueryChange: (String) -> Unit,
    onCitySelected: (Double, Double, String) -> Unit,
    onCreateReportClicked: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onNavigateToCamera: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as ComponentActivity)
    val responsive = calculateResponsiveValues(windowSizeClass)
    val fontScale = when {
        responsive.isExpanded -> 1.1f
        responsive.isMedium -> 1.05f
        else -> 1f
    }

    Scaffold(
        topBar = {
            WeatherSnapTopBar(
                responsive = responsive
            )
        },
        bottomBar = {
            WeatherSnapBottomNav(
                selectedTab = WeatherSnapTab.entries[selectedTab],
                onNavigateToHome = { onTabSelected(0) },
                onNavigateToCamera = onNavigateToCamera,
                onNavigateToReports = onNavigateToReports,
                onNavigateToSettings = onNavigateToSettings,
                responsive = responsive
            )
        },
        containerColor = BackgroundColor,
        contentColor = OnSurfaceColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HeroSection(
                uiState = uiState,
                locationName = locationName,
                searchQuery = searchQuery,
                searchResults = searchResults,
                onSearchQueryChange = onSearchQueryChange,
                onCitySelected = onCitySelected,
                responsive = responsive,
                fontScale = fontScale
            )

            AnimatedVisibility(
                visible = uiState is WeatherUiState.Success,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.padding(horizontal = responsive.screenPadding, vertical = responsive.sectionSpacing)) {
                    MetricsGrid(telemetry = (uiState as? WeatherUiState.Success)?.telemetry, responsive = responsive)
                }
            }

            Spacer(modifier = Modifier.weight(0.25f))

            CreateReportButton(onClick = onCreateReportClicked, responsive = responsive)

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroSection(
    uiState: WeatherUiState,
    locationName: String,
    searchQuery: String,
    searchResults: Result<List<LocationSearchResult>>,
    onSearchQueryChange: (String) -> Unit,
    onCitySelected: (Double, Double, String) -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(responsive.heroHeight * 1.35f),
        contentAlignment = Alignment.Center
    ) {
        val animCondition = (uiState as? WeatherUiState.Success)?.telemetry?.condition ?: com.weather.core.model.WeatherCondition.CLEAR

        AsyncImage(
            model = getBackgroundImageUrlForCondition(animCondition),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BackgroundColor.copy(alpha = 0.25f),
                            BackgroundColor.copy(alpha = 0.45f),
                            BackgroundColor.copy(alpha = 0.78f),
                            BackgroundColor.copy(alpha = 0.95f)
                        )
                    )
                )
        )
        // Additional dark overlay to improve text legibility
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        // Dynamic Weather Animation Background based on actual state condition
        WeatherAnimationBackground(
            condition = animCondition,
            modifier = Modifier.matchParentSize()
        )

        when (uiState) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(color = PrimaryColor)
            }
            is WeatherUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = WeatherSnapColors.Error,
                        modifier = Modifier.size(responsive.avatarSize)
                    )
                    Spacer(modifier = Modifier.height(responsive.itemSpacing / 2))
                    Text(
                        text = uiState.message,
                        color = WeatherSnapColors.Error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = responsive.screenPadding * 2)
                    )
                }
            }
            is WeatherUiState.Success -> {
                WeatherDisplay(telemetry = uiState.telemetry, locationName = locationName, fontScale = fontScale)
            }
        }

        SearchBarOverlay(
            searchQuery = searchQuery,
            searchResults = searchResults,
            onSearchQueryChange = onSearchQueryChange,
            onCitySelected = onCitySelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = responsive.screenPadding, vertical = responsive.itemSpacing / 2)
        )
    }
}

@Composable
private fun WeatherDisplay(telemetry: WeatherTelemetry, locationName: String, fontScale: Float) {
    val displayName = locationName.ifEmpty { "Seattle, WA" }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = (15 * fontScale).sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.6f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                    blurRadius = 10f
                )
            ),
            color = OnSurfaceColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = OnSurfaceVariantColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Field Station 04",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (15 * fontScale).sp,
                    fontWeight = FontWeight.Medium,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = OnSurfaceVariantColor
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = "${telemetry.temperatureCelsius.toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = (78 * fontScale).sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 6f),
                        blurRadius = 12f
                    )
                ),
                color = PrimaryColor
            )
            Text(
                text = "°C",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = (23 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                        blurRadius = 8f
                    )
                ),
                color = PrimaryColor,
                modifier = Modifier.padding(top = 10.dp, start = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = Color(0xCC25293A),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = homeConditionLabel(telemetry.condition),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Color(0xFFB3EDFF)
                )
            }
            
            val currentTemp = telemetry.temperatureCelsius.toInt()
            val highTemp = currentTemp + 2
            val lowTemp = currentTemp - 3
            Text(
                text = "H: ${highTemp}° L: ${lowTemp}°",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (14 * fontScale).sp,
                    fontWeight = FontWeight.Medium,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                        blurRadius = 4f
                    )
                ),
                color = OnSurfaceVariantColor
            )
        }
    }
}

@Composable
private fun SearchBarOverlay(
    searchQuery: String,
    searchResults: Result<List<LocationSearchResult>>,
    onSearchQueryChange: (String) -> Unit,
    onCitySelected: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(4.dp)),
            placeholder = {
                Text(
                    "Search location...",
                    color = OutlineVariantColor,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = OutlineVariantColor,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor.copy(alpha = 0.6f),
                unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.4f),
                focusedTextColor = OnSurfaceColor,
                unfocusedTextColor = OnSurfaceColor,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 2.dp)
                .background(PrimaryColor.copy(alpha = 0.85f))
        )

        AnimatedVisibility(
            visible = searchQuery.isNotEmpty() && searchResults is Result.Success
        ) {
            val results = (searchResults as? Result.Success)?.data ?: emptyList()
            if (results.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .background(SurfaceColor, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .border(1.dp, OutlineVariantColor, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    items(results) { city ->
                        val cityDisplayName = listOfNotNull(city.name, city.admin1, city.country).joinToString(", ")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city.latitude, city.longitude, cityDisplayName) }
                                .padding(16.dp)
                        ) {
                            Text(cityDisplayName, color = OnSurfaceColor, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsGrid(telemetry: WeatherTelemetry?, responsive: ResponsiveValues) {
    if (telemetry == null) return

    val visibilityVal = when (telemetry.condition) {
        com.weather.core.model.WeatherCondition.RAIN,
        com.weather.core.model.WeatherCondition.SNOW,
        com.weather.core.model.WeatherCondition.THUNDERSTORM -> "2.4"
        com.weather.core.model.WeatherCondition.CLOUDY -> "6.0"
        else -> "10.0"
    }
    
    Column(modifier = Modifier.padding(top = responsive.itemSpacing, bottom = responsive.itemSpacing / 2)) {
        Text(
            text = "CURRENT OBSERVATION METRICS",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariantColor,
            modifier = Modifier.padding(bottom = responsive.itemSpacing)
        )

        if (responsive.isCompact) {
            Column(verticalArrangement = Arrangement.spacedBy(responsive.gridGap)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = { DropletIcon(tint = OnSurfaceVariantColor) },
                        title = "HUMIDITY",
                        value = "${telemetry.humidityPercentage ?: 94}%",
                        responsive = responsive
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = { WindIcon(tint = OnSurfaceVariantColor) },
                        title = "WIND",
                        value = "${telemetry.windSpeedKph.toInt()}",
                        unit = "km/h NW",
                        responsive = responsive
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = { PressureIcon(tint = OnSurfaceVariantColor) },
                        title = "PRESSURE",
                        value = "${telemetry.pressure?.toInt() ?: 1008}",
                        unit = "hPa",
                        responsive = responsive
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = { EyeIcon(tint = OnSurfaceVariantColor) },
                        title = "VISIBILITY",
                        value = visibilityVal,
                        unit = "km",
                        responsive = responsive
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = { DropletIcon(tint = OnSurfaceVariantColor) },
                    title = "HUMIDITY",
                    value = "${telemetry.humidityPercentage ?: 94}%",
                    responsive = responsive
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = { WindIcon(tint = OnSurfaceVariantColor) },
                    title = "WIND",
                    value = "${telemetry.windSpeedKph.toInt()}",
                    unit = "km/h NW",
                    responsive = responsive
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = { PressureIcon(tint = OnSurfaceVariantColor) },
                    title = "PRESSURE",
                    value = "${telemetry.pressure?.toInt() ?: 1008}",
                    unit = "hPa",
                    responsive = responsive
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    icon = { EyeIcon(tint = OnSurfaceVariantColor) },
                    title = "VISIBILITY",
                    value = visibilityVal,
                    unit = "km",
                    responsive = responsive
                )
            }
        }
    }
}

@Composable
fun DropletIcon(tint: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(18.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2, size.height * 0.15f)
            cubicTo(
                size.width * 0.85f, size.height * 0.55f,
                size.width * 0.85f, size.height * 0.85f,
                size.width / 2, size.height * 0.9f
            )
            cubicTo(
                size.width * 0.15f, size.height * 0.85f,
                size.width * 0.15f, size.height * 0.55f,
                size.width / 2, size.height * 0.15f
            )
            close()
        }
        drawPath(path = path, color = tint)
    }
}

@Composable
fun WindIcon(tint: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(18.dp)) {
        val y1 = size.height * 0.3f
        val y2 = size.height * 0.5f
        val y3 = size.height * 0.7f
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(size.width * 0.1f, y1), end = androidx.compose.ui.geometry.Offset(size.width * 0.9f, y1), strokeWidth = 2.dp.toPx())
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(size.width * 0.2f, y2), end = androidx.compose.ui.geometry.Offset(size.width * 0.8f, y2), strokeWidth = 2.dp.toPx())
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, y3), end = androidx.compose.ui.geometry.Offset(size.width * 0.75f, y3), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun PressureIcon(tint: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(18.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(color = tint, radius = size.width * 0.4f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.2f), end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.8f), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.4f), end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.2f), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.4f), end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.2f), strokeWidth = strokeWidth)
    }
}

@Composable
fun EyeIcon(tint: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(18.dp)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.1f, size.height / 2)
            quadraticBezierTo(size.width / 2, size.height * 0.1f, size.width * 0.9f, size.height / 2)
            quadraticBezierTo(size.width / 2, size.height * 0.9f, size.width * 0.1f, size.height / 2)
            close()
        }
        drawPath(path = path, color = tint, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
        drawCircle(color = tint, radius = size.width * 0.18f, center = center)
    }
}

@Composable
fun CameraIcon(tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(24.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, tint, RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .border(2.dp, tint, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(3.dp)
                .background(tint, CircleShape)
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    unit: String = "",
    responsive: ResponsiveValues
) {
    Column(
        modifier = modifier
            .background(SurfaceColor, RoundedCornerShape(responsive.cardCornerRadius))
            .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius))
            .padding(responsive.cardPadding),
        verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariantColor)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = OnSurfaceColor)
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
                Text(text = unit, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariantColor)
            }
        }
    }
}

@Composable
private fun CreateReportButton(onClick: () -> Unit, responsive: ResponsiveValues) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .height(responsive.buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A90E2),
                contentColor = Color(0xFFF4F7FB)
            ),
            shape = RoundedCornerShape(responsive.buttonHeight / 2)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(responsive.iconSize))
            Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))
            Text(
                "CREATE REPORT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun homeConditionLabel(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.RAIN -> "HEAVY RAIN"
    WeatherCondition.THUNDERSTORM -> "SEVERE STORM"
    WeatherCondition.CLOUDY -> "CLOUDY"
    WeatherCondition.SNOW -> "SNOW"
    WeatherCondition.FOG -> "FOG"
    WeatherCondition.WINDY -> "WINDY"
    WeatherCondition.CLEAR -> "CLEAR"
    WeatherCondition.UNKNOWN -> "OBSERVED"
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Composable
fun WeatherAnimationBackground(condition: WeatherCondition, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "weatherAnimation")
    
    when (condition) {
        WeatherCondition.RAIN, WeatherCondition.THUNDERSTORM -> {
            val yOffsetState = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rainFall"
            )
            
            androidx.compose.foundation.Canvas(modifier = modifier) {
                val random = java.util.Random(42)
                val rainCount = 45
                val progress = yOffsetState.value
                
                for (i in 0 until rainCount) {
                    val initialX = random.nextFloat() * size.width
                    val initialY = random.nextFloat() * size.height
                    val length = 25f + random.nextFloat() * 20f
                    val speedMultiplier = 1.0f + random.nextFloat() * 0.8f
                    
                    val x = (initialX - (progress * 80f * speedMultiplier)) % size.width
                    val y = (initialY + (progress * size.height * speedMultiplier)) % size.height
                    
                    val drawX = if (x < 0) x + size.width else x
                    
                    drawLine(
                        color = Color(0xFFB3EDFF).copy(alpha = 0.20f + random.nextFloat() * 0.15f),
                        start = androidx.compose.ui.geometry.Offset(drawX, y),
                        end = androidx.compose.ui.geometry.Offset(drawX - length * 0.15f, y + length),
                        strokeWidth = 2f
                    )
                }
            }
        }
        WeatherCondition.SNOW -> {
            val floatState = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "snowFall"
            )
            
            androidx.compose.foundation.Canvas(modifier = modifier) {
                val random = java.util.Random(99)
                val snowCount = 35
                val progress = floatState.value
                
                for (i in 0 until snowCount) {
                    val initialX = random.nextFloat() * size.width
                    val initialY = random.nextFloat() * size.height
                    val radius = 3f + random.nextFloat() * 4f
                    val speedMultiplier = 0.5f + random.nextFloat() * 0.5f
                    
                    val sway = kotlin.math.sin((progress * 2 * kotlin.math.PI.toFloat()) + i) * 30f
                    
                    val x = (initialX + sway) % size.width
                    val y = (initialY + (progress * size.height * speedMultiplier)) % size.height
                    
                    val drawX = if (x < 0) x + size.width else x
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f + random.nextFloat() * 0.35f),
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(drawX, y)
                    )
                }
            }
        }
        WeatherCondition.CLEAR -> {
            val pulseState = infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sunPulse"
            )
            
            val rotationState = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "sunRotate"
            )
            
            androidx.compose.foundation.Canvas(modifier = modifier) {
                val centerX = size.width * 0.8f
                val centerY = size.height * 0.25f
                val baseRadius = size.width * 0.18f * pulseState.value
                
                drawScopeRotate(rotationState.value, pivot = androidx.compose.ui.geometry.Offset(centerX, centerY)) {
                    val rayCount = 8
                    for (i in 0 until rayCount) {
                        val angle = (i * 360f / rayCount) * (kotlin.math.PI.toFloat() / 180f)
                        val startRadius = baseRadius + 10f
                        val endRadius = baseRadius + 35f
                        
                        val startX = centerX + startRadius * kotlin.math.cos(angle)
                        val startY = centerY + startRadius * kotlin.math.sin(angle)
                        val endX = centerX + endRadius * kotlin.math.cos(angle)
                        val endY = centerY + endRadius * kotlin.math.sin(angle)
                        
                        drawLine(
                            color = Color(0xFFFFF9E6).copy(alpha = 0.22f),
                            start = androidx.compose.ui.geometry.Offset(startX, startY),
                            end = androidx.compose.ui.geometry.Offset(endX, endY),
                            strokeWidth = 4f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFEA9F).copy(alpha = 0.45f),
                            Color(0xFFFFEA9F).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                        radius = baseRadius * 1.6f
                    ),
                    radius = baseRadius * 1.6f,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
                
                drawCircle(
                    color = Color(0xFFFFF1C1).copy(alpha = 0.6f),
                    radius = baseRadius * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
            }
        }
        WeatherCondition.CLOUDY, WeatherCondition.FOG, WeatherCondition.WINDY, WeatherCondition.UNKNOWN -> {
            val driftState = infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 18000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "cloudDrift"
            )
            
            androidx.compose.foundation.Canvas(modifier = modifier) {
                val random = java.util.Random(77)
                val cloudCount = 4
                val progress = driftState.value
                
                for (i in 0 until cloudCount) {
                    val initialX = random.nextFloat() * size.width
                    val initialY = size.height * 0.15f + random.nextFloat() * size.height * 0.35f
                    val radius = 50f + random.nextFloat() * 60f
                    val speed = 0.5f + random.nextFloat() * 0.5f
                    
                    val x = (initialX + (progress * size.width * speed)) % size.width
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE2EBF5).copy(alpha = 0.12f),
                                Color(0xFFE2EBF5).copy(alpha = 0.03f),
                                Color.Transparent
                             ),
                            center = androidx.compose.ui.geometry.Offset(x, initialY),
                            radius = radius * 1.8f
                        ),
                        radius = radius * 1.8f,
                        center = androidx.compose.ui.geometry.Offset(x, initialY)
                    )
                }
                
                if (condition == WeatherCondition.WINDY) {
                    val windProgress = driftState.value
                    for (j in 0 until 4) {
                        val y = size.height * 0.2f + (j * size.height * 0.18f)
                        val startX = (windProgress * size.width + (j * 150f)) % size.width
                        val lineLength = 120f
                        
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = androidx.compose.ui.geometry.Offset(startX, y),
                            end = androidx.compose.ui.geometry.Offset(startX + lineLength, y),
                            strokeWidth = 2f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

private fun getBackgroundImageUrlForCondition(condition: com.weather.core.model.WeatherCondition): String {
    return when (condition) {
        com.weather.core.model.WeatherCondition.CLEAR -> "https://images.unsplash.com/photo-1601297183305-6df142704ea2?auto=format&fit=crop&w=1200&q=80"
        com.weather.core.model.WeatherCondition.CLOUDY -> "https://images.unsplash.com/photo-1445220499081-01f11a48c48a?auto=format&fit=crop&w=1200&q=80"
        com.weather.core.model.WeatherCondition.RAIN -> "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=1200&q=80"
        com.weather.core.model.WeatherCondition.SNOW -> "https://images.unsplash.com/photo-1491002052546-bf38f186af56?auto=format&fit=crop&w=1200&q=80"
        com.weather.core.model.WeatherCondition.THUNDERSTORM -> "https://images.unsplash.com/photo-1605727216801-e27ce1d0ce3c?auto=format&fit=crop&w=1200&q=80"
        else -> "https://images.unsplash.com/photo-1601297183305-6df142704ea2?auto=format&fit=crop&w=1200&q=80"
    }
}
