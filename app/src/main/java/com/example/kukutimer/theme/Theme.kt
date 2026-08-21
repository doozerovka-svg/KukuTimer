package com.example.kukutimer.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ZenColorScheme = darkColorScheme(
    primary = ShuIro,
    onPrimary = Shirayuri,
    primaryContainer = ShuIroGlow,
    onPrimaryContainer = Shirayuri,
    secondary = KinGold,
    onSecondary = SumiDark,
    tertiary = MatsuGreen,
    onTertiary = Shirayuri,
    background = SumiDark,
    onBackground = InkTextPrimary,
    surface = SumiSurface,
    onSurface = InkTextPrimary,
    surfaceVariant = SumiCard,
    onSurfaceVariant = InkTextSecondary,
    outline = SumiBorder
)

@Composable
fun KukuTimerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZenColorScheme,
        typography = Typography,
        content = content
    )
}
