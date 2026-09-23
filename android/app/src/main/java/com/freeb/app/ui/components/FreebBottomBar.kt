package com.freeb.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freeb.app.ui.Adaptive

private val BarColor = Color(0xE611161D)
private val Accent = Color(0xFF67E8F9)
private val Muted = Color(0xFF708091)

@Composable
fun FreebBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit, scale: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Adaptive.safeContentPadding(scale), vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(BarColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            item(Icons.Filled.ChatBubbleOutline, 0, selectedIndex, onSelect, scale, "messages")
            item(Icons.Filled.StarBorder, 1, selectedIndex, onSelect, scale, "stories")
            item(Icons.Filled.PhotoCamera, 2, selectedIndex, onSelect, scale, "camera", center = true)
            item(Icons.Filled.Explore, 3, selectedIndex, onSelect, scale, "discover")
            item(Icons.Filled.PersonOutline, 4, selectedIndex, onSelect, scale, "profile")
        }
    }
}

@Composable
private fun item(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    index: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    scale: Float,
    contentDescription: String,
    center: Boolean = false,
) {
    val selected = index == selectedIndex
    Box(
        Modifier
            .size(if (center) Adaptive.scaled(62.dp, scale) else Adaptive.scaled(48.dp, scale))
            .background(
                when {
                    center -> Accent
                    selected -> Color.White.copy(alpha = .08f)
                    else -> Color.Transparent
                },
                CircleShape,
            )
            .clickable { onSelect(index) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                center -> Color(0xFF061017)
                selected -> Color.White
                else -> Muted
            },
            modifier = Modifier.size(if (center) 26.dp else 22.dp),
        )
    }
}
