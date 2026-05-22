package com.weather.feature.history

import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.platform.LocalContext
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
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherCondition
import com.weather.core.model.WeatherSnap
import java.text.SimpleDateFormat
import java.util.*

// Severity level derived from snap data
private enum class Severity { ROUTINE, SEVERE, CRITICAL }

private fun WeatherSnap.severity(): Severity {
    val cond = telemetry?.condition ?: WeatherCondition.UNKNOWN
    return when {
        cond == WeatherCondition.THUNDERSTORM -> Severity.CRITICAL
        cond == WeatherCondition.RAIN || cond == WeatherCondition.SNOW -> Severity.SEVERE
        status == SyncStatus.FAILED -> Severity.SEVERE
        else -> Severity.ROUTINE
    }
}

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onReportClick: (String) -> Unit,
    onCreateReportClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreen(
        uiState = uiState,
        onReportClick = onReportClick,
        onRefreshClick = viewModel::forceSync,
        onCreateReportClick = onCreateReportClick,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCamera = onNavigateToCamera
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onReportClick: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onRefreshClick: () -> Unit,
    onCreateReportClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {}
) {
    val context = LocalContext.current
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
            WeatherSnapTopBar(
                title = "WeatherSnap",
                showSearch = false,
                responsive = responsive
            )
        },
        bottomBar = {
            WeatherSnapBottomNav(
                selectedTab = WeatherSnapTab.Reports,
                onNavigateToHome = onNavigateToHome,
                onNavigateToCamera = onNavigateToCamera,
                onNavigateToReports = {},
                responsive = responsive
            )
        },
        floatingActionButton = {
            // FAB moved into EmptyHistoryState and removed from list end per requirements
        },
        containerColor = WeatherSnapColors.Background
    ) { paddingValues ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryColor) }
            }
            is HistoryUiState.Empty -> {
                EmptyHistoryState(
                    modifier = Modifier.padding(paddingValues),
                    responsive = responsive,
                    fontScale = fontScale,
                    onCreateReportClick = onCreateReportClick
                )
            }
            is HistoryUiState.Success -> {
                HistoryTimelineList(
                    snaps = uiState.snaps,
                    onReportClick = onReportClick,
                    modifier = Modifier.padding(paddingValues),
                    responsive = responsive,
                    fontScale = fontScale
                )
            }
            is HistoryUiState.Error -> {
                ErrorHistoryState(message = uiState.message, modifier = Modifier.padding(paddingValues), responsive = responsive)
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    modifier: Modifier = Modifier,
    responsive: ResponsiveValues,
    fontScale: Float,
    onCreateReportClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(responsive.screenPadding * 2)) {
            Icon(
                Icons.Default.Info, contentDescription = null,
                tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.avatarSize * 2)
            )
            Spacer(modifier = Modifier.height(responsive.itemSpacing))
            Text("No Reports Yet", fontSize = (20 * fontScale).sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor)
            Spacer(modifier = Modifier.height(responsive.itemSpacing / 2))
            Text(
                "Tap the + button to create your first weather observation report",
                fontSize = (14 * fontScale).sp, color = OnSurfaceVariantColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(responsive.sectionSpacing))
            Button(
                onClick = onCreateReportClick,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .height(responsive.buttonHeight),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2),
                    contentColor = Color(0xFFF4F7FB)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(responsive.buttonHeight / 2)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(responsive.iconSize))
                Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))
                Text(
                    "CREATE REPORT",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorHistoryState(message: String, modifier: Modifier = Modifier, responsive: ResponsiveValues) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(responsive.screenPadding * 2)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = WeatherSnapColors.Error, modifier = Modifier.size(responsive.avatarSize * 1.5f))
            Spacer(modifier = Modifier.height(responsive.itemSpacing))
            Text(message, color = WeatherSnapColors.Error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun HistoryTimelineList(
    snaps: List<WeatherSnap>,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = responsive.screenPadding, end = responsive.screenPadding, top = responsive.itemSpacing / 2, bottom = responsive.buttonHeight * 2),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = responsive.itemSpacing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Saved Reports",
                        fontSize = (18 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Your saved weather observations.",
                        fontSize = (14 * fontScale).sp,
                        color = OnSurfaceVariantColor
                    )
                }
            }
        }

        itemsIndexed(snaps, key = { _, snap -> snap.id }) { index, snap ->
            TimelineSnapCard(
                snap = snap,
                isFirst = index == 0,
                isLast = index == snaps.lastIndex,
                onClick = { onReportClick(snap.id) },
                responsive = responsive,
                fontScale = fontScale
            )
        }

        // Load older reports (FAB removed as requested)
        if (snaps.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = responsive.itemSpacing, bottom = responsive.itemSpacing),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadOlderReportsButton(
                        responsive = responsive, 
                        fontScale = fontScale,
                        onClick = {
                            android.widget.Toast.makeText(context, "All available reports are already loaded.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadOlderReportsButton(modifier: Modifier = Modifier, responsive: ResponsiveValues, fontScale: Float, onClick: () -> Unit) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor),
        shape = RoundedCornerShape(responsive.cardCornerRadius / 1.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing / 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Load Older Reports",
                fontSize = (14 * fontScale).sp,
                color = OnSurfaceVariantColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize))
        }
    }
}

