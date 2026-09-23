# FREEB security baseline

- HTTPS only in production.
- Verify Firebase ID tokens server-side.
- Never embed Cloudinary API secrets or Firebase service-account private keys in the APK.
- Rate-limit auth, friend requests, messages and uploads.
- Validate media type, size, dimensions and duration.
- Keep private message content and tokens out of logs.
- Use Android Keystore-backed protection for local secrets.
- Support block, report, moderation and audit workflows.
