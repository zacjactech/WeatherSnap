package com.weather.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.weather.core.designsystem.responsive.ResponsiveValues
import com.weather.core.designsystem.theme.BackgroundColor
import com.weather.core.designsystem.theme.OnSurfaceColor
import com.weather.core.designsystem.theme.OnSurfaceVariantColor
import com.weather.core.designsystem.theme.OutlineVariantColor
import com.weather.core.designsystem.theme.PrimaryColor
import com.weather.core.designsystem.theme.WeatherSnapColors

enum class WeatherSnapTab {
    Home,
    Camera,
    Reports,
    Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSnapTopBar(
    title: String = "WeatherSnap",
    showSearch: Boolean = true,
    showProfile: Boolean = true,
    showOverflow: Boolean = false,
    profileUrl: String? = null,
    onSearchClick: () -> Unit = {},
    onOverflowClick: () -> Unit = {},
    action: (@Composable () -> Unit)? = null,
    responsive: ResponsiveValues? = null
) {
    val avatarSize = responsive?.avatarSize ?: 42.dp
    val iconSize = responsive?.iconSize ?: 22.dp
    val titleFontSize = when {
        responsive?.isExpanded == true -> 25.sp
        responsive?.isMedium == true -> 23.sp
        else -> 21.sp
    }
    val endPadding = responsive?.screenPadding ?: 16.dp

    CenterAlignedTopAppBar(
        windowInsets = WindowInsets(0.dp),
        title = {
            Text(
                text = title,
                color = PrimaryColor,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (showSearch) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.padding(start = (responsive?.screenPadding ?: 12.dp) / 2)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryColor, modifier = Modifier.size(iconSize * 1.05f))
                }
            }
        },
        actions = {
            action?.invoke()
            if (showOverflow) {
                IconButton(onClick = onOverflowClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = OnSurfaceColor, modifier = Modifier.size(iconSize))
                }
            }
            if (showProfile) {
                Box(
                    modifier = Modifier
                        .padding(end = endPadding)
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(WeatherSnapColors.SurfaceContainerHigh)
                        .border(1.dp, OutlineVariantColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileUrl != null) {
                        AsyncImage(
                            model = profileUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = OnSurfaceVariantColor,
                            modifier = Modifier.size(avatarSize * 0.6f)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundColor.copy(alpha = 0.96f)
        )
    )
}

@Composable
fun WeatherSnapBottomNav(
    selectedTab: WeatherSnapTab,
    onNavigateToHome: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    responsive: ResponsiveValues? = null
) {
    val navPadding = responsive?.itemSpacing ?: 10.dp
    val navIconSize = responsive?.iconSize ?: 20.dp
    val navLabelSize = when {
        responsive?.isExpanded == true -> 15.sp
        responsive?.isMedium == true -> 14.sp
        else -> 13.sp
    }
    val navItemPadding = responsive?.cardPadding ?: 14.dp
    val navCorner = responsive?.cardCornerRadius ?: 22.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WeatherSnapColors.SurfaceContainerLow,
        border = BorderStroke(1.dp, OutlineVariantColor.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = navPadding, vertical = navPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherSnapBottomNavItem(
                tab = WeatherSnapTab.Home,
                selectedTab = selectedTab,
                icon = Icons.Default.Home,
                label = "Home",
                onClick = onNavigateToHome,
                iconSize = navIconSize,
                labelSize = navLabelSize,
                itemPadding = navItemPadding,
                cornerRadius = navCorner
            )
            WeatherSnapBottomNavItem(
                tab = WeatherSnapTab.Camera,
                selectedTab = selectedTab,
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                onClick = onNavigateToCamera,
                iconSize = navIconSize,
                labelSize = navLabelSize,
                itemPadding = navItemPadding,
                cornerRadius = navCorner
            )
            WeatherSnapBottomNavItem(
                tab = WeatherSnapTab.Reports,
                selectedTab = selectedTab,
                icon = Icons.Default.Assignment,
                label = "Reports",
                onClick = onNavigateToReports,
                iconSize = navIconSize,
                labelSize = navLabelSize,
                itemPadding = navItemPadding,
                cornerRadius = navCorner
            )
            WeatherSnapBottomNavItem(
                tab = WeatherSnapTab.Settings,
                selectedTab = selectedTab,
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onNavigateToSettings,
                iconSize = navIconSize,
                labelSize = navLabelSize,
                itemPadding = navItemPadding,
                cornerRadius = navCorner
            )
        }
    }
}

@Composable
private fun WeatherSnapBottomNavItem(
    tab: WeatherSnapTab,
    selectedTab: WeatherSnapTab,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    labelSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    itemPadding: androidx.compose.ui.unit.Dp = 14.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 18.dp
) {
    val selected = selectedTab == tab
    val contentColor by animateColorAsState(
        targetValue = if (selected) PrimaryColor else OnSurfaceVariantColor,
        label = "bottomNavColor"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = itemPadding, vertical = itemPadding / 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.height(itemPadding / 4))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = labelSize, letterSpacing = 0.sp),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
