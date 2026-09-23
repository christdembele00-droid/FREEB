import firebase_admin
from firebase_admin import auth, credentials

def initialize_firebase() -> None:
    if firebase_admin._apps:
        return
    credentials.initialize_app()

def verify_id_token(id_token: str) -> dict:
    initialize_firebase()
    return auth.verify_id_token(id_token)
