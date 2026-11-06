package com.pheonix.camera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CameraColors = darkColorScheme(
    primary = Color(0xFFD6B66A),
    onPrimary = Color(0xFF17140D),
    background = Color(0xFF101010),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF4F1E8),
    secondary = Color(0xFFB7A77D)
)

@Composable
fun LrmaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CameraColors, content = content)
}
