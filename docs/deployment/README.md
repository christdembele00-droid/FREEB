# Deployment

MVP:
- Local PostgreSQL via Docker Compose.
- Backend container compatible with Cloud Run.
- Android release via GitHub Actions.
- Cloudinary for media CDN.
- Firebase for Auth/FCM/diagnostics.

Production secrets are injected by the deployment platform; never committed.
