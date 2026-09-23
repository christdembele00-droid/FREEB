package com.freeb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Screen { CHATS, STORIES, CAMERA, DISCOVER, PROFILE;
    companion object { fun fromIndex(index: Int) = entries.getOrElse(index) { CAMERA } }
}

@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().padding(24.dp)) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White, fontSize = 32.sp)
            Text(subtitle, color = Color.White.copy(.65f), fontSize = 15.sp, modifier = Modifier.padding(top = 8.dp))
            Text(
                "FREEB",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 26.dp).background(Color.White.copy(.08f), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable fun ProfileScreen() = PlaceholderScreen("FREEB", "Profil, amis, confidentialité et réglages")
