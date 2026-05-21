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
    val windowSizeClass = calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
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
                    if (responsive.isCompact) {
                        Column(verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)) {
                            WindCard(telemetry = telemetry, responsive = responsive, fontScale = fontScale)
                            PressureCard(telemetry = telemetry, responsive = responsive, fontScale = fontScale)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
                        ) {
                            WindCard(telemetry = telemetry, modifier = Modifier.weight(1f), responsive = responsive, fontScale = fontScale)
                            PressureCard(telemetry = telemetry, modifier = Modifier.weight(1f), responsive = responsive, fontScale = fontScale)
                        }
                    }
                }

                // Field Observer Notes
                if (snap.notes.isNotEmpty()) {
                    FieldNotesCard(snap = snap, responsive = responsive, fontScale = fontScale)
                }

                // Metadata grid
                snap.telemetry?.let { telemetry ->
                    MetadataCard(telemetry = telemetry, responsive = responsive, fontScale = fontScale)
                }
            }
        }

        // ── Floating top bar: Back | Title | More ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = responsive.itemSpacing / 2, vertical = responsive.itemSpacing / 2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(responsive.touchTargetMin)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "Observation Details",
                color = PrimaryColor,
                fontSize = (18 * fontScale).sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { /* options */ },
                modifier = Modifier
                    .size(responsive.touchTargetMin)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }

        // ── Sticky bottom action bar ────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, WeatherSnapColors.Background.copy(alpha = 0.96f), WeatherSnapColors.Background)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = responsive.screenPadding, vertical = responsive.itemSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
            ) {
                Button(
                    onClick = { /* Broadcast alert TODO */ },
                    modifier = Modifier.weight(1f).height(responsive.buttonHeight)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(responsive.cardCornerRadius / 1.5f),
                            ambientColor = Color(0xFF4A90E2),
                            spotColor = Color(0xFF4A90E2)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(responsive.cardCornerRadius / 1.5f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(responsive.iconSize))
                    Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
                    Text(
                        "BROADCAST ALERT",
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (0.5 * fontScale).sp
                    )
                }
                IconButton(
                    onClick = { /* Export TODO */ },
                    modifier = Modifier
                        .size(responsive.buttonHeight)
                        .clip(RoundedCornerShape(responsive.cardCornerRadius / 1.5f))
                        .background(WeatherSnapColors.SurfaceContainerHigh)
                        .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius / 1.5f))
                ) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = OnSurfaceColor)
                }
            }
        }
    }
}

// ── Hero section ─────────────────────────────────────────────────────────────
@Composable
private fun DetailHeroSection(snap: WeatherSnap, responsive: ResponsiveValues, fontScale: Float) {
    val severity = snap.severity()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(responsive.detailHeroHeight)
    ) {
        // Photo or condition gradient
        val photoPath = snap.photo?.filePath
        val bitmap = remember(photoPath) {
            photoPath?.let {
                try { BitmapFactory.decodeFile(it)?.asImageBitmap() } catch (_: Exception) { null }
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Observation photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Condition-based atmospheric gradient
            val gradientColors = snap.heroGradient()
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors)))
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
                .padding(responsive.screenPadding)
        ) {
            // Severity + Verified badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(responsive.gridGap / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (severity != Severity.ROUTINE) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (severity == Severity.CRITICAL)
                            WeatherSnapColors.Tertiary
                        else WeatherSnapColors.PrimaryContainer.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = responsive.cardPadding / 2, vertical = responsive.itemSpacing / 4),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(responsive.gridGap / 2)
                        ) {
                            // Icon based on severity
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(responsive.iconSize * 0.5f)) {
                                if (severity == Severity.CRITICAL) {
                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(size.width / 2, 0f)
                                        lineTo(size.width, size.height * 0.5f)
                                        lineTo(size.width / 2, size.height)
                                        lineTo(0f, size.height * 0.5f)
                                        close()
                                    }
                                    drawPath(path = path, color = Color.Black)
                                } else {
                                    drawCircle(color = Color.White, radius = size.width * 0.4f)
                                    drawCircle(color = WeatherSnapColors.PrimaryContainer.copy(alpha = 0.9f), radius = size.width * 0.2f)
                                }
                            }
                            Text(
                                text = if (severity == Severity.CRITICAL) "SEVERE SQUALL" else "SEVERE",
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (severity == Severity.CRITICAL) Color.Black else Color.White
                            )
                        }
                    }
                }
                if (snap.status == SyncStatus.COMPLETED) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1E1E1E),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "VERIFIED",
                            fontSize = (11 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (0.5 * fontScale).sp,
                            modifier = Modifier.padding(horizontal = responsive.cardPadding / 2, vertical = responsive.itemSpacing / 4)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(responsive.itemSpacing / 2))

            // Snap title (first line of notes or condition)
            val title = snap.heroTitle()
            Text(
                text = title,
                fontSize = (24 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(responsive.itemSpacing / 4))

            // Timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(responsive.iconSize * 0.7f))
                Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
                Text(
                    text = formatDetailTimestamp(snap.capturedAt),
                    fontSize = (13 * fontScale).sp,
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
    val high = temp + 2
    val low = temp - 3

    val cardBg = Color(0xFF1A243A)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Box(modifier = Modifier.padding(responsive.cardPadding)) {
            // Thermometer icon faded at top-right
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = OnSurfaceVariantColor.copy(alpha = 0.25f),
                modifier = Modifier.size(responsive.avatarSize * 1.25f).align(Alignment.TopEnd)
            )
            Column {
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
        Column(modifier = Modifier.padding(responsive.cardPadding), verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)) {
            Text("Wind (Gusts)", fontSize = (12 * fontScale).sp, color = OnSurfaceVariantColor)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${telemetry.windSpeedKph.toInt()}",
                    fontSize = (28 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor
                )
                Text(" km/h", fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor, modifier = Modifier.padding(bottom = responsive.itemSpacing / 4))
            }
            // Wind direction (compass placeholder — NW based on telemetry)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 4)) {
                Surface(shape = CircleShape, color = PrimaryColor.copy(alpha = 0.15f)) {
                    Box(modifier = Modifier.size(responsive.iconSize), contentAlignment = Alignment.Center) {
                        Text("⊙", fontSize = (11 * fontScale).sp, color = PrimaryColor)
                    }
                }
                Text("NW", fontSize = (13 * fontScale).sp, color = PrimaryColor, fontWeight = FontWeight.SemiBold)
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
        Column(modifier = Modifier.padding(responsive.cardPadding), verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)) {
            Text("Pressure", fontSize = (12 * fontScale).sp, color = OnSurfaceVariantColor)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$pressure",
                    fontSize = (28 * fontScale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurfaceColor
                )
                Text(" hPa", fontSize = (13 * fontScale).sp, color = OnSurfaceVariantColor, modifier = Modifier.padding(bottom = responsive.itemSpacing / 4))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 4)) {
                Icon(
                    if (isFalling) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
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
                text = snap.notes,
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
                        Text("Lead Meteorologist", fontSize = (11 * fontScale).sp, color = OnSurfaceVariantColor)
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
private fun MetadataCard(telemetry: WeatherTelemetry, responsive: ResponsiveValues, fontScale: Float) {
    val cardBg = Color(0xFF1A243A)
    val cardBorder = Color(0xFF2A3652)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Column(modifier = Modifier.padding(responsive.cardPadding), verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)) {
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = responsive.itemSpacing)
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
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm 'Local'", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private enum class Severity { ROUTINE, SEVERE, CRITICAL }
