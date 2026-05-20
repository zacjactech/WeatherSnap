package com.weather.feature.weather

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate as modifierRotate
import androidx.compose.ui.graphics.drawscope.rotate as drawScopeRotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.common.Result
import com.weather.core.designsystem.theme.*
import com.weather.core.model.LocationSearchResult
import com.weather.core.model.WeatherTelemetry
import com.weather.core.model.WeatherCondition

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

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WeatherSnap",
                        color = PrimaryColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryColor)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBd4X6xCdhz8cQhUzKkjXfHV4fHWzjzViMuonMvFEP9UtVs0O2sbnVeF6zv4CXiWtZS-9x8FPszNYz63A5oB7f0aOIq102liqwa9YmAblBIY2A_U4ovPzd2OiYnKbd08MOZq4tICsoBiPS8WNZG37KRKE6v9zw06jp5WfysYC7QvIZeVqNZzuNA8u57AOA4mEZpWj8YFpthRllR8RqIZqrn-HRpPyZqB7mWRGaGjWNP04cZU2HAss1wz1ruPLD3cuy7ioUwXi2xWeM",
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, OutlineVariantColor.copy(alpha = 0.4f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            WeatherSnapBottomNav(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                onNavigateToCamera = onNavigateToCamera,
                onNavigateToReports = onNavigateToReports,
                onNavigateToSettings = onNavigateToSettings
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
                onCitySelected = onCitySelected
            )

            AnimatedVisibility(
                visible = uiState is WeatherUiState.Success,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MetricsGrid(telemetry = (uiState as? WeatherUiState.Success)?.telemetry)
            }

            Spacer(modifier = Modifier.weight(1f))

            CreateReportButton(onClick = onCreateReportClicked)
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
    onCitySelected: (Double, Double, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(345.dp),
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric forest backdrop
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDPHTAtVFXu7NJf1Y2CwY62nnk3vQAkIOVUuFkcHFVVX6zEjiO9gTyafsBRwFrNsJXVfY5N9YIG8spvFhHuWHUuJkIwHDIhDhOqhLnsflN-sLlWsuCr8vp3HIJiCjlTGUYQoMOVr3dTAjlArLFLriJqpGWFdZ9uTLL8CZZjyUxmftwVI7sbAXSyJWhEcMVS9JLXR9zu5CNkYWiK76cx4Gc4te3ItYPvpvZWt-_KtATvIIc3A5EtFJiSyo2DUH2N48T8l2VlBfIUnLY",
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        // Dark vignette gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BackgroundColor.copy(alpha = 0.4f),
                            BackgroundColor.copy(alpha = 0.1f),
                            BackgroundColor.copy(alpha = 0.7f),
                            BackgroundColor
                        )
                    )
                )
        )

        // Dynamic Weather Animation Background based on actual state condition
        val animCondition = (uiState as? WeatherUiState.Success)?.telemetry?.condition ?: WeatherCondition.CLEAR
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
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        color = WeatherSnapColors.Error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            is WeatherUiState.Success -> {
                WeatherDisplay(telemetry = uiState.telemetry, locationName = locationName)
            }
        }

        SearchBarOverlay(
            searchQuery = searchQuery,
            searchResults = searchResults,
            onSearchQueryChange = onSearchQueryChange,
            onCitySelected = onCitySelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun WeatherDisplay(telemetry: WeatherTelemetry, locationName: String) {
    val displayName = locationName.ifEmpty { "Seattle, WA" }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Location text with a subtle, premium crisp text shadow matching Stitch
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                shadow = androidx.compose.ui.graphics.Shadow(
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    shadow = androidx.compose.ui.graphics.Shadow(
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
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "${telemetry.temperatureCelsius.toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                        blurRadius = 8f
                    )
                ),
                color = PrimaryColor,
                modifier = Modifier.padding(top = 8.dp, start = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = Color(0xCC25293A),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFF80D2E9).copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = telemetry.condition.name.replace("_", " ").uppercase(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.08.sp
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    shadow = androidx.compose.ui.graphics.Shadow(
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
                .background(SurfaceColor, RoundedCornerShape(8.dp)),
            placeholder = { Text("Search observation point...", color = OutlineVariantColor) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OutlineVariantColor) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryColor,
                unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                focusedTextColor = OnSurfaceColor,
                unfocusedTextColor = OnSurfaceColor
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
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
private fun MetricsGrid(telemetry: WeatherTelemetry?) {
    if (telemetry == null) return

    val visibilityVal = when (telemetry.condition) {
        com.weather.core.model.WeatherCondition.RAIN,
        com.weather.core.model.WeatherCondition.SNOW,
        com.weather.core.model.WeatherCondition.THUNDERSTORM -> "2.4"
        com.weather.core.model.WeatherCondition.CLOUDY -> "6.0"
        else -> "10.0"
    }
    
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(
            text = "CURRENT OBSERVATION METRICS",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariantColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = { DropletIcon(tint = OnSurfaceVariantColor) },
                title = "HUMIDITY",
                value = "${telemetry.humidityPercentage ?: 94}%"
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = { WindIcon(tint = OnSurfaceVariantColor) },
                title = "WIND",
                value = "${telemetry.windSpeedKph.toInt()}",
                unit = "km/h"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = { PressureIcon(tint = OnSurfaceVariantColor) },
                title = "PRESSURE",
                value = "${telemetry.pressure?.toInt() ?: 1008}",
                unit = "hPa"
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = { EyeIcon(tint = OnSurfaceVariantColor) },
                title = "VISIBILITY",
                value = visibilityVal,
                unit = "km"
            )
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
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: String,
    value: String,
    unit: String = ""
) {
    Column(
        modifier = modifier
            .background(SurfaceColor, RoundedCornerShape(12.dp))
            .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariantColor)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = OnSurfaceColor)
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariantColor)
            }
        }
    }
}

@Composable
private fun CreateReportButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4A90E2),
            contentColor = Color(0xFFF4F7FB)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Icon(Icons.Default.Edit, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "CREATE WEATHER REPORT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WeatherSnapBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = it) },
                label = "Home",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            BottomNavItem(
                icon = { CameraIcon(tint = it) },
                label = "Camera",
                isSelected = selectedTab == 1,
                onClick = { 
                    onTabSelected(1)
                    onNavigateToCamera()
                }
            )
            BottomNavItem(
                icon = { Icon(Icons.Default.List, contentDescription = "Reports", tint = it) },
                label = "Reports",
                isSelected = selectedTab == 2,
                onClick = { 
                    onTabSelected(2)
                    onNavigateToReports()
                }
            )
            BottomNavItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = it) },
                label = "Settings",
                isSelected = selectedTab == 3,
                onClick = { 
                    onTabSelected(3)
                    onNavigateToSettings()
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: @Composable (Color) -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) PrimaryColor else OnSurfaceVariantColor

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(contentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
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