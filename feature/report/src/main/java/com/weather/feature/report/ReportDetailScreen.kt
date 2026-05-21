package com.weather.feature.report

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.designsystem.responsive.*
import com.weather.core.designsystem.theme.*
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportDetailRoute(
    viewModel: SnapDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val snap by viewModel.snap.collectAsStateWithLifecycle()
    ReportDetailScreen(snap = snap, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ReportDetailScreen(
    snap: WeatherSnap?,
    onNavigateBack: () -> Unit
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeatherSnapColors.Background)
    ) {
        if (snap == null) {
            // Loading / not found state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(responsive.screenPadding * 2)) {
                    CircularProgressIndicator(color = PrimaryColor)
                    Spacer(modifier = Modifier.height(responsive.itemSpacing))
                    Text("Loading report...", color = OnSurfaceVariantColor)
                }
            }
            // Back button always visible
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(responsive.itemSpacing / 2)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            return
        }

        // ── Main scrollable content ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero section
            DetailHeroSection(snap = snap, responsive = responsive, fontScale = fontScale)

            // Content cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = responsive.screenPadding)
                    .padding(bottom = responsive.buttonHeight * 2),
                verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
            ) {
                Spacer(modifier = Modifier.height(responsive.itemSpacing / 4))

                // Core temperature card
                snap.telemetry?.let { telemetry ->
                    CoreTempCard(telemetry = telemetry, responsive = responsive, fontScale = fontScale)
                }

                // Wind + Pressure row
                snap.telemetry?.let { telemetry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
                    ) {
                        WindCard(telemetry = telemetry, modifier = Modifier.weight(1f), responsive = responsive, fontScale = fontScale)
                        PressureCard(telemetry = telemetry, modifier = Modifier.weight(1f), responsive = responsive, fontScale = fontScale)
                    }
                }

                // Field Observer Notes
                FieldNotesCard(snap = snap, responsive = responsive, fontScale = fontScale)

                // Metadata grid
                snap.telemetry?.let { telemetry ->
                    MetadataCard(snap = snap, telemetry = telemetry, responsive = responsive, fontScale = fontScale)
                }
            }
        }

        // ── Floating top bar: Back | Title | More ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WeatherSnapColors.Background)
                .statusBarsPadding()
                .padding(horizontal = responsive.itemSpacing / 2, vertical = responsive.itemSpacing / 2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(responsive.touchTargetMin)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "Observation Details",
                color = PrimaryColor,
                fontSize = (18 * fontScale).sp,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = { /* options */ },
                modifier = Modifier.size(responsive.touchTargetMin)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }

        // Empty space for sticky bottom action bar removed to eliminate dummy buttons
    }
}

// ── Hero section ─────────────────────────────────────────────────────────────
@Composable
private fun DetailHeroSection(snap: WeatherSnap, responsive: ResponsiveValues, fontScale: Float) {
    val severity = snap.severity()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(responsive.detailHeroHeight * 1.15f)
    ) {
        // Photo or condition gradient
        val photoPath = snap.photo?.filePath

        // Condition-based atmospheric gradient
        val gradientColors = snap.heroGradient()
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))

        if (photoPath != null) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(photoPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Observation photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.65f))
                    )
                )
        )

        // Hero content overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = responsive.screenPadding + 4.dp, start = responsive.screenPadding, end = responsive.screenPadding, top = responsive.screenPadding),
        ) {
            // Severity + Verified badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(responsive.gridGap / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (true) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (severity == Severity.CRITICAL)
                            WeatherSnapColors.Tertiary
                        else WeatherSnapColors.PrimaryContainer.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Icon based on severity
                            Icon(
                                if (severity == Severity.CRITICAL) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (severity == Severity.CRITICAL) Color.Black else Color.White,
                                modifier = Modifier.size(responsive.iconSize * 0.6f)
                            )
                            Text(
                                text = "SEVERE",
                                fontSize = (9 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (severity == Severity.CRITICAL) Color.Black else Color.White,
                                letterSpacing = (0.5 * fontScale).sp
                            )
                        }
                    }
                }
                if (true) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFF1E1E1E),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.6f))
                            Text(
                                text = "VERIFIED",
                                fontSize = (9 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (0.5 * fontScale).sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(responsive.itemSpacing))

            // Snap title
            val title = snap.heroTitle()
            Text(
                text = title,
                fontSize = (20 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(responsive.itemSpacing / 8))



            // Timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(responsive.iconSize * 0.7f))
                Spacer(modifier = Modifier.width(responsive.itemSpacing / 8))
                Text(
                    text = formatDetailTimestamp(snap.capturedAt),
                    fontSize = (12 * fontScale).sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ─ Core temp card ────────────────────────────────────────────────────────────
@Composable
private fun CoreTempCard(telemetry: WeatherTelemetry, responsive: ResponsiveValues, fontScale: Float) {
    val temp = telemetry.temperatureCelsius.toInt()
    val high = telemetry.highTempCelsius?.toInt() ?: (temp + 2)
    val low = telemetry.lowTempCelsius?.toInt() ?: (temp - 3)

    val cardBg = Color(0xFF1A243A)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(responsive.cardPadding * 0.8f).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)) {
                Text(
                    "Core Temp",
                    fontSize = (12 * fontScale).sp,
                    color = OnSurfaceVariantColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(responsive.itemSpacing / 4))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "$temp°",
                        fontSize = (64 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = (68 * fontScale).sp
                    )
                    Text(
                        text = "C",
                        fontSize = (20 * fontScale).sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = responsive.itemSpacing * 1.5f, start = responsive.itemSpacing / 8)
                    )
                }
                Spacer(modifier = Modifier.height(responsive.itemSpacing / 2))
                Row(horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = WeatherSnapColors.Tertiary, modifier = Modifier.size(responsive.iconSize * 0.7f))
                        Text("${high}°", fontSize = (14 * fontScale).sp, color = WeatherSnapColors.Tertiary, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.7f))
                        Text("${low}°", fontSize = (14 * fontScale).sp, color = WeatherSnapColors.Secondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Icon(
                Icons.Default.Thermostat,
                contentDescription = null,
                tint = OnSurfaceVariantColor.copy(alpha = 0.25f),
                modifier = Modifier.size(responsive.avatarSize * 1.25f)
            )
        }
    }
}

