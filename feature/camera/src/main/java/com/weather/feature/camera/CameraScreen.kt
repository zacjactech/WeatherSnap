package com.weather.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.designsystem.theme.*
import com.weather.core.designsystem.responsive.*
import com.weather.core.model.WeatherTelemetry

@Composable
fun CameraRoute(
    viewModel: CameraViewModel = hiltViewModel(),
    onPhotoTaken: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val lastPhotoPath by viewModel.lastPhotoPath.collectAsStateWithLifecycle()

    CameraScreen(
        uiState = uiState,
        telemetry = telemetry,
        lastPhotoPath = lastPhotoPath,
        onCaptureClick = { bytes -> viewModel.processCapturedPhoto(bytes) },
        onRetakeClick = viewModel::resetCamera,
        onConfirmClick = { filePath -> onPhotoTaken(filePath) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun CameraScreen(
    uiState: CameraUiState,
    telemetry: WeatherTelemetry?,
    lastPhotoPath: String?,
    onCaptureClick: (ByteArray) -> Unit,
    onRetakeClick: () -> Unit,
    onConfirmClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activity = LocalContext.current as? androidx.activity.ComponentActivity
        ?: return
    val windowSizeClass = calculateWindowSizeClass(activity)
    val responsive = calculateResponsiveValues(windowSizeClass)
    val fontScale = when {
        responsive.isExpanded -> 1.1f
        responsive.isMedium -> 1.05f
        else -> 1f
    }

    // Edge-to-edge fullscreen — no Scaffold, no TopAppBar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        when (uiState) {
            is CameraUiState.Ready -> {
                FullscreenCameraPreview(
                    telemetry = telemetry,
                    lastPhotoPath = lastPhotoPath,
                    onCaptureClick = onCaptureClick,
                    onNavigateBack = onNavigateBack,
                    responsive = responsive,
                    fontScale = fontScale
                )
            }
            is CameraUiState.Capturing -> {
                // Keep viewfinder visible during capture — just block input
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = PrimaryColor,
                        modifier = Modifier.size(responsive.avatarSize)
                    )
                }
            }
            is CameraUiState.Success -> {
                PhotoConfirmOverlay(
                    filePath = uiState.filePath,
                    onRetakeClick = onRetakeClick,
                    onConfirmClick = { onConfirmClick(uiState.filePath) },
                    responsive = responsive
                )
            }
            is CameraUiState.Error -> {
                CameraErrorOverlay(
                    message = uiState.exception.message ?: "Camera error",
                    onRetryClick = onRetakeClick,
                    onNavigateBack = onNavigateBack,
                    responsive = responsive
                )
            }
        }
    }
}

