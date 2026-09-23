package com.freeb.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.freeb.app.ui.components.FreebBottomBar
import com.freeb.app.ui.screens.CameraScreen
import com.freeb.app.ui.screens.ChatDetailScreen
import com.freeb.app.ui.screens.ChatListScreen
import com.freeb.app.ui.screens.DiscoverScreen
import com.freeb.app.ui.screens.MediaEditorScreen
import com.freeb.app.ui.screens.ProfileScreen
import com.freeb.app.ui.screens.Screen
import com.freeb.app.ui.screens.SettingsScreen
import com.freeb.app.ui.screens.StoriesListScreen
import com.freeb.app.ui.screens.StoryViewerScreen

private enum class OverlayPage { NONE, CHAT_DETAIL, STORY_VIEWER, MEDIA_EDITOR, SETTINGS }

@Composable
fun FreebApp(
    cameraPermissionGranted: Boolean,
    requestCameraPermission: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(2) }
    var overlay by remember { mutableStateOf(OverlayPage.NONE) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)

        if (overlay != OverlayPage.NONE) {
            when (overlay) {
                OverlayPage.CHAT_DETAIL -> ChatDetailScreen { overlay = OverlayPage.NONE }
                OverlayPage.STORY_VIEWER -> StoryViewerScreen { overlay = OverlayPage.NONE }
                OverlayPage.MEDIA_EDITOR -> MediaEditorScreen { overlay = OverlayPage.NONE }
                OverlayPage.SETTINGS -> SettingsScreen { overlay = OverlayPage.NONE }
                OverlayPage.NONE -> Unit
            }
            return@BoxWithConstraints
        }

        val screen = Screen.fromIndex(selectedIndex)

        AnimatedContent(
            targetState = screen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "freeb-screen"
        ) { current ->
            when (current) {
                Screen.CHATS -> ChatListScreen { overlay = OverlayPage.CHAT_DETAIL }
                Screen.STORIES -> StoriesListScreen { overlay = OverlayPage.STORY_VIEWER }
                Screen.CAMERA -> CameraScreen(
                    cameraPermissionGranted = cameraPermissionGranted,
                    requestCameraPermission = requestCameraPermission,
                    onOpenEditor = { overlay = OverlayPage.MEDIA_EDITOR }
                )
                Screen.DISCOVER -> DiscoverScreen { overlay = OverlayPage.STORY_VIEWER }
                Screen.PROFILE -> ProfileScreen { overlay = OverlayPage.SETTINGS }
            }
        }

        FreebBottomBar(
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
            scale = scale
        )
    }
}
