# FREEB on Koyeb

FREEB is a monorepo. The backend is in `backend/` and already contains a Dockerfile.

## Koyeb service

Create a **Web Service** from GitHub:

- Repository: `christdembele00-droid/FREEB`
- Branch: `main`
- Builder: **Dockerfile**
- Work directory: `backend`
- Dockerfile: `Dockerfile`
- Exposed port: `8080`
- Protocol: HTTP
- Route: `/`
- Health check: `/`
- Service name: `freeb-api`

Koyeb supports GitHub repositories and Dockerfile-based builds, including monorepos with a configurable work directory. See the official deployment documentation:
https://www.koyeb.com/docs/build-and-deploy/deploy-with-git

## Runtime variables

Set these in Koyeb. Never commit their real values:

- `DATABASE_URL`
- `FIREBASE_PROJECT_ID`
- `FIREBASE_CLIENT_EMAIL`
- `FIREBASE_PRIVATE_KEY`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`
- `ALLOWED_ORIGINS`
- `AUTO_CREATE_DB=false`
- `MAX_UPLOAD_BYTES=104857600`

The backend must use the PostgreSQL provider's SSL connection string in production.

## After deployment

1. Open the Koyeb service URL.
2. Confirm `/` returns the FREEB API health response.
3. Confirm `/v1/health` responds successfully.
4. Record the real HTTPS URL ending in `.koyeb.app`.
5. Use that URL for the Android API configuration.
6. Rebuild FREEB.
7. Test two separate phones: authentication, profile/search, messaging, WebSocket events, media, notifications, and call signaling.

Do not put a fake backend URL in the Android application.

## Important

Koyeb itself is not a PostgreSQL replacement for a production messaging database. Keep PostgreSQL as the source of truth and use Koyeb for the FastAPI service.
