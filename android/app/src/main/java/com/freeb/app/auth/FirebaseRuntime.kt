package com.freeb.app.auth

import android.content.Context
import com.freeb.app.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseRuntime {
    fun initialize(context: Context): Boolean =
        runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                    .build()

                if (
                    BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
                    BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
                    BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() &&
                    BuildConfig.FIREBASE_SENDER_ID.isNotBlank()
                ) {
                    FirebaseApp.initializeApp(context, options)
                } else {
                    FirebaseApp.initializeApp(context)
                }
            }
            FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)
}
