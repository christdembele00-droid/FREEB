# Firebase setup for FREEB

Create a dedicated Firebase project for FREEB and register the Android package:
com.freeb.app

Enable:
- Authentication
- Cloud Messaging (FCM)
- App Check / Play Integrity
- Crashlytics
- Analytics
- Performance Monitoring

Required local Android file (never commit):
android/app/google-services.json

Required backend secrets:
FIREBASE_PROJECT_ID
FIREBASE_CLIENT_EMAIL
FIREBASE_PRIVATE_KEY

Firebase is identity/mobile infrastructure. PostgreSQL remains FREEB's application source of truth.