@Composable
private fun FullscreenCameraPreview(
    telemetry: WeatherTelemetry?,
    lastPhotoPath: String?,
    onCaptureClick: (ByteArray) -> Unit,
    onNavigateBack: () -> Unit,
    responsive: ResponsiveValues,
    fontScale: Float
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        CameraPermissionDenied(onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }, responsive = responsive)
        return
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply { bindToLifecycle(lifecycleOwner) }
    }
    var isCapturing by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(0f) } // 0f=1x … 1f=3x

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Camera Preview (full bleed) ──────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Subtle dark scrim top + bottom ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
        )

        TechnicalGridOverlay(modifier = Modifier.matchParentSize())

        // ── Top Bar: Close | GPS chip | Flash ────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = responsive.screenPadding, vertical = responsive.itemSpacing)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(responsive.touchTargetMin)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, OutlineVariantColor, CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            // GPS coordinates chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = responsive.itemSpacing / 2),
                contentAlignment = Alignment.Center
            ) {
                telemetry?.let { t ->
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor)
                    ) {
                        Text(
                            text = "GPS: ${"%.2f".format(t.latitude)}°N, ${"%.2f".format(t.longitude)}°E",
                            fontSize = (14 * fontScale).sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = PrimaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 220.dp)
                                .padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing / 2)
                        )
                    }
                }
            }

            // Flash toggle
            IconButton(
                onClick = {
                    flashEnabled = !flashEnabled
                    cameraController.imageCaptureFlashMode =
                        if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                },
                modifier = Modifier
                    .size(responsive.touchTargetMin)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, OutlineVariantColor, CircleShape)
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = if (flashEnabled) WeatherSnapColors.Tertiary else Color.White
                )
            }
        }

        // ── Viewfinder brackets (4 corner L-shapes) ───────────────────────
        ViewfinderBrackets(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .fillMaxHeight(0.28f)
                .align(Alignment.Center),
            responsive = responsive
        )

        // ── Crosshair center ─────────────────────────────────────────────
        CrosshairCenter(modifier = Modifier.align(Alignment.Center), responsive = responsive)

        // ── Live Telemetry Chips row ─────────────────────────────────────
        telemetry?.let { t ->
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = responsive.buttonHeight * 4.2f)
                    .padding(horizontal = responsive.screenPadding)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)
            ) {
                TelemetryChip(
                    icon = { CameraThermometerIcon(tint = WeatherSnapColors.Secondary, iconSize = responsive.iconSize * 0.65f) },
                    value = "${t.temperatureCelsius.toInt()}°C",
                    responsive = responsive,
                    fontScale = fontScale * 0.82f
                )
                TelemetryChip(
                    icon = { CameraWindIcon(tint = WeatherSnapColors.Secondary, iconSize = responsive.iconSize * 0.65f) },
                    value = "${t.windSpeedKph.toInt()} KM/H",
                    responsive = responsive,
                    fontScale = fontScale * 0.82f
                )
                t.humidityPercentage?.let { hum ->
                    TelemetryChip(
                        icon = { CameraDropletIcon(tint = WeatherSnapColors.Secondary, iconSize = responsive.iconSize * 0.65f) },
                        value = "$hum% HUM",
                        responsive = responsive,
                        fontScale = fontScale * 0.82f
                    )
                }
                t.pressure?.let { pres ->
                    TelemetryChip(
                        icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                        value = "${pres.toInt()} hPa",
                        responsive = responsive,
                        fontScale = fontScale * 0.82f
                    )
                }
                t.visibilityKm?.let { vis ->
                    TelemetryChip(
                        icon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                        value = "${vis.toInt()} KM",
                        responsive = responsive,
                        fontScale = fontScale * 0.82f
                    )
                }
                t.uvIndex?.let { uv ->
                    TelemetryChip(
                        icon = { Icon(Icons.Default.WbSunny, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                        value = "UV $uv",
                        responsive = responsive,
                        fontScale = fontScale * 0.82f
                    )
                }
                t.cloudCoverPercent?.let { cloud ->
                    TelemetryChip(
                        icon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                        value = "$cloud% CLD",
                        responsive = responsive,
                        fontScale = fontScale * 0.82f
                    )
                }
                t.dewPointCelsius?.let { dew ->
                    TelemetryChip(
                        icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                        value = "DP ${dew.toInt()}°",
                        responsive = responsive,
                        fontScale = fontScale * 0.82f
                    )
                }
                // Altitude estimated from pressure
                TelemetryChip(
                    icon = { Icon(Icons.Default.Terrain, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                    value = "${((1013.25 - (t.pressure ?: 1013.25)) * 8.5).toInt().coerceAtLeast(0)}m",
                    responsive = responsive,
                    fontScale = fontScale * 0.82f
                )
                TelemetryChip(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WeatherSnapColors.Secondary, modifier = Modifier.size(responsive.iconSize * 0.65f)) },
                    value = "GPS",
                    responsive = responsive,
                    fontScale = fontScale * 0.82f
                )
            }
        }

        // ── Bottom controls: PANO | Shutter | Gallery ────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = responsive.sectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = responsive.sectionSpacing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PANO mode shortcut
                Column(
                    modifier = Modifier
                        .height(responsive.touchTargetMin * 1.3f)
                        .clickable { /* Panorama mode TODO */ }
                        .padding(top = responsive.itemSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Panorama",
                        tint = Color.White,
                        modifier = Modifier.size(responsive.iconSize * 1.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("PANO", fontSize = (10 * fontScale).sp, color = Color.White, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
                }

                // Shutter button
                ShutterButton(
                    isCapturing = isCapturing,
                    onClick = {
                        if (!isCapturing) {
                            isCapturing = true
                            val executor = ContextCompat.getMainExecutor(context)
                            cameraController.takePicture(
                                executor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        try {
                                            val buffer = image.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            onCaptureClick(bytes)
                                        } finally {
                                            image.close()
                                            isCapturing = false
                                        }
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                    }
                                }
                            )
                        }
                    },
                    responsive = responsive
                )

                // Last photo thumbnail
                Box(
                    modifier = Modifier
                        .size(responsive.touchTargetMin)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (lastPhotoPath != null) {
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(lastPhotoPath)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Last photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Photo, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(responsive.iconSize * 1.2f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(responsive.itemSpacing))

            // Zoom slider: 1x to 3x
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = responsive.sectionSpacing * 1.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
            ) {
                Text("1x", fontSize = (11 * fontScale).sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                Slider(
                    value = zoomLevel,
                    onValueChange = { level ->
                        zoomLevel = level
                        cameraController.setLinearZoom(level)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryColor,
                        activeTrackColor = PrimaryColor,
                        inactiveTrackColor = WeatherSnapColors.SurfaceContainerHighest
                    )
                )
                Text("3x", fontSize = (11 * fontScale).sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TechnicalGridOverlay(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val gridColor = OutlineVariantColor.copy(alpha = 0.2f)
        val stroke = 1.dp.toPx()
        val thirdWidth = size.width / 3f
        val thirdHeight = size.height / 3f

        for (index in 1..2) {
            val x = thirdWidth * index
            val y = thirdHeight * index
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, size.height),
                strokeWidth = stroke
            )
            drawLine(
                color = gridColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = stroke
            )
        }
    }
}

// ── Viewfinder corner bracket composable ────────────────────────────────────
@Composable
private fun ViewfinderBrackets(modifier: Modifier = Modifier, responsive: ResponsiveValues) {
    val bracketColor = PrimaryColor.copy(alpha = 0.8f)
    val strokeWidth = 2.dp
    val bracketLength = responsive.avatarSize * 1.5f

    Box(modifier = modifier) {
        // Top-left
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            Box(
                modifier = Modifier
                    .width(bracketLength)
                    .height(strokeWidth)
                    .background(bracketColor)
            )
            Box(
                modifier = Modifier
                    .width(strokeWidth)
                    .height(bracketLength)
                    .background(bracketColor)
            )
        }
        // Top-right
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Box(
                modifier = Modifier
                    .width(bracketLength)
                    .height(strokeWidth)
                    .background(bracketColor)
                    .align(Alignment.TopEnd)
            )
            Box(
                modifier = Modifier
                    .width(strokeWidth)
                    .height(bracketLength)
                    .background(bracketColor)
                    .align(Alignment.TopEnd)
            )
        }
        // Bottom-left
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(
                modifier = Modifier
                    .width(bracketLength)
                    .height(strokeWidth)
                    .background(bracketColor)
                    .align(Alignment.BottomStart)
            )
            Box(
                modifier = Modifier
                    .width(strokeWidth)
                    .height(bracketLength)
                    .background(bracketColor)
                    .align(Alignment.BottomStart)
            )
        }
        // Bottom-right
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            Box(
                modifier = Modifier
                    .width(bracketLength)
                    .height(strokeWidth)
                    .background(bracketColor)
                    .align(Alignment.BottomEnd)
            )
            Box(
                modifier = Modifier
                    .width(strokeWidth)
                    .height(bracketLength)
                    .background(bracketColor)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun CrosshairCenter(modifier: Modifier = Modifier, responsive: ResponsiveValues) {
    val lineColor = PrimaryColor.copy(alpha = 0.6f)
    val crosshairSize = responsive.avatarSize

    Box(modifier = modifier.size(crosshairSize)) {
        // Horizontal arm
        Box(
            modifier = Modifier
                .width(crosshairSize)
                .height(2.dp)
                .background(lineColor)
                .align(Alignment.Center)
        )
        // Vertical arm
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(crosshairSize)
                .background(lineColor)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun TelemetryChip(icon: @Composable () -> Unit, value: String, responsive: ResponsiveValues, fontScale: Float) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = responsive.cardPadding, vertical = responsive.itemSpacing / 3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(responsive.itemSpacing / 2)
        ) {
            icon()
            Text(
                text = value,
                fontSize = (12 * fontScale).sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                letterSpacing = (0.5 * fontScale).sp
            )
        }
    }
}

@Composable
private fun CameraThermometerIcon(tint: Color, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 16.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(iconSize)) {
        val strokeWidth = 1.5.dp.toPx()
        val bulbRadius = this.size.width * 0.3f
        val tubeRadius = this.size.width * 0.15f
        drawCircle(color = tint, radius = bulbRadius, center = androidx.compose.ui.geometry.Offset(this.size.width / 2, this.size.height - bulbRadius), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width / 2 - tubeRadius, this.size.height - bulbRadius * 1.5f), end = androidx.compose.ui.geometry.Offset(this.size.width / 2 - tubeRadius, bulbRadius), strokeWidth = strokeWidth)
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width / 2 + tubeRadius, this.size.height - bulbRadius * 1.5f), end = androidx.compose.ui.geometry.Offset(this.size.width / 2 + tubeRadius, bulbRadius), strokeWidth = strokeWidth)
        drawArc(color = tint, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = androidx.compose.ui.geometry.Offset(this.size.width / 2 - tubeRadius, bulbRadius - tubeRadius), size = androidx.compose.ui.geometry.Size(tubeRadius * 2, tubeRadius * 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
    }
}

