package com.freeb.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.freeb.app.BuildConfig
import com.freeb.app.calls.UserEventSocketClient
import com.freeb.app.chat.ConversationUi
import com.freeb.app.network.FreebApi
import com.freeb.app.ui.components.FreebBottomBar
import com.freeb.app.ui.components.IncomingCallCard
import com.freeb.app.ui.screens.CameraScreen
import com.freeb.app.ui.screens.MediaEditorScreen
import com.freeb.app.ui.screens.ProfileScreen
import com.freeb.app.ui.screens.RealChatDetailScreen
import com.freeb.app.ui.screens.RealChatListScreen
import com.freeb.app.ui.screens.Screen
import com.freeb.app.ui.screens.SettingsScreen
import com.freeb.app.ui.screens.StoriesListScreen
import com.freeb.app.ui.screens.StoryViewerScreen
import com.freeb.app.ui.screens.UserSearchScreen
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executors

private enum class OverlayPage {
    NONE, SEARCH, CHAT_DETAIL, STORY_VIEWER, MEDIA_EDITOR, SETTINGS, CALL
}

@Composable
fun FreebApp(
    cameraPermissionGranted: Boolean,
    requestCameraPermission: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(2) }
    var overlay by remember { mutableStateOf(OverlayPage.NONE) }
    var conversation by remember { mutableStateOf<ConversationUi?>(null) }
    var callId by remember { mutableStateOf<String?>(null) }
    var callInitiator by remember { mutableStateOf(false) }
    var incomingCallId by remember { mutableStateOf<String?>(null) }
    var incomingVideo by remember { mutableStateOf(true) }

    val api = remember { FreebApi(BuildConfig.FREEB_API_URL) }

    DisposableEffect(Unit) {
        val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
        val socket = UserEventSocketClient(BuildConfig.FREEB_API_URL)

        val listener = FirebaseAuth.AuthStateListener { firebase ->
            val uid = firebase.currentUser?.uid ?: return@AuthStateListener
            Thread {
                runCatching {
                    val me = api.me()
                    val backendId = me.getString("id")
                    socket.connect(backendId) { event, payload ->
                        if (event == "CALL_INCOMING") {
                            incomingCallId = payload.optString("call_id").ifBlank { null }
                            incomingVideo = payload.optString("kind").equals("VIDEO", true)
                        }
                    }
                }
            }.start()
        }

        if (auth != null) auth.addAuthStateListener(listener)

        onDispose {
            auth?.removeAuthStateListener(listener)
            socket.close()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)

        if (incomingCallId != null && overlay != OverlayPage.CALL) {
            IncomingCallCard(
                video = incomingVideo,
                onAccept = {
                    callId = incomingCallId
                    callInitiator = false
                    incomingCallId = null
                    overlay = OverlayPage.CALL
                },
                onReject = {
                    val id = incomingCallId ?: return@IncomingCallCard
                    incomingCallId = null
                    Thread { runCatching { api.rejectCall(id) } }.start()
                }
            )
            return@BoxWithConstraints
        }

        if (overlay != OverlayPage.NONE) {
            when (overlay) {
                OverlayPage.SEARCH -> UserSearchScreen(
                    onBack = { overlay = OverlayPage.NONE },
                    onOpenConversation = {
                        conversation = it
                        overlay = OverlayPage.CHAT_DETAIL
                    }
                )
                OverlayPage.CHAT_DETAIL -> conversation?.let { selected ->
                    RealChatDetailScreen(
                        conversation = selected,
                        onBack = {
                            conversation = null
                            overlay = OverlayPage.NONE
                        },
                        onCall = { peerId, video ->
                            Thread {
                                runCatching {
                                    val result = api.createCall(peerId, if (video) "VIDEO" else "AUDIO")
                                    callId = result.getString("call_id")
                                    callInitiator = true
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        overlay = OverlayPage.CALL
                                    }
                                }
                            }.start()
                        }
                    )
                }
                OverlayPage.STORY_VIEWER -> StoryViewerScreen { overlay = OverlayPage.NONE }
                OverlayPage.MEDIA_EDITOR -> MediaEditorScreen { overlay = OverlayPage.NONE }
                OverlayPage.SETTINGS -> SettingsScreen { overlay = OverlayPage.NONE }
                OverlayPage.CALL -> callId?.let { currentCall ->
                    com.freeb.app.calls.CallScreen(
                        callId = currentCall,
                        initiator = callInitiator,
                        onEnd = {
                            val finished = callId
                            callId = null
                            overlay = OverlayPage.NONE
                            if (!finished.isNullOrBlank()) {
                                Thread { runCatching { api.hangupCall(finished) } }.start()
                            }
                        }
                    )
                }
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
                Screen.CHATS -> RealChatListScreen(
                    onOpen = {
                        conversation = it
                        overlay = OverlayPage.CHAT_DETAIL
                    },
                    onSearch = { overlay = OverlayPage.SEARCH }
                )
                Screen.STORIES -> StoriesListScreen { overlay = OverlayPage.STORY_VIEWER }
                Screen.CAMERA -> CameraScreen(
                    cameraPermissionGranted = cameraPermissionGranted,
                    requestCameraPermission = requestCameraPermission,
                    onOpenEditor = { overlay = OverlayPage.MEDIA_EDITOR }
                )
                Screen.DISCOVER -> com.freeb.app.ui.screens.DiscoverScreen {
                    overlay = OverlayPage.STORY_VIEWER
                }
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
