package com.freeb.app.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Adaptive {
    fun uiScale(width: Dp, height: Dp): Float {
        val shortest = min(width.value, height.value)
        return (shortest / 360f).coerceIn(0.86f, 1.28f)
    }

    fun scaled(value: Dp, scale: Float): Dp = value * scale
    fun safeContentPadding(scale: Float): Dp = scaled(16.dp, scale)
    fun maxContentWidth(width: Dp): Dp = minOf(width, 520.dp)
    fun adaptiveIcon(scale: Float, compact: Boolean = false): Dp =
        scaled(if (compact) 36.dp else 44.dp, scale)
    fun adaptiveCorner(scale: Float): Dp = scaled(22.dp, scale)
    fun adaptiveTouch(scale: Float): Dp = maxOf(44.dp, scaled(48.dp, scale))
}
