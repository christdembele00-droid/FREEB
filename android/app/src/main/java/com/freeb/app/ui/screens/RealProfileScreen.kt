package com.freeb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freeb.app.BuildConfig
import com.freeb.app.network.FreebApi
import java.util.concurrent.Executors

@Composable
fun RealProfileScreen(onSettings: () -> Unit = {}) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val api = remember { FreebApi(BuildConfig.FREEB_API_URL) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        executor.submit {
            runCatching { api.me() }.onSuccess { me ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    username = me.optString("username")
                    displayName = me.optString("display_name")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { executor.shutdownNow() }
    }

    Column(
        Modifier.fillMaxSize().background(Color.Black).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profil FREEB", color = Color.White)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it.lowercase().filter { ch -> ch.isLetterOrDigit() || ch == '_' }.take(32) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nom d'utilisateur") },
            prefix = { Text("@") }
        )
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it.take(80) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nom affiché") }
        )
        Button(
            onClick = {
                executor.submit {
                    runCatching {
                        api.updateProfile(username, displayName)
                    }.onSuccess {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            status = "Profil enregistré"
                        }
                    }.onFailure {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            status = it.message ?: "Erreur"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, null)
            Text("Enregistrer")
        }
        if (status.isNotBlank()) Text(status, color = Color.White.copy(.65f))
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Paramètres")
        }
    }
}
