package com.freeb.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FreebColors = darkColorScheme(
    background = Color.Black,
    surface = Color(0xFF080808),
    surfaceContainer = Color(0xFF121212),
    onBackground = Color.White,
    onSurface = Color.White,
    primary = Color.White,
    onPrimary = Color.Black
)

@Composable
fun FreebTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = FreebColors, content = content)
}
