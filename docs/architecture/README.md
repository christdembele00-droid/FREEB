# FREEB architecture

ANDROID
UI/UX -> Core -> REST/WebSocket
CameraX/Camera2 -> C++/JNI -> Vulkan -> display/capture
Media3/MediaCodec -> Cloudinary

BACKEND
FastAPI -> domain services -> PostgreSQL
FastAPI -> Firebase Admin SDK for token verification / FCM operations
FastAPI -> Cloudinary signed upload operations

RULES
- Android never connects directly to PostgreSQL.
- Vulkan owns GPU rendering/effects, not business logic.
- Cloudinary stores binary media; PostgreSQL stores metadata.
- Firebase provides identity and mobile services; PostgreSQL remains the application source of truth.
- Secrets remain outside the APK and Git.
