package com.weather.feature.report

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.designsystem.theme.*
import com.weather.core.designsystem.responsive.*
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Self-contained high-fidelity custom ImageVectors for full compile-safety
val SaveIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Save",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(17f, 3f)
            lineTo(5f, 3f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineTo(19f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineTo(19f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(7f)
            lineTo(17f, 3f)
            close()
            moveTo(12f, 19f)
            curveTo(10.3f, 19f, 9f, 17.7f, 9f, 16f)
            curveTo(9f, 14.3f, 10.3f, 13f, 12f, 13f)
            curveTo(13.7f, 13f, 15f, 14.3f, 15f, 16f)
            curveTo(15f, 17.7f, 13.7f, 19f, 12f, 19f)
            close()
            moveTo(15f, 9f)
            horizontalLineTo(5f)
            verticalLineTo(5f)
            horizontalLineTo(15f)
            verticalLineTo(9f)
            close()
        }.build()
    }

val UploadIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Upload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(19.35f, 10.04f)
            curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
            curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
            curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
            curveTo(0f, 17.31f, 2.69f, 20f, 6f, 20f)
            horizontalLineTo(19f)
            curveTo(21.76f, 20f, 24f, 17.76f, 24f, 15f)
            curveTo(24f, 12.36f, 21.95f, 10.22f, 19.35f, 10.04f)
            close()
            moveTo(14f, 13f)
            verticalLineTo(17f)
            horizontalLineTo(10f)
            verticalLineTo(13f)
            horizontalLineTo(7f)
            lineTo(12f, 8f)
            lineTo(17f, 13f)
            horizontalLineTo(14f)
            close()
        }.build()
    }

val CameraIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(9f, 2f)
            lineTo(7.17f, 4f)
            horizontalLineTo(4f)
            curveTo(2.9f, 4f, 2f, 2.9f, 2f, 4f)
            verticalLineTo(18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            horizontalLineTo(20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            verticalLineTo(6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            horizontalLineTo(16.83f)
            lineTo(15f, 2f)
            horizontalLineTo(9f)
            close()
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
            close()
        }.build()
    }

val TagIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Tag",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(21.41f, 11.58f)
            lineTo(12.42f, 2.58f)
            curveTo(12.05f, 2.22f, 11.55f, 2f, 11f, 2f)
            horizontalLineTo(4f)
            curveTo(2.9f, 2f, 2f, 2.9f, 2f, 4f)
            verticalLineTo(11f)
            curveTo(2f, 11.55f, 2.22f, 12.05f, 2.59f, 12.42f)
            lineTo(11.59f, 21.42f)
            curveTo(12.37f, 22.2f, 13.63f, 22.2f, 14.41f, 21.41f)
            lineTo(21.41f, 14.41f)
            curveTo(22.2f, 13.63f, 22.2f, 12.37f, 21.41f, 11.58f)
            close()
            moveTo(6.5f, 8f)
            curveTo(5.67f, 8f, 5f, 7.33f, 5f, 6.5f)
            curveTo(5f, 5.67f, 5.67f, 5f, 6.5f, 5f)
            curveTo(7.33f, 5f, 8f, 5.67f, 8f, 6.5f)
            curveTo(8f, 7.33f, 7.33f, 8f, 6.5f, 8f)
            close()
        }.build()
    }

val MicIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Mic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 14f)
            curveTo(13.66f, 14f, 15f, 12.66f, 15f, 11f)
            verticalLineTo(5f)
            curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
            curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
            verticalLineTo(11f)
            curveTo(9f, 12.66f, 10.34f, 14f, 12f, 14f)
            close()
            moveTo(17f, 11f)
            curveTo(17f, 13.8f, 14.7f, 16f, 12f, 16f)
            curveTo(9.3f, 16f, 7f, 13.8f, 7f, 11f)
            horizontalLineTo(5f)
            curveTo(5f, 14.5f, 7.72f, 17.4f, 11f, 17.9f)
            verticalLineTo(21f)
            horizontalLineTo(13f)
            verticalLineTo(17.9f)
            curveTo(16.28f, 17.4f, 19f, 14.5f, 19f, 11f)
            horizontalLineTo(17f)
            close()
        }.build()
    }

val CodeIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Code",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(9.4f, 16.6f)
            lineTo(4.8f, 12f)
            lineTo(9.4f, 7.4f)
            lineTo(8f, 6f)
            lineTo(2f, 12f)
            lineTo(8f, 18f)
            lineTo(9.4f, 16.6f)
            close()
            moveTo(14.6f, 16.6f)
            lineTo(19.2f, 12f)
            lineTo(14.6f, 7.4f)
            lineTo(16f, 6f)
            lineTo(22f, 12f)
            lineTo(16f, 18f)
            lineTo(14.6f, 16.6f)
            close()
        }.build()
    }

val WifiIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Wifi",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 21f)
            lineTo(22.8f, 7.6f)
            curveTo(22.3f, 7.2f, 17.9f, 3f, 12f, 3f)
            curveTo(6.1f, 3f, 1.7f, 7.2f, 1.2f, 7.6f)
            lineTo(12f, 21f)
            close()
        }.build()
    }

val WindIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Wind",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(2f, 12f)
            horizontalLineTo(18f)
            curveTo(19.7f, 12f, 21f, 10.7f, 21f, 9f)
            curveTo(21f, 7.3f, 19.7f, 6f, 18f, 6f)
            curveTo(16.8f, 6f, 15.8f, 6.7f, 15.3f, 7.7f)
            lineTo(13.9f, 7f)
            curveTo(14.6f, 5.2f, 16.2f, 4f, 18f, 4f)
            curveTo(20.8f, 4f, 23f, 6.2f, 23f, 9f)
            curveTo(23f, 11.8f, 20.8f, 14f, 18f, 14f)
            horizontalLineTo(2f)
            verticalLineTo(12f)
            close()
            moveTo(20f, 16f)
            curveTo(18.3f, 16f, 17f, 17.3f, 17f, 19f)
            curveTo(17f, 20.7f, 18.3f, 22f, 20f, 22f)
            curveTo(21.2f, 22f, 22.2f, 21.3f, 22.7f, 20.3f)
            lineTo(21.3f, 19.6f)
            curveTo(21f, 20.2f, 20.5f, 20.6f, 20f, 20.6f)
            curveTo(19.1f, 20.6f, 18.4f, 19.9f, 18.4f, 19f)
            curveTo(18.4f, 18.1f, 19.1f, 17.4f, 20f, 17.4f)
            horizontalLineTo(22f)
            verticalLineTo(16f)
            horizontalLineTo(20f)
            close()
        }.build()
    }

val DropletIcon: ImageVector
    @Composable
    get() = remember {
        ImageVector.Builder(
            name = "Droplet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2.69f)
            lineTo(7.56f, 7.12f)
            curveTo(5.5f, 9.18f, 4.47f, 11.9f, 4.47f, 14.62f)
            curveTo(4.47f, 18.78f, 7.84f, 22.15f, 12f, 22.15f)
            curveTo(16.16f, 22.15f, 19.53f, 18.78f, 19.53f, 14.62f)
            curveTo(19.53f, 11.9f, 18.5f, 9.18f, 16.44f, 7.12f)
            lineTo(12f, 2.69f)
            close()
            moveTo(12f, 20.15f)
            curveTo(8.96f, 20.15f, 6.47f, 17.66f, 6.47f, 14.62f)
            curveTo(6.47f, 12.63f, 7.26f, 10.73f, 8.68f, 9.31f)
            lineTo(12f, 5.99f)
            lineTo(15.32f, 9.31f)
            curveTo(16.74f, 10.73f, 17.53f, 12.63f, 17.53f, 14.62f)
            curveTo(17.53f, 17.66f, 15.04f, 20.15f, 12f, 20.15f)
            close()
        }.build()
    }

