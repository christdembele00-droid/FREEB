# FREEB on Koyeb

FREEB is a monorepo. The backend lives in `backend/` and is deployable as a Dockerfile Web Service.

Koyeb supports GitHub deployments, Dockerfile builds, monorepo work directories, runtime environment variables, custom HTTP health checks, and WebSocket connections. The current Koyeb Free Instance is a single 512 MB / 0.1 vCPU / 2 GB SSD Web Service and scales to zero after one hour without traffic. 

## Service

Create a Koyeb Web Service from GitHub:

- Repository: `christdembele00-droid/FREEB`
- Branch: `main`
- Builder: **Dockerfile**
- Work directory: `backend`
- Dockerfile: `Dockerfile`
- Exposed port: `8080`
- Protocol: **HTTP**
- Route: `/`
- Health check: **HTTP GET `/v1/health/ready` on port 8080**

Koyeb's monorepo behavior means the configured work directory becomes the build environment, so `backend/requirements.txt` and `backend/Dockerfile` are intentionally self-contained.

## Runtime variables

Set the following in Koyeb. Do not put real values in Git:

- `DATABASE_URL` — PostgreSQL connection string using the `asyncpg` SQLAlchemy driver and SSL.
- `FIREBASE_PROJECT_ID`
- `FIREBASE_CLIENT_EMAIL`
- `FIREBASE_PRIVATE_KEY`
- `CLOUDINARY_CLOUD_NAME`
- `CLOUDINARY_API_KEY`
- `CLOUDINARY_API_SECRET`
- `ALLOWED_ORIGINS` — comma-separated browser origins.
- `AUTO_CREATE_DB`
- `MAX_UPLOAD_BYTES`

For the first deployment against an empty PostgreSQL database, temporarily set `AUTO_CREATE_DB=true` so SQLAlchemy creates the current schema. Once migrations are introduced, set it back to `false`.

## Endpoints

- `GET /` — service liveness and endpoint metadata.
- `GET /v1/health` — dependency status.
- `GET /v1/health/ready` — PostgreSQL readiness probe.
- `WS /v1/ws/{conversation_id}` — authenticated conversation events.
- `WS /v1/ws/calls/{call_id}` — authenticated WebRTC signaling.

The WebSocket layer accepts a Firebase ID token through either an `Authorization: Bearer ...` header or the `token` query parameter. Browser clients normally use the query-parameter form because the standard browser WebSocket API does not expose arbitrary request headers.

## Database

PostgreSQL remains FREEB's source of truth. Do not use Koyeb's limited database offering as the primary messaging database.

The application uses a conservative async SQLAlchemy pool (2 connections, no overflow) so a small PostgreSQL plan is not flooded with idle connections.

## Important Koyeb behavior

Koyeb Free Instances scale to zero after one hour without traffic. An active WebSocket connection counts as held traffic, but users can still experience a cold start when the service has been idle. Design Android/Web clients to reconnect and retry safely.

The current WebSocket connection manager is in-process memory. Keep the first Koyeb service at a single instance. A future multi-instance deployment should replace that manager with a shared broker such as Redis/Valkey.

## Deployment checklist

1. Create the Koyeb Web Service from the FREEB GitHub repository.
2. Set the work directory to `backend`.
3. Configure port `8080` and readiness check `/v1/health/ready`.
4. Add PostgreSQL, Firebase Admin and Cloudinary environment variables.
5. Deploy and open the generated `.koyeb.app` HTTPS URL.
6. Verify `/` and `/v1/health`.
7. Put that real URL into the Android/web API configuration.
8. Test authentication, conversations, WebSocket events, media, FCM notifications, and call signaling on two devices.

Never hard-code a fake Koyeb URL into FREEB.
