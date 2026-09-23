import firebase_admin
from firebase_admin import auth, credentials

from app.core.config import settings


def initialize_firebase() -> None:
    if firebase_admin._apps:
        return

    if settings.firebase_configured:
        info = {
            "type": "service_account",
            "project_id": settings.firebase_project_id,
            "client_email": settings.firebase_client_email,
            "private_key": settings.firebase_private_key.replace("\\n", "\n"),
            "token_uri": "https://oauth2.googleapis.com/token",
        }
        firebase_admin.initialize_app(credentials.Certificate(info))
        return

    # Local development can use Application Default Credentials.
    # Production Render deployments should use explicit FIREBASE_* variables.
    firebase_admin.initialize_app(credentials.ApplicationDefault())


def verify_id_token(id_token: str) -> dict:
    if not id_token or not id_token.strip():
        raise ValueError("Empty Firebase ID token")
    initialize_firebase()
    return auth.verify_id_token(id_token.strip())


def extract_websocket_token(websocket) -> str:
    authorization = websocket.headers.get("authorization", "")
    if authorization.lower().startswith("bearer "):
        return authorization[7:].strip()
    return websocket.query_params.get("token", "").strip()


def origin_allowed(origin: str | None) -> bool:
    if not origin:
        return True
    return origin in settings.origin_list