@Composable
fun CreateReportRoute(
    viewModel: ReportViewModel = hiltViewModel(),
    onNavigateToCamera: () -> Unit,
    onNavigateBack: () -> Unit,
    onReportSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreateReportScreen(
        uiState = uiState,
        onNotesChange = viewModel::updateNotes,
        onAddPhotoClick = onNavigateToCamera,
        onDraftClick = {
            viewModel.saveDraft()
        },
        onTransmitClick = {
            viewModel.submitSnap()
        },
        onNavigateBack = onNavigateBack,
        onReportSaved = onReportSaved
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CreateReportScreen(
    uiState: ReportUiState,
    onNotesChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    onDraftClick: () -> Unit,
    onTransmitClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onReportSaved: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as androidx.activity.ComponentActivity)
    val responsive = calculateResponsiveValues(windowSizeClass)
    val fontScale = when {
        responsive.isExpanded -> 1.1f
        responsive.isMedium -> 1.05f
        else -> 1f
    }

    LaunchedEffect(uiState) {
        if (uiState is ReportUiState.Success) {
            onReportSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Report", color = PrimaryColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurfaceColor)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Sync status */ }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = PrimaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        when (uiState) {
            is ReportUiState.Drafting -> {
                DraftingContent(
                    draft = uiState.draft,
                    locationName = uiState.locationName,
                    onNotesChange = onNotesChange,
                    onAddPhotoClick = onAddPhotoClick,
                    onDraftClick = onDraftClick,
                    onTransmitClick = onTransmitClick,
                    modifier = Modifier.padding(paddingValues),
                    responsive = responsive,
                    fontScale = fontScale
                )
            }
            is ReportUiState.Submitting -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryColor)
                        Spacer(modifier = Modifier.height(responsive.itemSpacing))
                        Text("Persisting Report...", color = OnSurfaceVariantColor)
                    }
                }
            }
            is ReportUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WeatherSnapColors.Error, modifier = Modifier.size(responsive.avatarSize))
                        Spacer(modifier = Modifier.height(responsive.itemSpacing))
                        Text(uiState.message, color = WeatherSnapColors.Error)
                        Spacer(modifier = Modifier.height(responsive.itemSpacing))
                        Button(onClick = onTransmitClick) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }
        }
    }
}

