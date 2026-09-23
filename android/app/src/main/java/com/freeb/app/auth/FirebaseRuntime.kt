package com.freeb.app.auth

import android.content.Context
import com.google.firebase.FirebaseApp

object FirebaseRuntime {
    fun initialize(context: Context): Boolean =
        runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)
}
