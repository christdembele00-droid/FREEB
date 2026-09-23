package com.freeb.app.calls

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class CallSignalingClient(
    private val baseUrl: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val http: OkHttpClient = OkHttpClient()
) {
    private var socket: WebSocket? = null

    fun connect(
        callId: String,
        onEvent: (String, JSONObject) -> Unit,
        onClosed: () -> Unit = {}
    ) {
        val token = auth.currentUser?.let {
            Tasks.await(it.getIdToken(false)).token
        } ?: error("Authentication required")

        val wsBase = baseUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
            .trimEnd('/')

        socket = http.newWebSocket(
            Request.Builder()
                .url("$wsBase/v1/ws/calls/$callId?token=$token")
                .build(),
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    runCatching {
                        val json = JSONObject(text)
                        onEvent(json.optString("event"), json.optJSONObject("payload") ?: JSONObject())
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    onClosed()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    onClosed()
                }
            }
        )
    }

    fun sendOffer(description: SessionDescription) {
        send("OFFER", JSONObject().put("type", description.type.canonicalForm()).put("sdp", description.description))
    }

    fun sendAnswer(description: SessionDescription) {
        send("ANSWER", JSONObject().put("type", description.type.canonicalForm()).put("sdp", description.description))
    }

    fun sendIce(candidate: IceCandidate) {
        send(
            "ICE",
            JSONObject()
                .put("sdpMid", candidate.sdpMid)
                .put("sdpMLineIndex", candidate.sdpMLineIndex)
                .put("candidate", candidate.sdp)
        )
    }

    fun hangup() {
        send("CALL_ENDED", JSONObject())
        socket?.close(1000, "hangup")
        socket = null
    }

    private fun send(event: String, payload: JSONObject) {
        socket?.send(JSONObject().put("event", event).put("payload", payload).toString())
    }
}
