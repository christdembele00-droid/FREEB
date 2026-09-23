package com.freeb.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.freeb.app.network.FreebApi

class FreebMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Thread {
            runCatching {
                FreebApi(com.freeb.app.BuildConfig.FREEB_API_URL).registerDevice(token)
            }
        }.start()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // App-specific notification display can be attached to the notification channel here.
    }
}
