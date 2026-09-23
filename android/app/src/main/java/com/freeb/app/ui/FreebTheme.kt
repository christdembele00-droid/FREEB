package com.freeb.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val FreebColors = darkColorScheme(
    background = Color(0xFF07090C),
    surface = Color(0xFF0D1117),
    surfaceVariant = Color(0xFF151B24),
    surfaceContainer = Color(0xFF111720),
    onBackground = Color(0xFFF5FAFF),
    onSurface = Color(0xFFF5FAFF),
    onSurfaceVariant = Color(0xFF8E9AAA),
    primary = Color(0xFF67E8F9),
    onPrimary = Color(0xFF061017),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF080A13),
    tertiary = Color(0xFF43E6A8),
    onTertiary = Color(0xFF04120B),
    outline = Color(0xFF2A3442),
    outlineVariant = Color(0xFF1C2530),
)

private val FreebShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun FreebTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FreebColors,
        shapes = FreebShapes,
        content = content,
    )
}
