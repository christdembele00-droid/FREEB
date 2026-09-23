# Android architecture

Layers:
- presentation: Jetpack Compose, navigation, state holders
- domain: application use cases and business contracts
- data: repositories, local cache, API/WebSocket clients
- camera: CameraX/Camera2 integration
- media: capture, compression, MediaCodec/Media3, cache, upload queue
- native: JNI/C++ bridge
- vulkan: Vulkan device, swapchain, command buffers, pipelines, shaders

Feature modules:
onboarding, auth, camera, stories, snaps, chat, friends, discover, spotlight, profile, calls, settings, moderation.
