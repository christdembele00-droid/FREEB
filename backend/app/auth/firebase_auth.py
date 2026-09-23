import json
import os

import firebase_admin
from firebase_admin import auth, credentials

def initialize_firebase() -> None:
    if firebase_admin._apps:
        return

    project_id = os.getenv("FIREBASE_PROJECT_ID")
    client_email = os.getenv("FIREBASE_CLIENT_EMAIL")
    private_key = os.getenv("FIREBASE_PRIVATE_KEY")

    if project_id and client_email and private_key:
        info = {
            "type": "service_account",
            "project_id": project_id,
            "client_email": client_email,
            "private_key": private_key.replace("\\n", "\n"),
            "token_uri": "https://oauth2.googleapis.com/token",
        }
        firebase_admin.initialize_app(credentials.Certificate(info))
    else:
        firebase_admin.initialize_app(credentials.ApplicationDefault())

def verify_id_token(id_token: str) -> dict:
    initialize_firebase()
    return auth.verify_id_token(id_token)
