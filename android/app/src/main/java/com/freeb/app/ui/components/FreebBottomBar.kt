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
                .background(Color(0xD9070707), CircleShape)
                .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            item(Icons.Filled.ChatBubbleOutline, 0, selectedIndex, onSelect, scale)
            item(Icons.Filled.StarBorder, 1, selectedIndex, onSelect, scale)
            item(Icons.Filled.PhotoCamera, 2, selectedIndex, onSelect, scale, center = true)
            item(Icons.Filled.Explore, 3, selectedIndex, onSelect, scale)
            item(Icons.Filled.PersonOutline, 4, selectedIndex, onSelect, scale)
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
    center: Boolean = false
) {
    Box(
        Modifier
            .size(if (center) Adaptive.scaled(64.dp, scale) else Adaptive.scaled(48.dp, scale))
            .background(if (center) Color.White else Color.Transparent, CircleShape)
            .padding(if (center) 7.dp else 4.dp)
            .background(if (center) Color.Black else if (index == selectedIndex) Color.White.copy(.12f) else Color.Transparent, CircleShape)
            .then(Modifier),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .matchParentSize()
                .background(Color.Transparent, CircleShape)
                .clickable { onSelect(index) }
        )
        Icon(
            icon,
            contentDescription = null,
            tint = if (center) Color.White else Color.White.copy(if (index == selectedIndex) 1f else .72f),
            modifier = Modifier.size(if (center) 28.dp else 22.dp)
        )
    }
}
