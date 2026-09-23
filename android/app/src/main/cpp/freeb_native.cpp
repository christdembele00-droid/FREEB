#include <jni.h>
#include <cstdio>
#include <vulkan/vulkan.h>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_freeb_app_NativeBridge_nativeVulkanAvailable(JNIEnv*, jclass) {
    uint32_t count = 0;
    const VkResult result = vkEnumerateInstanceVersion(&count);
    return result == VK_SUCCESS;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_freeb_app_NativeBridge_nativeVulkanApiVersion(JNIEnv* env, jclass) {
    uint32_t version = VK_API_VERSION_1_0;
    if (vkEnumerateInstanceVersion(&version) != VK_SUCCESS) {
        return env->NewStringUTF("VK_1_0");
    }

    const int major = static_cast<int>(VK_VERSION_MAJOR(version));
    const int minor = static_cast<int>(VK_VERSION_MINOR(version));
    const int patch = static_cast<int>(VK_VERSION_PATCH(version));

    char buffer[32];
    std::snprintf(buffer, sizeof(buffer), "VK_%d_%d_%d", major, minor, patch);
    return env->NewStringUTF(buffer);
}
