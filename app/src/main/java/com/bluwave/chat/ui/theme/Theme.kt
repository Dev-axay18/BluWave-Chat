package com.bluwave.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Cyberpunk/Neon Color Palette
val DeepNavy = Color(0xFF0A0A23)
val Indigo = Color(0xFF1A1A3A)
val NeonPurple = Color(0xFF8A2BE2)
val HotPink = Color(0xFFFF1493)
val ElectricBlue = Color(0xFF00BFFF)
val NeonGreen = Color(0xFF00FF7F)
val NeonYellow = Color(0xFFFFFF00)
val NeonOrange = Color(0xFFFF8C00)

// Glassmorphism Colors
val GlassWhite = Color(0xFFFFFFFF)
val GlassBlack = Color(0xFF000000)
val GlassTransparent = Color(0x00FFFFFF)
val GlassSemiTransparent = Color(0x80FFFFFF)

// Status Colors
val ConnectedGreen = Color(0xFF00FF7F)
val DisconnectedRed = Color(0xFFFF4444)
val ConnectingYellow = Color(0xFFFFD700)

// Standard Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Gray = Color(0xFF808080)
val LightGray = Color(0xFFD3D3D3)
val DarkGray = Color(0xFF404040)

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = White,
    primaryContainer = DeepNavy,
    onPrimaryContainer = White,
    secondary = HotPink,
    onSecondary = White,
    secondaryContainer = Indigo,
    onSecondaryContainer = White,
    tertiary = ElectricBlue,
    onTertiary = White,
    tertiaryContainer = DeepNavy,
    onTertiaryContainer = White,
    background = DeepNavy,
    onBackground = White,
    surface = Indigo,
    onSurface = White,
    surfaceVariant = DeepNavy,
    onSurfaceVariant = LightGray,
    outline = NeonPurple,
    outlineVariant = ElectricBlue
)

private val LightColorScheme = lightColorScheme(
    primary = NeonPurple,
    onPrimary = White,
    primaryContainer = ElectricBlue,
    onPrimaryContainer = Black,
    secondary = HotPink,
    onSecondary = White,
    secondaryContainer = NeonGreen,
    onSecondaryContainer = Black,
    tertiary = ElectricBlue,
    onTertiary = White,
    tertiaryContainer = NeonYellow,
    onTertiaryContainer = Black,
    background = White,
    onBackground = Black,
    surface = LightGray,
    onSurface = Black,
    surfaceVariant = White,
    onSurfaceVariant = Black,
    outline = NeonPurple,
    outlineVariant = ElectricBlue
)

@Composable
fun BluWaveChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
