#include <jni.h>
#include <cstdio>
#include <vulkan/vulkan.h>

static PFN_vkEnumerateInstanceVersion getEnumerateVersion() {
    return reinterpret_cast<PFN_vkEnumerateInstanceVersion>(
        vkGetInstanceProcAddr(VK_NULL_HANDLE, "vkEnumerateInstanceVersion")
    );
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_freeb_app_NativeBridge_nativeVulkanAvailable(JNIEnv*, jobject) {
    const auto enumerateVersion = getEnumerateVersion();
    if (!enumerateVersion) {
        return true;
    }

    uint32_t version = VK_API_VERSION_1_0;
    return enumerateVersion(&version) == VK_SUCCESS;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_freeb_app_NativeBridge_nativeVulkanApiVersion(JNIEnv* env, jobject) {
    uint32_t version = VK_API_VERSION_1_0;
    const auto enumerateVersion = getEnumerateVersion();

    if (enumerateVersion) {
        if (enumerateVersion(&version) != VK_SUCCESS) {
            version = VK_API_VERSION_1_0;
        }
    }

    char buffer[32];
    std::snprintf(
        buffer,
        sizeof(buffer),
        "VK_%d_%d_%d",
        static_cast<int>(VK_VERSION_MAJOR(version)),
        static_cast<int>(VK_VERSION_MINOR(version)),
        static_cast<int>(VK_VERSION_PATCH(version))
    );

    return env->NewStringUTF(buffer);
}