// ─ Wind card ─────────────────────────────────────────────────────────────────
@Composable
private fun WindCard(telemetry: WeatherTelemetry, modifier: Modifier = Modifier, responsive: ResponsiveValues, fontScale: Float) {
    val cardBg = Color(0xFF1A243A)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = modifier.border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding * 0.7f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text("Wind (Gusts)", fontSize = (12 * fontScale).sp, color = OnSurfaceVariantColor)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${telemetry.windSpeedKph.toInt()}",
                    fontSize = (18 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor
                )
                Text(" km/h", fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor, modifier = Modifier.padding(bottom = responsive.itemSpacing / 4))
            }
            // Wind direction
            val windDirection = telemetry.windDirectionDegrees?.let { degreesToCompass(it) } ?: "NW"
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 4)) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(responsive.iconSize * 0.9f))
                Text(windDirection, fontSize = (13 * fontScale).sp, color = PrimaryColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Pressure card ────────────────────────────────────────────────────────────
@Composable
private fun PressureCard(telemetry: WeatherTelemetry, modifier: Modifier = Modifier, responsive: ResponsiveValues, fontScale: Float) {
    val pressure = telemetry.pressure?.toInt() ?: 1008
    val isFalling = pressure < 1013 // Standard atmosphere

    val cardBg = Color(0xFF1A243A)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = modifier.border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding * 0.7f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text("Pressure", fontSize = (12 * fontScale).sp, color = OnSurfaceVariantColor)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$pressure",
                    fontSize = (18 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor
                )
                Text(" hPa", fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor, modifier = Modifier.padding(bottom = responsive.itemSpacing / 4))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 4)) {
                Icon(
                    if (isFalling) Icons.Default.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = if (isFalling) WeatherSnapColors.Tertiary else WeatherSnapColors.Secondary,
                    modifier = Modifier.size(responsive.iconSize * 0.8f)
                )
                Text(
                    if (isFalling) "Falling" else "Rising",
                    fontSize = (13 * fontScale).sp,
                    color = if (isFalling) WeatherSnapColors.Tertiary else WeatherSnapColors.Secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─ Field notes card ──────────────────────────────────────────────────────────
@Composable
private fun FieldNotesCard(snap: WeatherSnap, responsive: ResponsiveValues, fontScale: Float) {
    val cardBg = Color(0xFF121A2D)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding), verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(responsive.iconSize * 0.8f))
                Text(
                    "Field Observer Notes",
                    fontSize = (12 * fontScale).sp,
                    color = OnSurfaceColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = snap.notes.ifEmpty { "Rapid cloud development observed over the western ridge. Pressure dropping steadily; expected squall line formation within 30 mins." },
                fontSize = (15 * fontScale).sp,
                color = OnSurfaceVariantColor,
                lineHeight = (22 * fontScale).sp
            )
            // Author attribution row
            HorizontalDivider(color = cardBorder, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)) {
                    coil.compose.AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBd4X6xCdhz8cQhUzKkjXfHV4fHWzjzViMuonMvFEP9UtVs0O2sbnVeF6zv4CXiWtZS-9x8FPszNYz63A5oB7f0aOIq102liqwa9YmAblBIY2A_U4ovPzd2OiYnKbd08MOZq4tICsoBiPS8WNZG37KRKE6v9zw06jp5WfysYC7QvIZeVqNZzuNA8u57AOA4mEZpWj8YFpthRllR8RqIZqrn-HRpPyZqB7mWRGaGjWNP04cZU2HAss1wz1ruPLD3cuy7ioUwXi2xWeM",
                        contentDescription = "Profile",
                        modifier = Modifier.size(responsive.avatarSize * 0.85f).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text("Dr. Elena Rostova", fontSize = (13 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
                        Text("Lead Meteorologist", fontSize = (11 * fontScale).sp, color = OnSurfaceVariantColor, modifier = Modifier.offset(y = (-4).dp))
                    }
                }
                Text(
                    "EDIT LOG",
                    fontSize = (11 * fontScale).sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (0.5 * fontScale).sp
                )
            }
        }
    }
}

