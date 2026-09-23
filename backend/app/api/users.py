from fastapi import APIRouter, Depends
from app.auth.dependencies import get_current_user
from app.db.models import User

router = APIRouter(prefix="/users", tags=["users"])

@router.get("/me")
async def me(user: User = Depends(get_current_user)):
    return {
        "id": user.id,
        "firebase_uid": user.firebase_uid,
        "username": user.username,
        "display_name": user.display_name,
        "email": user.email,
        "avatar_asset_id": user.avatar_asset_id,
    }
