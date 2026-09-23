package com.freeb.app.ui.screens

import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScreen(
    cameraPermissionGranted: Boolean,
    requestCameraPermission: () -> Unit
) {
    var frontCamera by remember { mutableStateOf(false) }
    var flashOn by remember { mutableStateOf(false) }
    var soundOn by remember { mutableStateOf(true) }
    var recording by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            enabled = cameraPermissionGranted,
            frontCamera = frontCamera
        )

        if (!cameraPermissionGranted) {
            PermissionOverlay(onRequest = requestCameraPermission)
        } else {
            CameraTopBar(
                onFlash = { flashOn = !flashOn },
                flashOn = flashOn,
                onSearch = { },
                onChat = { }
            )

            CameraSideControls(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp),
                frontCamera = frontCamera,
                onFlip = { frontCamera = !frontCamera },
                soundOn = soundOn,
                onSound = { soundOn = !soundOn }
            )

            CameraCaptureControls(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 102.dp),
                recording = recording,
                onCapture = { recording = !recording }
            )
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier,
    enabled: Boolean,
    frontCamera: Boolean
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

    DisposableEffect(providerFuture, lifecycleOwner, lensFacing) {
        val listener = Runnable {
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (_: Exception) {
                // The UI remains usable; the fallback/error state can be connected later.
            }
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose { }
    }
}

@Composable
private fun CameraTopBar(
    flashOn: Boolean,
    onFlash: () -> Unit,
    onSearch: () -> Unit,
    onChat: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CameraCircleButton("●", "Profil", onClick = { })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CameraCircleButton(if (flashOn) "⚡" else "ϟ", "Flash", onFlash)
            CameraCircleButton("⌕", "Recherche", onSearch)
            CameraCircleButton("▣", "Chat", onChat)
        }
    }
}

@Composable
private fun CameraSideControls(
    modifier: Modifier,
    frontCamera: Boolean,
    onFlip: () -> Unit,
    soundOn: Boolean,
    onSound: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CameraCircleButton("↻", "Caméra", onFlip)
        CameraCircleButton(if (soundOn) "♪" else "×", "Son", onSound)
        CameraCircleButton("✦", "Effets", onClick = { })
        CameraCircleButton("T", "Texte", onClick = { })
        CameraCircleButton("⌁", "Galerie", onClick = { })
    }
}

@Composable
private fun CameraCaptureControls(
    modifier: Modifier,
    recording: Boolean,
    onCapture: () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (recording) "ENREGISTREMENT" else "PHOTO",
            color = Color.White.copy(.9f),
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Box(
            Modifier
                .size(if (recording) 86.dp else 78.dp)
                .background(Color.White, CircleShape)
                .clickable(onClick = onCapture),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(if (recording) 48.dp else 62.dp)
                    .background(
                        if (recording) Color(0xFF111111) else Color.Black,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun CameraCircleButton(
    glyph: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(44.dp)
            .background(Color(0x66000000), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, color = Color.White, fontSize = 19.sp)
    }
}

@Composable
private fun PermissionOverlay(onRequest: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color(0xF0000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Caméra FREEB", color = Color.White, fontSize = 28.sp)
            Text(
                "Active la caméra pour capturer des photos et vidéos.",
                color = Color.White.copy(.7f),
                fontSize = 14.sp
            )
            Box(
                Modifier.background(Color.White, CircleShape).clickable(onClick = onRequest).padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text("Activer la caméra", color = Color.Black, fontSize = 14.sp)
            }
        }
    }
}
