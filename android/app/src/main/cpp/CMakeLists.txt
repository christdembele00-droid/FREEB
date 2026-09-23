cmake_minimum_required(VERSION 3.22.1)
project(freeb_native)

add_library(freeb_native SHARED
    freeb_native.cpp
    vulkan/VulkanContext.cpp
    vulkan/VulkanRenderer.cpp
)

target_compile_features(freeb_native PRIVATE cxx_std_20)

find_library(log-lib log)
find_library(android-lib android)
find_library(vulkan-lib vulkan)

target_link_libraries(
    freeb_native
    ${log-lib}
    ${android-lib}
    ${vulkan-lib}
)
