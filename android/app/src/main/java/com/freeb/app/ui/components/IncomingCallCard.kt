package com.freeb.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IncomingCallCard(
    video: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 28.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.widthIn(max = 420.dp)
                .background(Color(0xF5111111), RoundedCornerShape(26.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                if (video) "Appel vidéo entrant" else "Appel entrant",
                color = Color.White
            )
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(
                    Modifier.background(Color(0xFF2E7D32), RoundedCornerShape(50))
                        .clickable(onClick = onAccept)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Call, null, tint = Color.White)
                }
                Box(
                    Modifier.background(Color(0xFFC62828), RoundedCornerShape(50))
                        .clickable(onClick = onReject)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.CallEnd, null, tint = Color.White)
                }
            }
        }
    }
}
