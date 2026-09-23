#include <vulkan/vulkan.h>

bool FreebVulkanLoaderAvailable() {
    auto enumerate = reinterpret_cast<PFN_vkEnumerateInstanceVersion>(
        vkGetInstanceProcAddr(VK_NULL_HANDLE, "vkEnumerateInstanceVersion")
    );

    if (!enumerate) {
        return true;
    }

    uint32_t version = VK_API_VERSION_1_0;
    return enumerate(&version) == VK_SUCCESS;
}