@Composable
private fun CreateReportFab(onClick: () -> Unit, modifier: Modifier = Modifier, responsive: ResponsiveValues) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = PrimaryColor,
        contentColor = WeatherSnapColors.OnPrimary,
        shape = CircleShape,
        modifier = modifier.size(responsive.touchTargetMin * 1.08f)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Create Report", modifier = Modifier.size(responsive.iconSize * 1.2f))
    }
}

@Composable
private fun TimelineSnapCard(
    snap: WeatherSnap,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    val severity = snap.severity()
    val nodeColor = when (severity) {
        Severity.CRITICAL -> WeatherSnapColors.Tertiary
        Severity.SEVERE   -> PrimaryColor
        Severity.ROUTINE  -> OutlineVariantColor.copy(alpha = 0.7f)
    }
    val nodeSize = 20.dp
    val lineColor = OutlineVariantColor.copy(alpha = 0.35f)

    // IntrinsicSize.Min allows the timeline column to fill the card's height
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // ── Timeline connector column ─────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(responsive.avatarSize)
                .fillMaxHeight()
        ) {
            if (!isFirst) {
                Box(modifier = Modifier.width(2.dp).height(responsive.photoHeroHeight * 0.3f).background(lineColor))
            } else {
                Spacer(modifier = Modifier.height(responsive.photoHeroHeight * 0.3f))
            }
            Box(
                modifier = Modifier
                    .size(nodeSize)
                    .clip(CircleShape)
                    .background(WeatherSnapColors.Background)
                    .border(
                        width = if (severity == Severity.CRITICAL) 2.dp else 1.dp,
                        color = nodeColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (severity == Severity.CRITICAL) nodeSize / 2.2f else nodeSize / 3.5f)
                        .clip(CircleShape)
                        .background(nodeColor)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(lineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))

        // ── Card ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f).padding(bottom = responsive.itemSpacing)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(responsive.cardCornerRadius))
                    .clickable(onClick = onClick)
                    .then(
                        if (severity == Severity.CRITICAL)
                            Modifier.shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(responsive.cardCornerRadius),
                                ambientColor = WeatherSnapColors.Tertiary.copy(alpha = 0.15f),
                                spotColor = WeatherSnapColors.Tertiary.copy(alpha = 0.15f)
                            )
                        else Modifier
                    ),
                color = WeatherSnapColors.SurfaceContainer,
                shape = RoundedCornerShape(responsive.cardCornerRadius),
                border = androidx.compose.foundation.BorderStroke(
                    if (severity == Severity.CRITICAL) 1.5.dp else 1.dp,
                    if (severity == Severity.CRITICAL) WeatherSnapColors.Tertiary.copy(alpha = 0.6f)
                    else OutlineVariantColor.copy(alpha = 0.4f)
                )
            ) {
                Column {
                    SnapPhotoHero(snap = snap, severity = severity, responsive = responsive, fontScale = fontScale)
                    Column(modifier = Modifier.padding(responsive.cardPadding)) {
                        val titleColor = if (severity == Severity.CRITICAL) WeatherSnapColors.Tertiary else OnSurfaceColor
                        Text(
                            text = buildCardTitle(snap),
                            fontSize = (15 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val descText = if (snap.notes.isNotEmpty()) snap.notes
                        else snap.telemetry?.condition?.name
                            ?.replace("_", " ")
                            ?.lowercase()
                            ?.replaceFirstChar { it.uppercase() }
                            ?.let { "$it observation recorded at this location." }
                            ?: "Observation recorded."
                        Text(
                            text = descText,
                            fontSize = (12 * fontScale).sp,
                            color = OnSurfaceVariantColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SnapPhotoHero(snap: WeatherSnap, severity: Severity, responsive: ResponsiveValues, fontScale: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(responsive.photoHeroHeight)
            .clip(RoundedCornerShape(topStart = responsive.cardCornerRadius, topEnd = responsive.cardCornerRadius))
    ) {
        val photoPath = snap.photo?.thumbnailFilePath ?: snap.photo?.filePath

        // Atmospheric gradient based on condition
        val gradientColors = conditionGradient(snap.telemetry?.condition ?: WeatherCondition.UNKNOWN)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        )

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

        // Dark scrim at bottom so chips/text are legible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                    )
                )
        )

        // Timestamp + severity chip overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(responsive.cardPadding * 0.6f),
            horizontalArrangement = Arrangement.spacedBy(responsive.gridGap / 2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date/time
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = formatTimestamp(snap.capturedAt),
                    fontSize = (11 * fontScale).sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = responsive.cardPadding / 2, vertical = responsive.itemSpacing / 4)
                )
            }
            // Severity chip
            SeverityChip(severity = severity, responsive = responsive, fontScale = fontScale)
        }
    }
}

