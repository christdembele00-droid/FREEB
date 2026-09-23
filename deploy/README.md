# FREEB deployment

## Cloud Run
Set these GitHub secrets:
- GCP_WORKLOAD_IDENTITY_PROVIDER
- GCP_SERVICE_ACCOUNT
- GCP_PROJECT_ID
- GCP_REGION
- CLOUD_RUN_SERVICE
- FREEB_DATABASE_URL

Backend runtime environment:
FIREBASE_PROJECT_ID
FIREBASE_CLIENT_EMAIL
FIREBASE_PRIVATE_KEY
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
ALLOWED_ORIGINS
AUTO_CREATE_DB=false

## Firebase
Place the real google-services.json at android/app/google-services.json locally. It is intentionally ignored by Git.
Enable the authentication provider selected by the product and FCM/App Check as required.

## Cloudinary
Use the connected Cloudinary environment. Keep API secret backend-only.
