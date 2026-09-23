package com.freeb.app.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class FreebApi(
    private val baseUrl: String,
    private val authProvider: () -> FirebaseAuth? = {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    },
    private val client: OkHttpClient = OkHttpClient()
) {
    private fun idToken(): String {
        val auth = authProvider() ?: error("Firebase is not configured")
        val user = auth.currentUser ?: error("Authentication required")
        return Tasks.await(user.getIdToken(false)).token
            ?: error("Missing Firebase token")
    }

    private fun request(path: String, method: String = "GET", body: String? = null): String {
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Authorization", "Bearer " + idToken())

        val requestBody = body?.toRequestBody("application/json".toMediaType())
        when (method.uppercase()) {
            "POST" -> builder.post(requestBody ?: ByteArray(0).toRequestBody())
            "PATCH" -> builder.patch(requestBody ?: ByteArray(0).toRequestBody())
            "DELETE" -> builder.delete(requestBody)
            else -> builder.get()
        }

        client.newCall(builder.build()).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("HTTP " + response.code + ": " + payload)
            }
            return payload
        }
    }

    fun me(): JSONObject = JSONObject(request("/v1/users/me"))

    fun updateProfile(username: String?, displayName: String?): JSONObject {
        val body = JSONObject().apply {
            if (username != null) put("username", username)
            if (displayName != null) put("display_name", displayName)
        }
        return JSONObject(request("/v1/users/me", "PATCH", body.toString()))
    }

    fun searchUsers(query: String): JSONArray =
        JSONArray(
            request(
                "/v1/search/users?q=" + java.net.URLEncoder.encode(query, "UTF-8")
            )
        )

    fun createConversation(peerUserId: String): JSONObject =
        JSONObject(
            request(
                "/v1/chat/conversations",
                "POST",
                JSONArray(listOf(peerUserId)).toString()
            )
        )

    fun conversations(): JSONArray = JSONArray(request("/v1/chat/conversations"))

    fun messages(conversationId: String): JSONArray =
        JSONArray(
            request(
                "/v1/chat/conversations/" + conversationId + "/messages"
            )
        )

    fun sendMessage(conversationId: String, body: String): JSONObject =
        JSONObject(
            request(
                "/v1/chat/conversations/" + conversationId + "/messages",
                "POST",
                JSONObject().put("body", body).toString()
            )
        )

    fun createCall(peerUserId: String, kind: String): JSONObject =
        JSONObject(
            request(
                "/v1/calls",
                "POST",
                JSONObject()
                    .put("callee_id", peerUserId)
                    .put("kind", kind)
                    .toString()
            )
        )

    fun incomingCalls(): JSONArray = JSONArray(request("/v1/calls/incoming"))

    fun answerCall(callId: String): JSONObject =
        JSONObject(request("/v1/calls/" + callId + "/answer", "POST", "{}"))

    fun rejectCall(callId: String): JSONObject =
        JSONObject(request("/v1/calls/" + callId + "/reject", "POST", "{}"))

    fun hangupCall(callId: String): JSONObject =
        JSONObject(request("/v1/calls/" + callId + "/hangup", "POST", "{}"))

    fun uploadMedia(file: File, kind: String, folder: String): JSONObject {
        val mime = when (kind) {
            "IMAGE" -> "image/jpeg"
            "VIDEO" -> "video/mp4"
            "AUDIO" -> "audio/mp4"
            else -> "application/octet-stream"
        }.toMediaType()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("kind", kind)
            .addFormDataPart("folder", folder)
            .addFormDataPart("file", file.name, file.asRequestBody(mime))
            .build()

        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/v1/media/upload")
            .header("Authorization", "Bearer " + idToken())
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Upload failed: HTTP " + response.code + " " + payload)
            return JSONObject(payload)
        }
    }

    fun registerDevice(token: String, platform: String = "android") {
        val auth = authProvider() ?: return
        val user = auth.currentUser ?: return
        val tokenValue = Tasks.await(user.getIdToken(false)).token ?: return
        val body = JSONObject().apply {
            put("token", token)
            put("platform", platform)
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/v1/devices")
            .header("Authorization", "Bearer " + tokenValue)
            .post(body)
            .build()

        client.newCall(request).execute().use { }
    }
}