@Composable
private fun DraftingContent(
    draft: WeatherSnap,
    locationName: String?,
    onNotesChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    onDraftClick: () -> Unit,
    onTransmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = responsive.screenPadding, end = responsive.screenPadding, top = responsive.screenPadding, bottom = responsive.buttonHeight * 2),
            verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
        ) {
            // Weather Snapshot Card
            draft.telemetry?.let { telemetry ->
                WeatherSnapshotCard(telemetry = telemetry, locationName = locationName, responsive = responsive, fontScale = fontScale)
            }

            // Photo Capture Area / Media Section
            Column(
                verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "OBSERVATION MEDIA",
                        fontSize = (12 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariantColor,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        if (draft.photo != null) "1/3 Attached" else "0/3 Attached",
                        fontSize = (11 * fontScale).sp,
                        color = PrimaryColor
                    )
                }

                val photo = draft.photo
                if (photo != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(responsive.photoHeroHeight)
                            .clip(RoundedCornerShape(responsive.cardCornerRadius))
                            .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius))
                            .background(SurfaceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(photo.filePath) {
                            try {
                                BitmapFactory.decodeFile(photo.filePath)?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Observation Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = OnSurfaceVariantColor)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(responsive.photoHeroHeight)
                            .clip(RoundedCornerShape(responsive.cardCornerRadius))
                            .background(Color(0xFF111827))
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                                drawRoundRect(
                                    color = OutlineVariantColor,
                                    style = stroke,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(responsive.cardCornerRadius.toPx())
                                )
                            }
                            .clickable(onClick = onAddPhotoClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Camera with + badge
                            Box {
                                Icon(CameraIcon, contentDescription = null, tint = OnSurfaceVariantColor, modifier = Modifier.size(responsive.iconSize * 2f))
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryColor,
                                    modifier = Modifier
                                        .size(responsive.iconSize * 0.7f)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = WeatherSnapColors.OnPrimary, modifier = Modifier.size(responsive.iconSize * 0.45f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(responsive.itemSpacing))
                            Text("Tap to Capture", fontSize = (14 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Text("or ", fontSize = (12 * fontScale).sp, color = OnSurfaceVariantColor)
                                Text("select from gallery", fontSize = (12 * fontScale).sp, color = OnSurfaceVariantColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Notes Input Section with auto-save bar
            Column(
                verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
            ) {
                Text(
                    "FIELD NOTES",
                    fontSize = (12 * fontScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariantColor,
                    letterSpacing = 1.2.sp
                )

                var isNotesFocused by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(responsive.cardCornerRadius))
                        .background(SurfaceColor)
                        .border(
                            width = 1.dp,
                            color = if (isNotesFocused) WeatherSnapColors.Secondary else OutlineVariantColor,
                            shape = RoundedCornerShape(responsive.cardCornerRadius)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(responsive.detailHeroHeight * 0.67f)
                            .padding(responsive.cardPadding)
                    ) {
                        BasicTextField(
                            value = draft.notes,
                            onValueChange = onNotesChange,
                            modifier = Modifier
                                .fillMaxSize()
                                .onFocusChanged { isNotesFocused = it.isFocused },
                            textStyle = TextStyle(
                                color = OnSurfaceColor,
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Normal
                            ),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (draft.notes.isEmpty()) {
                                        Text(
                                            text = "Describe specific atmospheric anomalies, ground conditions, or equipment status...",
                                            color = OnSurfaceVariantColor.copy(alpha = 0.5f),
                                            fontSize = (14 * fontScale).sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    // Auto-saving actions footer
                    HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.5f), thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceColor)
                            .padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing / 2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)) {
                            Icon(
                                TagIcon,
                                contentDescription = "Add tags",
                                tint = OnSurfaceVariantColor,
                                modifier = Modifier.size(responsive.iconSize).clickable { }
                            )
                            Icon(
                                MicIcon,
                                contentDescription = "Voice memo",
                                tint = OnSurfaceVariantColor,
                                modifier = Modifier.size(responsive.iconSize).clickable { }
                            )
                        }
                        Text(
                            "Auto-saving...",
                            color = OnSurfaceVariantColor,
                            fontSize = (11 * fontScale).sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Telemetry Data Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius)),
                colors = CardDefaults.cardColors(containerColor = SurfaceLowColor),
                shape = RoundedCornerShape(responsive.cardCornerRadius)
            ) {
                Column(
                    modifier = Modifier.padding(responsive.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
                    ) {
                        Icon(CodeIcon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(responsive.iconSize))
                        Text(
                            "TELEMETRY DATA",
                            fontSize = (12 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariantColor,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Elevation", fontSize = (11 * fontScale).sp, color = OnSurfaceVariantColor)
                            Text(
                                "${estimateDraftElevation(draft.telemetry)}m",
                                fontSize = (13 * fontScale).sp,
                                color = OnSurfaceColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Barometer", fontSize = (11 * fontScale).sp, color = OnSurfaceVariantColor)
                            Text(
                                "${draft.telemetry?.pressure?.toInt() ?: 1012} hPa",
                                fontSize = (13 * fontScale).sp,
                                color = OnSurfaceColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Coordinates", fontSize = (11 * fontScale).sp, color = OnSurfaceVariantColor)
                            // Lat and Lon on separate lines to match design
                            val coords = draft.telemetry?.let { t ->
                                val latH = if (t.latitude >= 0) "N" else "S"
                                val lonH = if (t.longitude >= 0) "E" else "W"
                                Pair(
                                    "%.4f° %s,".format(kotlin.math.abs(t.latitude), latH),
                                    "%.4f° %s".format(kotlin.math.abs(t.longitude), lonH)
                                )
                            } ?: Pair("46.8523° N,", "121.7603° W")
                            Text(coords.first, fontSize = (13 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
                            Text(coords.second, fontSize = (13 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Network", fontSize = (11 * fontScale).sp, color = OnSurfaceVariantColor)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 4)
                            ) {
                                Icon(WifiIcon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size((responsive.iconSize * 0.8f)))
                                Text("Satellite Link", fontSize = (13 * fontScale).sp, color = PrimaryColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Sticky Actions Bar — DRAFT and TRANSMIT always side by side
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BackgroundColor.copy(alpha = 0.9f),
                            BackgroundColor
                        )
                    )
                )
                .padding(
                    start = responsive.screenPadding,
                    end = responsive.screenPadding,
                    top = responsive.screenPadding,
                    bottom = responsive.screenPadding + 16.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)
            ) {
                DraftButton(onClick = onDraftClick, modifier = Modifier.weight(1f), responsive = responsive, fontScale = fontScale)
                TransmitButton(onClick = onTransmitClick, modifier = Modifier.weight(2f), responsive = responsive, fontScale = fontScale)
            }
        }
    }
}

@Composable
private fun DraftButton(onClick: () -> Unit, modifier: Modifier = Modifier, responsive: ResponsiveValues, fontScale: Float) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(responsive.buttonHeight)
            .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius / 1.5f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = WeatherSnapColors.SurfaceContainerHigh,
            contentColor = OnSurfaceColor
        ),
        shape = RoundedCornerShape(responsive.cardCornerRadius / 1.5f),
        contentPadding = PaddingValues(horizontal = responsive.itemSpacing / 2)
    ) {
        Icon(SaveIcon, contentDescription = null, modifier = Modifier.size(responsive.iconSize))
        Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
        Text("DRAFT", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold, letterSpacing = (1 * fontScale).sp)
    }
}

@Composable
private fun TransmitButton(onClick: () -> Unit, modifier: Modifier = Modifier, responsive: ResponsiveValues, fontScale: Float) {
    Button(
        onClick = onClick,
        modifier = modifier.height(responsive.buttonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryColor,
            contentColor = WeatherSnapColors.OnPrimary
        ),
        shape = RoundedCornerShape(responsive.cardCornerRadius / 1.5f),
        contentPadding = PaddingValues(horizontal = responsive.itemSpacing / 2)
    ) {
        Icon(UploadIcon, contentDescription = null, modifier = Modifier.size(responsive.iconSize))
        Spacer(modifier = Modifier.width(responsive.itemSpacing / 4))
        Text(
            "TRANSMIT REPORT",
            fontSize = (13 * fontScale).sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (1 * fontScale).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WeatherSnapshotCard(
    telemetry: WeatherTelemetry,
    locationName: String?,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    val timeString = remember {
        val sdf = SimpleDateFormat("HH:mm z", Locale.getDefault())
        sdf.format(Date())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius)),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(responsive.cardCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(responsive.cardPadding),
            verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 4)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(responsive.iconSize * 0.8f))
                    Text(
                        "CURRENT CONDITIONS",
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariantColor,
                        letterSpacing = (1 * fontScale).sp
                    )
                }
                Text(
                    timeString,
                    fontSize = (11 * fontScale).sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SnapshotTemperatureBlock(telemetry = telemetry, locationName = locationName, modifier = Modifier.weight(1f), responsive = responsive, fontScale = fontScale)
                SnapshotTelemetryChips(telemetry = telemetry, responsive = responsive, fontScale = fontScale)
            }
        }
    }
}

@Composable
private fun SnapshotTemperatureBlock(
    telemetry: WeatherTelemetry,
    locationName: String?,
    modifier: Modifier = Modifier,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "${telemetry.temperatureCelsius.toInt()}°",
                fontSize = (48 * fontScale).sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )
            Text(
                "C",
                fontSize = (20 * fontScale).sp,
                color = OnSurfaceVariantColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = responsive.itemSpacing / 2)
            )
        }
        Text(
            locationName ?: "Mt. Rainier Base Camp",
            fontSize = (14 * fontScale).sp,
            color = OnSurfaceColor,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SnapshotTelemetryChips(
    telemetry: WeatherTelemetry,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing),
        horizontalAlignment = Alignment.End
    ) {
        SnapshotTelemetryChip(icon = { Icon(WindIcon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size((14 * fontScale).dp)) }) {
            Text("${telemetry.windSpeedKph.toInt()} km/h NW", fontSize = (11 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
        }

        telemetry.humidityPercentage?.let { humidity ->
            SnapshotTelemetryChip(icon = { Icon(DropletIcon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size((14 * fontScale).dp)) }) {
                Text("$humidity% RH", fontSize = (11 * fontScale).sp, color = OnSurfaceColor, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SnapshotTelemetryChip(
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.5.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
            .padding(horizontal = 24.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        icon()
        content()
    }
}

private fun estimateDraftElevation(telemetry: WeatherTelemetry?): String {
    val pressure = telemetry?.pressure ?: return "1,440"
    val meters = ((1013.25 - pressure) * 8.5).toInt().coerceAtLeast(0)
    return "%,d".format(meters)
}

private fun formatDraftCoordinates(telemetry: WeatherTelemetry): String {
    val latHemisphere = if (telemetry.latitude >= 0) "N" else "S"
    val lonHemisphere = if (telemetry.longitude >= 0) "E" else "W"
    return "%.4f° %s, %.4f° %s".format(
        kotlin.math.abs(telemetry.latitude),
        latHemisphere,
        kotlin.math.abs(telemetry.longitude),
        lonHemisphere
    )
}
