from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from sqlalchemy import select

from app.auth.firebase_auth import verify_id_token
from app.db.models import ConversationMember, User
from app.db.session import SessionLocal
from app.websocket.manager import manager

router = APIRouter(tags=["websocket"])

@router.websocket("/ws/{conversation_id}")
async def websocket_endpoint(websocket: WebSocket, conversation_id: str):
    token = websocket.query_params.get("token")
    if not token:
        await websocket.close(code=4401)
        return

    try:
        decoded = verify_id_token(token)
    except Exception:
        await websocket.close(code=4401)
        return

    firebase_uid = str(decoded["uid"])
    async with SessionLocal() as db:
        user_result = await db.execute(select(User).where(User.firebase_uid == firebase_uid))
        user = user_result.scalar_one_or_none()
        if not user:
            await websocket.close(code=4403)
            return

        membership = await db.get(
            ConversationMember,
            {"conversation_id": conversation_id, "user_id": user.id},
        )
        if not membership:
            await websocket.close(code=4403)
            return

    await manager.connect(conversation_id, websocket)
    try:
        while True:
            payload = await websocket.receive_json()
            if not isinstance(payload, dict):
                continue
            event = payload.get("event")
            if event in {"TYPING_START", "TYPING_STOP", "MESSAGE_READ"}:
                await manager.broadcast(
                    conversation_id,
                    {"event": event, "payload": payload.get("payload", {})},
                )
    except WebSocketDisconnect:
        manager.disconnect(conversation_id, websocket)
