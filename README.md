# FREEB

FREEB is a native Android social-media platform architecture designed for Snapchat-class experiences while remaining independently implemented.

## Core stack

- Android: Kotlin, Jetpack Compose, CameraX/Camera2, Media3, C++/NDK, CMake, Vulkan
- Backend: Python, FastAPI, Pydantic, SQLAlchemy, Alembic, WebSocket
- Data: PostgreSQL
- Identity/Push: Firebase Authentication, FCM, App Check
- Media: Cloudinary
- Delivery: Docker, Cloud Run, GitHub Actions
- Quality: Crashlytics, Performance Monitoring, automated tests

## Architecture

Android UI -> Core -> REST/WebSocket -> FastAPI -> PostgreSQL
CameraX/Camera2 -> C++/Vulkan -> Media encoder -> Cloudinary
Firebase Authentication -> ID token -> FastAPI authorization
Firebase Cloud Messaging -> device notifications

## Principles

1. Vulkan owns GPU rendering and real-time effects, not business logic.
2. Cloudinary owns binary media delivery; PostgreSQL stores media metadata.
3. Firebase owns identity/push/mobile diagnostics; PostgreSQL remains the application source of truth.
4. Android never talks directly to PostgreSQL.
5. Secrets never ship in the APK.
6. The MVP keeps the backend modular instead of prematurely splitting into microservices.
