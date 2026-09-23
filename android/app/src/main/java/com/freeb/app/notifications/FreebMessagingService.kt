package com.freeb.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.freeb.app.BuildConfig
import com.freeb.app.MainActivity
import com.freeb.app.network.FreebApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FreebMessagingService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "freeb_messages"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Thread {
            runCatching { FreebApi(BuildConfig.FREEB_API_URL).registerDevice(token) }
        }.start()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "FREEB"
        val body = message.notification?.body ?: "Nouvelle activité"
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            message.data["call_id"]?.let { putExtra("freeb_call_id", it) }
            message.data["conversation_id"]?.let { putExtra("freeb_conversation_id", it) }
        }

        val pending = PendingIntent.getActivity(
            this,
            (message.data["call_id"] ?: message.data["conversation_id"] ?: "freeb").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "FREEB",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Messages et appels FREEB"
                }
            )
        }

        manager.notify(
            (message.data["call_id"] ?: message.data["conversation_id"] ?: body).hashCode(),
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
    }
}
