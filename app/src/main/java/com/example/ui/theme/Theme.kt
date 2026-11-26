package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val CodivaLightColorScheme = lightColorScheme(
    primary = CodivaCobalt,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = CodivaNavy,
    secondary = CodivaEmerald,
    onSecondary = Color.White,
    secondaryContainer = CodivaEmeraldSoft,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = CodivaAmber,
    onTertiary = CodivaNavy,
    tertiaryContainer = CodivaAmberSoft,
    onTertiaryContainer = Color(0xFF78350F),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = CodivaRose,
    onError = Color.White
)

private val CodivaDarkColorScheme = darkColorScheme(
    primary = CodivaCobaltBright,
    onPrimary = CodivaNavy,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = CodivaEmeraldSoft,
    tertiary = Color(0xFFFBBF24),
    onTertiary = CodivaNavy,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = CodivaAmberSoft,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFF87171),
    onError = CodivaNavy
)

val CodivaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CodivaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CodivaDarkColorScheme
        else -> CodivaLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CodivaTypography,
        shapes = CodivaShapes,
        content = content
    )
}
