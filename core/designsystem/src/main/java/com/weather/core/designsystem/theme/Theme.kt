package com.weather.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StitchDarkColorScheme = darkColorScheme(
    primary = WeatherSnapColors.Primary,
    onPrimary = WeatherSnapColors.OnPrimary,
    primaryContainer = WeatherSnapColors.PrimaryContainer,
    onPrimaryContainer = WeatherSnapColors.OnPrimaryContainer,
    inversePrimary = WeatherSnapColors.InversePrimary,
    
    secondary = WeatherSnapColors.Secondary,
    onSecondary = WeatherSnapColors.OnSecondary,
    secondaryContainer = WeatherSnapColors.SecondaryContainer,
    onSecondaryContainer = WeatherSnapColors.OnSecondaryContainer,
    
    tertiary = WeatherSnapColors.Tertiary,
    onTertiary = WeatherSnapColors.OnTertiary,
    tertiaryContainer = WeatherSnapColors.TertiaryContainer,
    onTertiaryContainer = WeatherSnapColors.OnTertiaryContainer,
    
    background = WeatherSnapColors.Background,
    onBackground = WeatherSnapColors.OnSurface,
    
    surface = WeatherSnapColors.Surface,
    onSurface = WeatherSnapColors.OnSurface,
    surfaceVariant = WeatherSnapColors.SurfaceContainerHigh,
    onSurfaceVariant = WeatherSnapColors.OnSurfaceVariant,
    
    inverseSurface = WeatherSnapColors.InverseSurface,
    inverseOnSurface = WeatherSnapColors.InverseOnSurface,
    
    outline = WeatherSnapColors.Outline,
    outlineVariant = WeatherSnapColors.OutlineVariant,
    
    error = WeatherSnapColors.Error,
    onError = WeatherSnapColors.OnError,
    errorContainer = WeatherSnapColors.ErrorContainer,
    onErrorContainer = WeatherSnapColors.OnErrorContainer
)

@Composable
fun WeatherSnapTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StitchDarkColorScheme,
        typography = Typography,
        content = content
    )
}