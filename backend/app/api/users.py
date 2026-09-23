from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select

from app.auth.dependencies import get_current_user
from app.db.models import User
from app.db.session import get_db

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

@router.patch("/me")
async def update_me(
    payload: dict,
    user: User = Depends(get_current_user),
    db = Depends(get_db),
):
    username = payload.get("username")
    display_name = payload.get("display_name")

    if username is not None:
        username = str(username).strip().lower()
        if not 3 <= len(username) <= 32:
            raise HTTPException(400, "Username must be 3-32 characters")
        if not username.replace("_", "").isalnum():
            raise HTTPException(400, "Username can use letters, numbers and underscores")
        clash = await db.execute(
            select(User.id).where(User.username == username, User.id != user.id)
        )
        if clash.scalar_one_or_none():
            raise HTTPException(409, "Username already used")
        user.username = username

    if display_name is not None:
        display_name = str(display_name).strip()
        if len(display_name) > 80:
            raise HTTPException(413, "Display name too long")
        user.display_name = display_name or None

    await db.commit()
    await db.refresh(user)
    return {
        "id": user.id,
        "username": user.username,
        "display_name": user.display_name,
        "email": user.email,
        "avatar_asset_id": user.avatar_asset_id,
    }
