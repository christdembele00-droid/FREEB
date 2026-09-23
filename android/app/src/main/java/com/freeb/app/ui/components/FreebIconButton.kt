package com.freeb.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun FreebIconButton(
    icon: ImageVector,
    size: Dp,
    onClick: () -> Unit,
    selected: Boolean = false,
    filled: Boolean = false
) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when {
                    filled -> Color.White
                    selected -> Color.White.copy(alpha = 0.16f)
                    else -> Color.Black.copy(alpha = 0.42f)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (filled) Color.Black else Color.White,
            modifier = Modifier.size(size * 0.46f)
        )
    }
}
