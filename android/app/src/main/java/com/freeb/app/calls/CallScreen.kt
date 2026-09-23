package com.freeb.app.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import com.freeb.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CallScreen(
    callId: String,
    initiator: Boolean,
    onEnd: () -> Unit
) {
    val eglBase = remember { EglBase.create() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val engine = remember { FreebWebRtcEngine(context, eglBase) }
    val signaling = remember { CallSignalingClient(BuildConfig.FREEB_API_URL) }
    var muted by remember { mutableStateOf(false) }
    var remoteTrack by remember { mutableStateOf<VideoTrack?>(null) }

    DisposableEffect(callId) {
        engine.createPeerConnection(
            onRemoteTrack = { remoteTrack = it },
            onIce = { signaling.sendIce(it) }
        )
        val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)
        if (token != null) {
            signaling.connect(
                callId = callId,
                onEvent = { event, payload ->
                    when (event) {
                        "OFFER" -> {
                            engine.setRemoteDescription(payload.getString("type"), payload.getString("sdp"))
                            engine.createAnswer(signaling::sendAnswer)
                        }
                        "ANSWER" -> engine.setRemoteDescription(payload.getString("type"), payload.getString("sdp"))
                        "ICE" -> engine.addIceCandidate(
                            org.webrtc.IceCandidate(
                                payload.optString("sdpMid"),
                                payload.getInt("sdpMLineIndex"),
                                payload.getString("candidate")
                            )
                        )
                        "CALL_ENDED" -> onEnd()
                    }
                }
            )
        }
        if (initiator) engine.createOffer(signaling::sendOffer)

        onDispose {
            signaling.hangup()
            engine.close()
            eglBase.release()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                SurfaceViewRenderer(it).apply {
                    init(eglBase.eglBaseContext, null)
                    setEnableHardwareScaler(true)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                }
            },
            update = { renderer ->
                remoteTrack?.addSink(renderer)
            },
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            factory = {
                SurfaceViewRenderer(it).apply {
                    init(eglBase.eglBaseContext, null)
                    setEnableHardwareScaler(true)
                    setMirror(true)
                    setZOrderMediaOverlay(true)
                    setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                    engine.localVideoTrack.addSink(this)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(118.dp)
        )

        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconControl(if (muted) Icons.Filled.MicOff else Icons.Filled.Mic) {
                muted = !muted
                engine.localAudioTrack.setEnabled(!muted)
            }
            IconControl(Icons.Filled.Cameraswitch) { }
            IconControl(Icons.Filled.CallEnd, end = true) {
                signaling.hangup()
                onEnd()
            }
        }
    }
}

@Composable
private fun IconControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    end: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        Modifier.size(58.dp)
            .background(if (end) Color(0xFFDF3B3B) else Color(0xAA111111), CircleShape)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                        )
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}
