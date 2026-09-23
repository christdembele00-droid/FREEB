package com.freeb.app.ui.screens

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.freeb.app.NativeBridge
import com.freeb.app.media.CaptureManager
import com.freeb.app.media.CapturedMedia
import com.freeb.app.media.MediaKind
import com.freeb.app.media.MediaUploadManager
import com.freeb.app.network.FreebApi
import com.freeb.app.ui.Adaptive
import com.freeb.app.ui.components.FreebIconButton

private data class CameraSession(
    val camera: Camera,
    val imageCapture: ImageCapture,
    val videoCapture: VideoCapture<Recorder>
)

@Composable
fun CameraScreen(
    cameraPermissionGranted: Boolean,
    requestCameraPermission: () -> Unit,
    onOpenEditor: () -> Unit = {}
) {
    val context = LocalContext.current
    var frontCamera by remember { mutableStateOf(false) }
    var flashOn by remember { mutableStateOf(false) }
    var soundOn by remember { mutableStateOf(true) }
    var session by remember { mutableStateOf<CameraSession?>(null) }
    var recording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<androidx.camera.video.Recording?>(null) }
    val captureManager = remember(context) { CaptureManager(context) }
    val uploadManager = remember(context) { MediaUploadManager(FreebApi(com.freeb.app.BuildConfig.FREEB_API_URL)) }

    BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
        val scale = Adaptive.uiScale(maxWidth, maxHeight)

        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            enabled = cameraPermissionGranted,
            frontCamera = frontCamera,
            flashOn = flashOn,
            onSessionReady = { session = it }
        )

        if (!cameraPermissionGranted) {
            PermissionOverlay(scale, requestCameraPermission)
        } else {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = Adaptive.safeContentPadding(scale), vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FreebIconButton(Icons.Filled.PersonOutline, Adaptive.adaptiveIcon(scale), {})
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FreebIconButton(
                        if (flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        Adaptive.adaptiveIcon(scale, true),
                        { flashOn = !flashOn },
                        selected = flashOn
                    )
                    FreebIconButton(Icons.Filled.Search, Adaptive.adaptiveIcon(scale, true), {})
                    FreebIconButton(Icons.Filled.ChatBubbleOutline, Adaptive.adaptiveIcon(scale, true), {})
                }
            }

            Column(
                Modifier.align(Alignment.CenterEnd).padding(end = Adaptive.safeContentPadding(scale)).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FreebIconButton(Icons.Filled.FlipCameraAndroid, Adaptive.adaptiveIcon(scale, true), {
                    if (activeRecording == null) frontCamera = !frontCamera
                })
                FreebIconButton(
                    if (soundOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                    Adaptive.adaptiveIcon(scale, true),
                    { soundOn = !soundOn },
                    selected = soundOn
                )
                FreebIconButton(Icons.Filled.AutoAwesome, Adaptive.adaptiveIcon(scale, true), {})
                FreebIconButton(Icons.Filled.Image, Adaptive.adaptiveIcon(scale, true), onOpenEditor)
            }

            Box(
                Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = 102.dp)
                    .size(84.dp)
                    .background(Color.White, CircleShape)
                    .pointerInput(session, recording, soundOn) {
                        detectTapGestures(
                            onTap = {
                                val current = session ?: return@detectTapGestures
                                if (activeRecording != null) {
                                    activeRecording?.stop()
                                    activeRecording = null
                                    recording = false
                                    return@detectTapGestures
                                }
                                captureManager.capturePhoto(current.imageCapture) { result ->
                                    result.onSuccess { file ->
                                        uploadManager.upload(
                                            com.freeb.app.media.CapturedMedia(file, MediaKind.IMAGE, "image/jpeg"),
                                            "freeb/dev/camera",
                                            onSuccess = { onOpenEditor() },
                                            onError = { onOpenEditor() }
                                        )
                                    }
                                }
                            },
                            onLongPress = {
                                val current = session ?: return@detectTapGestures
                                if (activeRecording == null) {
                                    activeRecording = captureManager.startVideo(
                                        current.videoCapture,
                                        audioEnabled = soundOn,
                                        onStarted = { recording = true },
                                        onFinalized = {
                                            recording = false
                                            activeRecording = null
                                            it.onSuccess { file ->
                                                uploadManager.upload(
                                                    com.freeb.app.media.CapturedMedia(file, MediaKind.VIDEO, "video/mp4"),
                                                    "freeb/dev/camera",
                                                    onSuccess = { onOpenEditor() },
                                                    onError = { onOpenEditor() }
                                                )
                                            }
                                        }
                                    )
                                } else {
                                    activeRecording?.stop()
                                    activeRecording = null
                                    recording = false
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(if (recording) 48.dp else 66.dp)
                        .background(Color.Black, CircleShape)
                )
            }

            Box(
                Modifier.align(Alignment.BottomStart)
                    .padding(start = Adaptive.safeContentPadding(scale), bottom = 112.dp)
                    .size(18.dp)
                    .background(
                        if (NativeBridge.vulkanAvailable()) Color.White else Color.White.copy(.28f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier,
    enabled: Boolean,
    frontCamera: Boolean,
    flashOn: Boolean,
    onSessionReady: (CameraSession) -> Unit
) {
    if (!enabled) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val lensFacing = if (frontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
    val providerFuture = remember(lensFacing) { ProcessCameraProvider.getInstance(context) }

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(providerFuture, lifecycleOwner, lensFacing, flashOn) {
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
                .also { it.flashMode = if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            val videoCapture = VideoCapture.withOutput(recorder)
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            runCatching {
                provider.unbindAll()
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageCapture,
                    videoCapture
                )
                onSessionReady(CameraSession(camera, imageCapture, videoCapture))
            }
        }
        providerFuture.addListener(listener, executor)
        onDispose {
            runCatching { providerFuture.get().unbindAll() }
        }
    }
}

@Composable
private fun PermissionOverlay(scale: Float, onRequest: () -> Unit) {
    Box(
        Modifier.fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF252525), Color.Black)))
            .pointerInput(Unit) { detectTapGestures { onRequest() } },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(104.dp).background(Color.White.copy(.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}