@Composable
private fun CameraWindIcon(tint: Color, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 16.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(iconSize)) {
        val y1 = this.size.height * 0.3f
        val y2 = this.size.height * 0.5f
        val y3 = this.size.height * 0.7f
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width * 0.1f, y1), end = androidx.compose.ui.geometry.Offset(this.size.width * 0.9f, y1), strokeWidth = 1.5.dp.toPx())
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width * 0.2f, y2), end = androidx.compose.ui.geometry.Offset(this.size.width * 0.8f, y2), strokeWidth = 1.5.dp.toPx())
        drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(this.size.width * 0.15f, y3), end = androidx.compose.ui.geometry.Offset(this.size.width * 0.75f, y3), strokeWidth = 1.5.dp.toPx())
    }
}

@Composable
private fun CameraDropletIcon(tint: Color, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 16.dp) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(iconSize)) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(this@Canvas.size.width / 2, this@Canvas.size.height * 0.15f)
            cubicTo(this@Canvas.size.width * 0.85f, this@Canvas.size.height * 0.55f, this@Canvas.size.width * 0.85f, this@Canvas.size.height * 0.85f, this@Canvas.size.width / 2, this@Canvas.size.height * 0.9f)
            cubicTo(this@Canvas.size.width * 0.15f, this@Canvas.size.height * 0.85f, this@Canvas.size.width * 0.15f, this@Canvas.size.height * 0.55f, this@Canvas.size.width / 2, this@Canvas.size.height * 0.15f)
            close()
        }
        drawPath(path = path, color = tint, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
    }
}

