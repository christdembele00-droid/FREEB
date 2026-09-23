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
import com.google.firebase.messaging.FirebaseMessaging
import com.freeb.app.auth.FirebaseAuthManager
import com.freeb.app.network.FreebApi
import com.freeb.app.ui.FreebApp
import com.freeb.app.ui.FreebTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authManager = FirebaseAuthManager()

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
                        cameraGranted = result[Manifest.permission.CAMERA] == true ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        audioGranted = result[Manifest.permission.RECORD_AUDIO] == true ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    }

                LaunchedEffect(Unit) {
                    if (!cameraGranted || !audioGranted) {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    }
                    authManager.ensureSession(
                        onReady = {
                            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                Thread {
                                    runCatching {
                                        FreebApi(BuildConfig.FREEB_API_URL).registerDevice(token)
                                    }
                                }.start()
                            }
                        }
                    )
                }

                FreebApp(
                    cameraPermissionGranted = cameraGranted,
                    requestCameraPermission = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    }
                )
            }
        }
    }
}
