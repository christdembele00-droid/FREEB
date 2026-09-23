package com.freeb.app.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

fun uiScale(width: Dp, height: Dp): Float {
    val base = 360f
    val shortest = min(width.value, height.value)
    return (shortest / base).coerceIn(0.86f, 1.28f)
}

fun scaled(value: Dp, scale: Float): Dp = value * scale

fun safeContentPadding(scale: Float): Dp = scaled(16.dp, scale)

fun maxContentWidth(width: Dp): Dp = min(width, 520.dp)

fun adaptiveIcon(scale: Float, compact: Boolean = false): Dp =
    scaled(if (compact) 36.dp else 44.dp, scale)

fun adaptiveCorner(scale: Float): Dp = scaled(22.dp, scale)

fun adaptiveTouch(scale: Float): Dp = max(44.dp, scaled(48.dp, scale))