@Composable
private fun ShutterButton(isCapturing: Boolean, onClick: () -> Unit, responsive: ResponsiveValues) {
    val scale by animateFloatAsState(
        targetValue = if (isCapturing) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "shutterScale"
    )
    val outerSize = responsive.touchTargetMin * 1.5f
    val innerSize = responsive.touchTargetMin * 1.2f

    Box(
        modifier = Modifier
            .size(outerSize * scale)
            .clip(CircleShape)
            .background(Color.Transparent)
            .border(4.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize * scale)
                .clip(CircleShape)
                .background(Color.White)
        ) {
        }
        // Use a button for the interaction
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            onClick = onClick
        ) {}
    }
}

// ── Permission denied state ──────────────────────────────────────────────────
@Composable
private fun CameraPermissionDenied(onRequestPermission: () -> Unit, responsive: ResponsiveValues) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(responsive.screenPadding * 2)
        ) {
            Icon(
                Icons.Default.Warning, contentDescription = null,
                tint = OnSurfaceVariantColor,
                modifier = Modifier.size(responsive.avatarSize)
            )
            Spacer(modifier = Modifier.height(responsive.itemSpacing))
            Text(
                "Camera permission required",
                fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = OnSurfaceColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(responsive.itemSpacing / 2))
            Text(
                "Grant camera access to capture weather conditions and embed telemetry metadata.",
                fontSize = 14.sp, color = OnSurfaceVariantColor, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(responsive.sectionSpacing))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) {
                Text("Grant Permission", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Photo confirm overlay ────────────────────────────────────────────────────
@Composable
private fun PhotoConfirmOverlay(
    filePath: String,
    onRetakeClick: () -> Unit,
    onConfirmClick: () -> Unit,
    responsive: ResponsiveValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(responsive.screenPadding)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(responsive.itemSpacing)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(responsive.photoHeroHeight)
                    .clip(RoundedCornerShape(responsive.cardCornerRadius))
                    .background(WeatherSnapColors.SurfaceContainerLow)
                    .border(1.dp, OutlineVariantColor, RoundedCornerShape(responsive.cardCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Photo captured", color = OnSurfaceColor, fontSize = 16.sp)
                }
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(filePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)
            ) {
                OutlinedButton(
                    onClick = onRetakeClick,
                    modifier = Modifier.weight(1f).height(responsive.buttonHeight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariantColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))
                    Text("Retake", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier.weight(2f).height(responsive.buttonHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(responsive.itemSpacing / 2))
                    Text("USE PHOTO", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

// ── Camera error overlay ─────────────────────────────────────────────────────
@Composable
private fun CameraErrorOverlay(
    message: String,
    onRetryClick: () -> Unit,
    onNavigateBack: () -> Unit,
    responsive: ResponsiveValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WeatherSnapColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(responsive.screenPadding * 2)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = WeatherSnapColors.Error, modifier = Modifier.size(responsive.avatarSize))
            Spacer(modifier = Modifier.height(responsive.itemSpacing))
            Text(message, color = WeatherSnapColors.Error, textAlign = TextAlign.Center, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(responsive.sectionSpacing))
            Row(horizontalArrangement = Arrangement.spacedBy(responsive.gridGap)) {
                OutlinedButton(onClick = onNavigateBack) { Text("Go Back", color = OnSurfaceColor) }
                Button(
                    onClick = onRetryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                ) { Text("Retry") }
            }
        }
    }
}
