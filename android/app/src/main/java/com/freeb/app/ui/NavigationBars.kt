package com.freeb.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CameraBottomNavigationBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xCC000000), CircleShape).padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("▣", "Chats", 0, selectedIndex, onSelect)
            NavItem("○", "Stories", 1, selectedIndex, onSelect)
            Box(Modifier.size(66.dp).background(Color.White, CircleShape).clickable { onSelect(2) }, contentAlignment = Alignment.Center) {
                Box(Modifier.size(54.dp).background(Color.Black, CircleShape))
            }
            NavItem("✦", "Discover", 3, selectedIndex, onSelect)
            NavItem("●", "Profil", 4, selectedIndex, onSelect)
        }
    }
}

@Composable
fun BottomNavigationBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xF20A0A0A), CircleShape).padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("▣", "Chats", 0, selectedIndex, onSelect)
            NavItem("○", "Stories", 1, selectedIndex, onSelect)
            NavItem("●", "Caméra", 2, selectedIndex, onSelect)
            NavItem("✦", "Discover", 3, selectedIndex, onSelect)
            NavItem("●", "Profil", 4, selectedIndex, onSelect)
        }
    }
}

@Composable
private fun NavItem(glyph: String, label: String, index: Int, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val selected = index == selectedIndex
    Column(
        Modifier.clickable { onSelect(index) }.padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = Color.White, fontSize = if (selected) 22.sp else 19.sp)
        Text(label, color = Color.White.copy(if (selected) 1f else .6f), fontSize = 10.sp)
    }
}
