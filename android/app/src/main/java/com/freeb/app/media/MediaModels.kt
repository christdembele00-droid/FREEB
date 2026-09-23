package com.freeb.app.media

import java.io.File

enum class MediaKind {
    IMAGE,
    VIDEO,
    AUDIO
}

data class CapturedMedia(
    val file: File,
    val kind: MediaKind,
    val mimeType: String
)
