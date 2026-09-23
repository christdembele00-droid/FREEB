# Cloudinary setup for FREEB

Cloudinary owns binary media and CDN delivery.

Recommended folders:
freeb/dev/users
freeb/dev/avatars
freeb/dev/stories
freeb/dev/snaps
freeb/dev/chat
freeb/dev/spotlight
freeb/dev/thumbnails

Production mirrors the same structure under freeb/prod.

Use signed uploads from the backend for production. Never ship the API secret in Android.
