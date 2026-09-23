#include <vulkan/vulkan.h>

VkInstance CreateFreebVulkanInstance() {
    VkApplicationInfo appInfo{
        VK_STRUCTURE_TYPE_APPLICATION_INFO,
        nullptr,
        "FREEB",
        VK_MAKE_VERSION(0, 2, 0),
        "FREEB Renderer",
        VK_MAKE_VERSION(0, 2, 0),
        VK_API_VERSION_1_0,
    };

    VkInstanceCreateInfo createInfo{
        VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
        nullptr,
        0,
        &appInfo,
        0,
        nullptr,
        0,
        nullptr,
    };

    VkInstance instance = VK_NULL_HANDLE;
    if (vkCreateInstance(&createInfo, nullptr, &instance) != VK_SUCCESS) {
        return VK_NULL_HANDLE;
    }
    return instance;
}

void DestroyFreebVulkanInstance(VkInstance instance) {
    if (instance != VK_NULL_HANDLE) {
        vkDestroyInstance(instance, nullptr);
    }
}