@Composable
private fun SeverityChip(severity: Severity, responsive: ResponsiveValues, fontScale: Float) {
    val chipData = when (severity) {
        Severity.CRITICAL -> ChipData(
            bg = WeatherSnapColors.Tertiary.copy(alpha = 0.95f),
            fg = WeatherSnapColors.OnTertiaryContainer,
            label = "Critical",
            icon = {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(responsive.iconSize * 0.5f)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width / 2, 0f)
                        lineTo(size.width, size.height * 0.8f)
                        lineTo(0f, size.height * 0.8f)
                        close()
                    }
                    drawPath(path = path, color = WeatherSnapColors.OnTertiaryContainer)
                    // draw exclamation point
                    drawLine(color = WeatherSnapColors.Tertiary.copy(alpha = 0.95f), start = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.35f), end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.55f), strokeWidth = 2f)
                    drawCircle(color = WeatherSnapColors.Tertiary.copy(alpha = 0.95f), radius = 1.5f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.65f))
                }
            }
        )
        Severity.SEVERE -> ChipData(
            bg = Color(0xFF2D5673).copy(alpha = 0.95f),
            fg = Color.White,
            label = "Severe",
            icon = {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(responsive.iconSize * 0.6f)) {
                    drawArc(color = Color.White, startAngle = 180f, sweepAngle = 180f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    drawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(0f, size.height / 2), end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2), strokeWidth = 2f)
                    // rain drops
                    drawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.6f), end = androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.8f), strokeWidth = 1.5f)
                    drawLine(color = Color.White, start = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.6f), end = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.8f), strokeWidth = 1.5f)
                }
            }
        )
        Severity.ROUTINE -> ChipData(
            bg = Color(0xFF2A3044).copy(alpha = 0.95f),
            fg = OnSurfaceColor,
            label = "Routine",
            icon = {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(responsive.iconSize * 0.6f)) {
                    drawArc(color = OnSurfaceColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    drawLine(color = OnSurfaceColor, start = androidx.compose.ui.geometry.Offset(0f, size.height / 2), end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2), strokeWidth = 2f)
                }
            }
        )
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = chipData.bg,
        border = if (severity == Severity.ROUTINE) androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            chipData.icon()
            Text(
                text = chipData.label,
                fontSize = (11 * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                color = chipData.fg
            )
        }
    }
}

private data class ChipData(
    val bg: Color,
    val fg: Color,
    val label: String,
    val icon: @Composable () -> Unit
)

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun buildCardTitle(snap: WeatherSnap): String {
    // Use first line of notes as title if available
    val firstLine = snap.notes.lines().firstOrNull { it.isNotBlank() }
    if (!firstLine.isNullOrBlank()) return firstLine
    val cond = snap.telemetry?.condition ?: WeatherCondition.UNKNOWN
    return "${cond.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} Observation"
}

private fun conditionGradient(condition: WeatherCondition): List<Color> = when (condition) {
    WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF1a1040), Color(0xFF2d1b69))
    WeatherCondition.RAIN -> listOf(Color(0xFF0d1b2a), Color(0xFF1b3a5c))
    WeatherCondition.SNOW -> listOf(Color(0xFF1a2a3a), Color(0xFF2d4a6a))
    WeatherCondition.CLOUDY -> listOf(Color(0xFF1e2433), Color(0xFF2d3548))
    WeatherCondition.CLEAR -> listOf(Color(0xFF0d1b35), Color(0xFF1a3a6b))
    WeatherCondition.FOG -> listOf(Color(0xFF1e2533), Color(0xFF2a3040))
    WeatherCondition.WINDY -> listOf(Color(0xFF0f1a2a), Color(0xFF1a2d45))
    else -> listOf(Color(0xFF161b2b), Color(0xFF1a1f30))
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
