package com.weather.core.designsystem.responsive

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ResponsiveValues(
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean,
    val screenPadding: Dp,
    val cardPadding: Dp,
    val sectionSpacing: Dp,
    val itemSpacing: Dp,
    val gridGap: Dp,
    val heroHeight: Dp,
    val touchTargetMin: Dp = 48.dp,
    val iconSize: Dp,
    val avatarSize: Dp,
    val buttonHeight: Dp,
    val cardCornerRadius: Dp,
    val timelineNodeSize: Dp,
    val photoHeroHeight: Dp,
    val detailHeroHeight: Dp
)

@Composable
fun calculateResponsiveValues(windowSizeClass: WindowSizeClass): ResponsiveValues {
    val widthSizeClass = windowSizeClass.widthSizeClass
    
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> ResponsiveValues(
            isCompact = true,
            isMedium = false,
            isExpanded = false,
            screenPadding = 12.dp,
            cardPadding = 12.dp,
            sectionSpacing = 16.dp,
            itemSpacing = 12.dp,
            gridGap = 12.dp,
            heroHeight = 280.dp,
            touchTargetMin = 40.dp,
            iconSize = 18.dp,
            avatarSize = 40.dp,
            buttonHeight = 44.dp,
            cardCornerRadius = 10.dp,
            timelineNodeSize = 28.dp,
            photoHeroHeight = 180.dp,
            detailHeroHeight = 220.dp
        )
        WindowWidthSizeClass.Medium -> ResponsiveValues(
            isCompact = false,
            isMedium = true,
            isExpanded = false,
            screenPadding = 16.dp,
            cardPadding = 14.dp,
            sectionSpacing = 20.dp,
            itemSpacing = 14.dp,
            gridGap = 14.dp,
            heroHeight = 300.dp,
            touchTargetMin = 44.dp,
            iconSize = 20.dp,
            avatarSize = 44.dp,
            buttonHeight = 48.dp,
            cardCornerRadius = 12.dp,
            timelineNodeSize = 32.dp,
            photoHeroHeight = 140.dp,
            detailHeroHeight = 200.dp
        )
        WindowWidthSizeClass.Expanded -> ResponsiveValues(
            isCompact = false,
            isMedium = false,
            isExpanded = true,
            screenPadding = 20.dp,
            cardPadding = 16.dp,
            sectionSpacing = 24.dp,
            itemSpacing = 16.dp,
            gridGap = 16.dp,
            heroHeight = 320.dp,
            touchTargetMin = 48.dp,
            iconSize = 22.dp,
            avatarSize = 48.dp,
            buttonHeight = 50.dp,
            cardCornerRadius = 12.dp,
            timelineNodeSize = 36.dp,
            photoHeroHeight = 160.dp,
            detailHeroHeight = 220.dp
        )
        else -> ResponsiveValues(
            isCompact = true,
            isMedium = false,
            isExpanded = false,
            screenPadding = 12.dp,
            cardPadding = 12.dp,
            sectionSpacing = 16.dp,
            itemSpacing = 12.dp,
            gridGap = 12.dp,
            heroHeight = 280.dp,
            touchTargetMin = 40.dp,
            iconSize = 18.dp,
            avatarSize = 40.dp,
            buttonHeight = 44.dp,
            cardCornerRadius = 10.dp,
            timelineNodeSize = 28.dp,
            photoHeroHeight = 180.dp,
            detailHeroHeight = 220.dp
        )
    }
}

@Composable
fun WindowSizeClass.isTablet(): Boolean {
    return widthSizeClass != WindowWidthSizeClass.Compact
}

@Composable
fun WindowSizeClass.isPhone(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}
