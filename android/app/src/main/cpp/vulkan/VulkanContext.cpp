#include <vulkan/vulkan.h>

bool FreebVulkanLoaderAvailable() {
    uint32_t count = 0;
    return vkEnumerateInstanceVersion(&count) == VK_SUCCESS;
}
