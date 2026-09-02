package com.sajda.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SajdaPrimary = Color(0xFF09090B)
val SajdaPrimaryContainer = Color(0xFF18181B)
val SajdaPrimaryFixed = Color(0xFF27272A)
val SajdaPrimaryFixedDim = Color(0xFF3F3F46)
val SajdaSecondary = Color(0xFF0284C7)
val SajdaSecondaryContainer = Color(0xFFE0F2FE)
val SajdaAccent = Color(0xFF09090B)
val SajdaBackground = Color(0xFFF4F4F5)
val SajdaSurface = Color(0xFFFFFFFF)
val SajdaSurfaceLow = Color(0xFFF4F4F5)
val SajdaSurfaceLowest = Color(0xFFFFFFFF)
val SajdaSurfaceHigh = Color(0xFFE4E4E7)
val SajdaSurfaceHighest = Color(0xFFD4D4D8)
val SajdaOutline = Color(0xFF71717A)
val SajdaOutlineVariant = Color(0xFFE4E4E7)
val SajdaOnSurface = Color(0xFF09090B)
val SajdaOnSurfaceVariant = Color(0xFF71717A)
val SajdaError = Color(0xFFEF4444)
val SajdaWarm = Color(0xFF09090B)

val SajdaDarkBackground = Color(0xFF09090B)
val SajdaDarkSurface = Color(0xFF18181B)
val SajdaDarkSurfaceLow = Color(0xFF09090B)
val SajdaDarkSurfaceLowest = Color(0xFF18181B)
val SajdaDarkSurfaceHigh = Color(0xFF27272A)
val SajdaDarkSurfaceHighest = Color(0xFF3F3F46)
val SajdaDarkOnSurface = Color(0xFFFAFAFA)
val SajdaDarkOnSurfaceVariant = Color(0xFFA1A1AA)

private val LightColorScheme = lightColorScheme(
    primary = SajdaPrimary,
    onPrimary = Color.White,
    primaryContainer = SajdaPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = SajdaSecondary,
    onSecondary = Color.White,
    secondaryContainer = SajdaSecondaryContainer,
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = SajdaWarm,
    onTertiary = Color.White,
    background = SajdaBackground,
    onBackground = SajdaOnSurface,
    surface = SajdaSurface,
    onSurface = SajdaOnSurface,
    surfaceVariant = SajdaSurfaceHighest,
    onSurfaceVariant = SajdaOnSurfaceVariant,
    surfaceTint = SajdaPrimary,
    outline = SajdaOutline,
    outlineVariant = SajdaOutlineVariant,
    scrim = Color.Black.copy(alpha = 0.6f),
    error = SajdaError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    inverseSurface = Color(0xFF27272A),
    inverseOnSurface = Color(0xFFFAFAFA),
    inversePrimary = SajdaPrimaryFixedDim
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0369A1),
    primaryContainer = Color(0xFF18181B),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFFFAFAFA),
    onSecondary = Color(0xFF18181B),
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color(0xFFFAFAFA),
    tertiary = Color(0xFFFAFAFA),
    onTertiary = Color(0xFF18181B),
    tertiaryContainer = Color(0xFF27272A),
    onTertiaryContainer = Color(0xFFFAFAFA),
    background = SajdaDarkBackground,
    onBackground = SajdaDarkOnSurface,
    surface = SajdaDarkSurface,
    onSurface = SajdaDarkOnSurface,
    surfaceVariant = SajdaDarkSurfaceHighest,
    onSurfaceVariant = SajdaDarkOnSurfaceVariant,
    surfaceTint = Color(0xFF38BDF8),
    outline = Color(0xFF52525B),
    outlineVariant = Color(0xFF27272A),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5),
    inverseSurface = Color(0xFFFAFAFA),
    inverseOnSurface = Color(0xFF09090B),
    inversePrimary = SajdaPrimary
)

@Composable
fun SajdaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SajdaTypography,
        content = content
    )
}

@Composable
fun sajdaBackgroundBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(
            colors.background,
            colors.background,
            colors.background
        )
    )
}

@Composable
fun sajdaHeroBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(
            colors.primaryContainer,
            colors.primaryContainer,
            colors.primaryContainer
        )
    )
}
