package com.ghostbug.heavyliftsapp.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val HeavyLiftsColorScheme = lightColorScheme(
    primary = HeavyLiftsColors.Accent,
    onPrimary = HeavyLiftsColors.NavyStrong,
    primaryContainer = HeavyLiftsColors.AccentSoft,
    onPrimaryContainer = HeavyLiftsColors.NavyStrong,
    secondary = HeavyLiftsColors.BgChip,
    onSecondary = HeavyLiftsColors.Navy,
    tertiary = HeavyLiftsColors.AccentStrong,
    onTertiary = HeavyLiftsColors.White,
    background = HeavyLiftsColors.Bg,
    onBackground = HeavyLiftsColors.Fg1,
    surface = HeavyLiftsColors.White,
    onSurface = HeavyLiftsColors.Fg1,
    surfaceVariant = HeavyLiftsColors.BgElevated,
    onSurfaceVariant = HeavyLiftsColors.Fg2,
    error = HeavyLiftsColors.Danger,
    onError = HeavyLiftsColors.White,
    errorContainer = HeavyLiftsColors.DangerSoft,
    onErrorContainer = HeavyLiftsColors.Danger,
    outline = HeavyLiftsColors.BorderDefault,
    outlineVariant = HeavyLiftsColors.BorderSubtle,
    scrim = HeavyLiftsColors.Scrim
)

private fun typeStyle(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    color: androidx.compose.ui.graphics.Color,
    letterSpacing: Float = 0f
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    color = color,
    letterSpacing = letterSpacing.sp
)

private val HeavyLiftsTypography = Typography(
    displayLarge = typeStyle(HeavyLiftsType.Display, FontWeight.ExtraBold, 56, 60, HeavyLiftsColors.Fg1, -1f),
    displayMedium = typeStyle(HeavyLiftsType.Display, FontWeight.Bold, 48, 54, HeavyLiftsColors.Fg1, -0.5f),
    displaySmall = typeStyle(HeavyLiftsType.Display, FontWeight.Bold, 40, 46, HeavyLiftsColors.Fg1),
    headlineLarge = typeStyle(HeavyLiftsType.Display, FontWeight.Bold, 32, 38, HeavyLiftsColors.Fg1),
    headlineMedium = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 28, 34, HeavyLiftsColors.Fg1),
    headlineSmall = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 24, 30, HeavyLiftsColors.Fg1),
    titleLarge = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 22, 28, HeavyLiftsColors.Fg1),
    titleMedium = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 18, 24, HeavyLiftsColors.Fg1),
    titleSmall = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 16, 22, HeavyLiftsColors.Fg1),
    bodyLarge = typeStyle(HeavyLiftsType.Body, FontWeight.Normal, 16, 24, HeavyLiftsColors.Fg2),
    bodyMedium = typeStyle(HeavyLiftsType.Body, FontWeight.Normal, 14, 20, HeavyLiftsColors.Fg2),
    bodySmall = typeStyle(HeavyLiftsType.Body, FontWeight.Normal, 12, 18, HeavyLiftsColors.Fg3),
    labelLarge = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 16, 22, HeavyLiftsColors.Fg1),
    labelMedium = typeStyle(HeavyLiftsType.Body, FontWeight.SemiBold, 14, 20, HeavyLiftsColors.Fg2),
    labelSmall = typeStyle(HeavyLiftsType.Display, FontWeight.SemiBold, 12, 16, HeavyLiftsColors.Fg3, 0.3f)
)

private val HeavyLiftsShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun Demo103Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeavyLiftsColorScheme,
        typography = HeavyLiftsTypography,
        shapes = HeavyLiftsShapes,
        content = content
    )
}
