package com.freeb.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.freeb.app.ui.screens.CameraScreen
import com.freeb.app.ui.screens.PlaceholderScreen
import com.freeb.app.ui.screens.ProfileScreen
import com.freeb.app.ui.screens.Screen

@Composable
fun FreebApp(
    cameraPermissionGranted: Boolean,
    requestCameraPermission: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(2) }
    val screen = Screen.fromIndex(selectedIndex)

    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "freeb-screen"
        ) { current ->
            when (current) {
                Screen.CHATS -> PlaceholderScreen("Messages", "Conversations, snaps et groupes")
                Screen.STORIES -> PlaceholderScreen("Stories", "Stories de tes amis")
                Screen.CAMERA -> CameraScreen(cameraPermissionGranted, requestCameraPermission)
                Screen.DISCOVER -> PlaceholderScreen("Discover", "Créateurs et contenus")
                Screen.PROFILE -> ProfileScreen()
            }
        }

        if (screen == Screen.CAMERA) {
            CameraBottomNavigationBar(selectedIndex, onSelect = { selectedIndex = it })
        } else {
            BottomNavigationBar(selectedIndex, onSelect = { selectedIndex = it })
        }
    }
}
