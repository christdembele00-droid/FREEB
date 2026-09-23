package com.freeb.app.calls

import android.os.Handler
import android.os.Looper
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class UserEventSocketClient(
    private val baseUrl: String
) {
    private val client = OkHttpClient()
    private val main = Handler(Looper.getMainLooper())
    private var socket: WebSocket? = null

    fun connect(userId: String, onEvent: (String, JSONObject) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val token = Tasks.await(user.getIdToken(false)).token ?: return
        val wsBase = baseUrl.replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
            .trimEnd('/')

        socket = client.newWebSocket(
            Request.Builder()
                .url("$wsBase/v1/ws/events/$userId?token=$token")
                .build(),
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    runCatching { JSONObject(text) }.onSuccess { payload ->
                        main.post {
                            onEvent(
                                payload.optString("event"),
                                payload.optJSONObject("payload") ?: JSONObject()
                            )
                        }
                    }
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = Unit
            }
        )
    }

    fun close() {
        socket?.close(1000, "close")
        socket = null
        client.dispatcher.executorService.shutdown()
    }
}
