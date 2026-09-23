package com.freeb.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.freeb.app.BuildConfig
import com.freeb.app.network.FreebApi

class FreebMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Thread { runCatching { FreebApi(BuildConfig.FREEB_API_URL).registerDevice(token) } }.start()
    }
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}
