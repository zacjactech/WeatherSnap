package com.weather.feature.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weather.core.designsystem.theme.*
import com.weather.core.model.SyncStatus
import com.weather.core.model.WeatherSnap
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel(),
    onReportClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreen(
        uiState = uiState,
        onReportClick = onReportClick,
        onRefreshClick = viewModel::forceSync
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onReportClick: (String) -> Unit,
    onRefreshClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Observation History", color = OnSurfaceColor) },
                actions = {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = OnSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        when (uiState) {
            is HistoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                }
            }
            is HistoryUiState.Empty -> {
                EmptyHistoryState(modifier = Modifier.padding(paddingValues))
            }
            is HistoryUiState.Success -> {
                HistoryList(
                    snaps = uiState.snaps,
                    onReportClick = onReportClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is HistoryUiState.Error -> {
                ErrorHistoryState(
                    message = uiState.message,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = null,
                tint = OnSurfaceVariantColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Reports Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Create your first weather report to see it here",
                fontSize = 14.sp,
                color = OnSurfaceVariantColor
            )
        }
    }
}

@Composable
private fun ErrorHistoryState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = WeatherSnapColors.Error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                color = WeatherSnapColors.Error,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun HistoryList(
    snaps: List<WeatherSnap>,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(snaps, key = { it.id }) { snap ->
            HistoryItem(
                snap = snap,
                onClick = { onReportClick(snap.id) }
            )
        }
    }
}

@Composable
private fun HistoryItem(
    snap: WeatherSnap,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        snap.telemetry?.condition?.name ?: "Unknown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatTimestamp(snap.capturedAt),
                    fontSize = 12.sp,
                    color = OnSurfaceVariantColor
                )
                snap.notes.takeIf { it.isNotEmpty() }?.let { notes ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        notes,
                        fontSize = 14.sp,
                        color = OnSurfaceColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            SyncStatusBadge(status = snap.status)
        }
    }
}

@Composable
private fun SyncStatusBadge(status: SyncStatus) {
    val (color, text) = when (status) {
        SyncStatus.DRAFT -> OnSurfaceVariantColor to "Draft"
        SyncStatus.SYNCING -> WeatherSnapColors.Tertiary to "Syncing"
        SyncStatus.COMPLETED -> WeatherSnapColors.Primary to "Synced"
        SyncStatus.FAILED -> WeatherSnapColors.Error to "Failed"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}