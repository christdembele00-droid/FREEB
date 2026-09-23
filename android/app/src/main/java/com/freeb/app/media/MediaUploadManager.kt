package com.freeb.app.media

import com.freeb.app.network.FreebApi
import java.io.File
import java.util.concurrent.Executors

class MediaUploadManager(private val api: FreebApi) {
    private val executor = Executors.newFixedThreadPool(2)

    fun upload(
        media: CapturedMedia,
        folder: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        executor.execute {
            runCatching {
                api.uploadMedia(
                    media.file,
                    media.kind.name,
                    folder
                ).getString("secure_url")
            }.onSuccess(onSuccess).onFailure { error ->
                onError(error as? Exception ?: RuntimeException(error))
            }
        }
    }
}
