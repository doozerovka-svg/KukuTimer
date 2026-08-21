package com.example.kukutimer.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val ZenLightColorScheme = lightColorScheme(
    primary = ShuIro,
    onPrimary = BeigeSurface,
    primaryContainer = ShuIroLight,
    onPrimaryContainer = ShuIro,
    secondary = KinGold,
    onSecondary = BeigeSurface,
    secondaryContainer = KinGoldLight,
    onSecondaryContainer = KinGold,
    tertiary = MatsuGreen,
    onTertiary = BeigeSurface,
    tertiaryContainer = MatsuGreenLight,
    onTertiaryContainer = MatsuGreen,
    background = BeigeBackground,
    onBackground = InkPrimary,
    surface = BeigeSurface,
    onSurface = InkPrimary,
    surfaceVariant = BeigeCard,
    onSurfaceVariant = InkSecondary,
    outline = BeigeBorder
)

@Composable
fun KukuTimerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZenLightColorScheme,
        typography = Typography,
        content = content
    )
}
