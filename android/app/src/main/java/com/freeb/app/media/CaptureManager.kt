package com.freeb.app.media

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CaptureManager(private val context: Context) {
    private fun newFile(extension: String): File {
        val dir = File(context.cacheDir, "freeb/media").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
        return File(dir, "FREEB_\${stamp}.\${extension}")
    }

    fun capturePhoto(imageCapture: ImageCapture, onResult: (Result<File>) -> Unit) {
        val file = newFile("jpg")
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) { onResult(Result.failure(exc)) }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) { onResult(Result.success(file)) }
        })
    }

    fun startVideo(videoCapture: VideoCapture<Recorder>, audioEnabled: Boolean, onStarted: () -> Unit, onFinalized: (Result<File>) -> Unit): Recording {
        val file = newFile("mp4")
        var pending = videoCapture.output.prepareRecording(context, FileOutputOptions.Builder(file).build())
        if (audioEnabled) pending = pending.withAudioEnabled()
        return pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> onStarted()
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) onFinalized(Result.failure(IllegalStateException("Video recording failed: \${event.error}")))
                    else onFinalized(Result.success(file))
                }
            }
        }
    }
}
