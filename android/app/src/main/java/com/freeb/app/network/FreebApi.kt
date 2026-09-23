package com.freeb.app.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class FreebApi(
    private val baseUrl: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val client: OkHttpClient = OkHttpClient.Builder().build()
) {
    fun uploadMedia(file: File, kind: String, folder: String): JSONObject {
        val user = auth.currentUser ?: error("Authentication required")
        val token = Tasks.await(user.getIdToken(false)).token ?: error("Missing Firebase token")

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
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val payload = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Upload failed: HTTP ${response.code} $payload")
            return JSONObject(payload)
        }
    }

    fun registerDevice(token: String, platform: String = "android") {
        val user = auth.currentUser ?: return
        val idToken = Tasks.await(user.getIdToken(false)).token ?: return
        val body = JSONObject().apply {
            put("token", token)
            put("platform", platform)
        }.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/v1/devices")
            .header("Authorization", "Bearer $idToken")
            .post(body)
            .build()
        client.newCall(request).execute().close()
    }
}
