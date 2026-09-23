package com.freeb.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.freeb.app.auth.FirebaseAuthManager
import com.freeb.app.auth.FirebaseRuntime
import com.freeb.app.network.FreebApi
import com.freeb.app.ui.FreebApp
import com.freeb.app.ui.FreebTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseReady = FirebaseRuntime.initialize(this)
        val authManager = if (firebaseReady) FirebaseAuthManager() else null

        setContent {
            FreebTheme {
                var cameraGranted by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }
                var audioGranted by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        cameraGranted =
                            result[Manifest.permission.CAMERA] == true ||
                                ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                        audioGranted =
                            result[Manifest.permission.RECORD_AUDIO] == true ||
                                ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                    }

                LaunchedEffect(firebaseReady) {
                    if (!cameraGranted || !audioGranted) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        )
                    }

                    if (firebaseReady && authManager != null) {
                        authManager.ensureSession(
                            onReady = {
                                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                    Thread {
                                        runCatching {
                                            FreebApi(BuildConfig.FREEB_API_URL)
                                                .registerDevice(token)
                                        }
                                    }.start()
                                }
                            }
                        )
                    }
                }

                FreebApp(
                    cameraPermissionGranted = cameraGranted,
                    requestCameraPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    }
                )
            }
        }
    }
}
