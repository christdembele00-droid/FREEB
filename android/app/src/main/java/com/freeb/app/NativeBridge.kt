package com.freeb.app

object NativeBridge {
    private val loaded: Boolean = runCatching {
        System.loadLibrary("freeb_native")
        true
    }.getOrDefault(false)

    fun isLoaded(): Boolean = loaded

    fun vulkanAvailable(): Boolean = loaded && nativeVulkanAvailable()

    fun vulkanApiVersion(): String =
        if (loaded) nativeVulkanApiVersion() else "UNAVAILABLE"

    private external fun nativeVulkanAvailable(): Boolean
    private external fun nativeVulkanApiVersion(): String
}