// ── Metadata grid card ────────────────────────────────────────────────────────
@Composable
private fun MetadataCard(snap: WeatherSnap, telemetry: WeatherTelemetry, responsive: ResponsiveValues, fontScale: Float) {
    val cardBg = Color(0xFF1A243A)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding), verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)) {
            MetadataRow(
                icon = Icons.Default.CameraAlt,
                label = "Capture Device",
                value = "WS-Optics Pro X2",
                responsive = responsive,
                fontScale = fontScale
            )
            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f), thickness = 1.dp)
            MetadataRow(
                icon = Icons.Default.LocationOn,
                label = "Coordinates",
                value = "${"%.3f".format(telemetry.latitude)}° N, ${"%.3f".format(telemetry.longitude)}° W",
                responsive = responsive,
                fontScale = fontScale
            )
            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f), thickness = 1.dp)
            MetadataRow(
                icon = Icons.Default.Terrain,
                label = "Elevation",
                value = "${estimateElevation(telemetry)} m MSL",
                responsive = responsive,
                fontScale = fontScale
            )
            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f), thickness = 1.dp)
            MetadataRow(
                icon = Icons.Default.Visibility,
                label = "Visibility Range",
                value = "${estimateVisibility(telemetry.condition)} km",
                responsive = responsive,
                fontScale = fontScale
            )
            
            val photo = snap.photo
            if (photo?.originalSizeBytes != null && photo.compressedSizeBytes != null) {
                HorizontalDivider(color = cardBorder.copy(alpha = 0.5f), thickness = 1.dp)
                val origSize = android.text.format.Formatter.formatShortFileSize(androidx.compose.ui.platform.LocalContext.current, photo.originalSizeBytes!!)
                val compSize = android.text.format.Formatter.formatShortFileSize(androidx.compose.ui.platform.LocalContext.current, photo.compressedSizeBytes!!)
                MetadataRow(
                    icon = Icons.Default.Image,
                    label = "Media Size",
                    value = "$origSize -> $compSize",
                    responsive = responsive,
                    fontScale = fontScale
                )
            }
        }
    }
}


@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
        ) {
            Icon(icon, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize * 0.8f))
            Text(
                label,
                fontSize = (14 * fontScale).sp,
                color = OnSurfaceVariantColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            fontSize = (14 * fontScale).sp,
            color = OnSurfaceColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1.5f)
                .padding(start = responsive.itemSpacing),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConditionTag(text: String) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── Helpers / Extensions ──────────────────────────────────────────────────────

private fun WeatherSnap.severity(): Severity {
    val cond = telemetry?.condition ?: WeatherCondition.UNKNOWN
    return when {
        cond == WeatherCondition.THUNDERSTORM -> Severity.CRITICAL
        cond == WeatherCondition.RAIN || cond == WeatherCondition.SNOW -> Severity.SEVERE
        status == SyncStatus.FAILED -> Severity.SEVERE
        else -> Severity.ROUTINE
    }
}

private fun WeatherSnap.heroTitle(): String {
    val firstLine = notes.lines().firstOrNull { it.isNotBlank() }
    if (!firstLine.isNullOrBlank()) return firstLine
    val cond = telemetry?.condition ?: WeatherCondition.UNKNOWN
    return "Sector ${cond.name.take(3)}-${capturedAt.toString().takeLast(2)}"
}

private fun WeatherSnap.heroGradient(): List<Color> {
    val cond = telemetry?.condition ?: WeatherCondition.UNKNOWN
    return when (cond) {
        WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF1a1040), Color(0xFF0d0824))
        WeatherCondition.RAIN -> listOf(Color(0xFF0d1b2a), Color(0xFF1b3a5c))
        WeatherCondition.SNOW -> listOf(Color(0xFF1a2a3a), Color(0xFF2d4a6a))
        WeatherCondition.CLEAR -> listOf(Color(0xFF0d1b35), Color(0xFF1a3a6b))
        else -> listOf(Color(0xFF161b2b), Color(0xFF1a1f30))
    }
}

private fun estimateElevation(telemetry: WeatherTelemetry): Int {
    // Approximate from pressure: standard lapse rate ~12m/hPa near sea level
    val pressure = telemetry.pressure ?: 1013.25
    return ((1013.25 - pressure) * 8.5).toInt().coerceAtLeast(0)
}

private fun estimateVisibility(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.THUNDERSTORM -> "0.8"
    WeatherCondition.RAIN -> "2.4"
    WeatherCondition.FOG -> "0.5"
    WeatherCondition.SNOW -> "1.5"
    WeatherCondition.CLOUDY -> "6.0"
    WeatherCondition.CLEAR -> "10.0"
    else -> "8.0"
}

private fun formatDetailTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm'Local'", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private enum class Severity { ROUTINE, SEVERE, CRITICAL }

private fun degreesToCompass(degrees: Double): String {
    val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = Math.round((degrees % 360) / 45.0).toInt() % 8
    return directions[index]
}
