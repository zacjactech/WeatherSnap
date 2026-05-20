package com.weather.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weather.core.designsystem.theme.*
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherSnap
import com.weather.core.model.WeatherTelemetry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportDetailRoute(
    snapId: String,
    onNavigateBack: () -> Unit
) {
    // In a real implementation, we would fetch by ID
    // For now, show placeholder
    ReportDetailScreen(
        snap = null,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    snap: WeatherSnap?,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Details", color = OnSurfaceColor) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        if (snap == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = OnSurfaceVariantColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Report not found",
                        color = OnSurfaceVariantColor
                    )
                }
            }
        } else {
            ReportDetailContent(
                snap = snap,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ReportDetailContent(
    snap: WeatherSnap,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Status",
                        fontSize = 12.sp,
                        color = OnSurfaceVariantColor
                    )
                    Text(
                        snap.status.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColor
                    )
                }
                Text(
                    formatTimestamp(snap.capturedAt),
                    fontSize = 14.sp,
                    color = OnSurfaceVariantColor
                )
            }
        }

        // Weather Data Card
        snap.telemetry?.let { telemetry ->
            WeatherDataCard(telemetry = telemetry)
        }

        // Photo Card
        snap.photo?.let { photo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "ATTACHMENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariantColor,
                        letterSpacing = 1.2.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceLowColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = OnSurfaceVariantColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Photo captured",
                                fontSize = 14.sp,
                                color = OnSurfaceVariantColor
                            )
                        }
                    }
                }
            }
        }

        // Notes Card
        if (snap.notes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "FIELD NOTES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariantColor,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        snap.notes,
                        fontSize = 16.sp,
                        color = OnSurfaceColor
                    )
                }
            }
        }

        // Location Card
        snap.telemetry?.let { telemetry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OnSurfaceVariantColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Location",
                            fontSize = 12.sp,
                            color = OnSurfaceVariantColor
                        )
                        Text(
                            "Lat: ${telemetry.latitude.format(4)}, Lon: ${telemetry.longitude.format(4)}",
                            fontSize = 14.sp,
                            color = OnSurfaceColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDataCard(telemetry: WeatherTelemetry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "WEATHER CONDITIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariantColor,
                letterSpacing = 1.2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherMetric(
                    icon = Icons.Default.Star,
                    label = "Temperature",
                    value = "${telemetry.temperatureCelsius.toInt()}°C"
                )
                WeatherMetric(
                    icon = Icons.Default.Search,
                    label = "Humidity",
                    value = "${telemetry.humidityPercentage ?: "--"}%"
                )
                WeatherMetric(
                    icon = Icons.Default.Star,
                    label = "Wind",
                    value = "${telemetry.windSpeedKph.toInt()} km/h"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = PrimaryColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        text = telemetry.condition.name.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PrimaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            color = OnSurfaceVariantColor
        )
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceColor
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)