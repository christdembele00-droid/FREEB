package com.freeb.app.chat

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

class ChatSocketClient(
    private val baseUrl: String,
    private val conversationId: String
) {
    private val client = OkHttpClient()
    private var socket: WebSocket? = null
    private val main = Handler(Looper.getMainLooper())

    fun connect(onMessage: (JSONObject) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val token = Tasks.await(user.getIdToken(false)).token ?: return
        val wsBase = baseUrl.replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://").trimEnd('/')

        socket = client.newWebSocket(
            Request.Builder()
                .url("$wsBase/v1/ws/$conversationId?token=$token")
                .build(),
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    runCatching { JSONObject(text) }.onSuccess {
                        main.post { onMessage(it) }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    main.post { onMessage(JSONObject().put("event", "SOCKET_ERROR").put("error", t.message.orEmpty())) }
                }
            }
        )
    }

    fun sendTyping(started: Boolean) {
        socket?.send(
            JSONObject()
                .put("event", if (started) "TYPING_START" else "TYPING_STOP")
                .put("payload", JSONObject())
                .toString()
        )
    }

    fun sendRead(messageId: String) {
        socket?.send(
            JSONObject()
                .put("event", "MESSAGE_READ")
                .put("payload", JSONObject().put("message_id", messageId))
                .toString()
        )
    }

    fun close() {
        socket?.close(1000, "close")
        socket = null
        client.dispatcher.executorService.shutdown()
    }
}
