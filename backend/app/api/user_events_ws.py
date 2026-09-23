from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from sqlalchemy import select

from app.auth.firebase_auth import verify_id_token
from app.db.models import User
from app.db.session import SessionLocal
from app.websocket.manager import manager

router = APIRouter(tags=["user-events"])

@router.websocket("/ws/events/{user_id}")
async def user_events(websocket: WebSocket, user_id: str):
    token = websocket.query_params.get("token")
    if not token:
        await websocket.close(code=4401)
        return

    try:
        decoded = verify_id_token(token)
    except Exception:
        await websocket.close(code=4401)
        return

    async with SessionLocal() as db:
        result = await db.execute(
            select(User).where(User.firebase_uid == str(decoded["uid"]))
        )
        user = result.scalar_one_or_none()
        if not user or user.id != user_id:
            await websocket.close(code=4403)
            return

    room = f"user:{user_id}"
    await manager.connect(room, websocket)
    try:
        while True:
            await websocket.receive()
    except WebSocketDisconnect:
        manager.disconnect(room, websocket)
