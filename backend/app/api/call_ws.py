from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from sqlalchemy import select

from app.auth.firebase_auth import (
    extract_websocket_token,
    origin_allowed,
    verify_id_token,
)
from app.db.models import CallSession, User
from app.db.session import SessionLocal
from app.websocket.manager import manager

router = APIRouter(tags=["call-signaling"])


@router.websocket("/ws/calls/{call_id}")
async def call_signaling(websocket: WebSocket, call_id: str):
    if not origin_allowed(websocket.headers.get("origin")):
        await websocket.close(code=4403)
        return

    token = extract_websocket_token(websocket)
    if not token:
        await websocket.close(code=4401)
        return

    try:
        decoded = verify_id_token(token)
    except Exception:
        await websocket.close(code=4401)
        return

    async with SessionLocal() as db:
        user_result = await db.execute(
            select(User).where(User.firebase_uid == str(decoded["uid"]))
        )
        user = user_result.scalar_one_or_none()
        call = await db.get(CallSession, call_id)

        if not user or not call or user.id not in {call.caller_id, call.callee_id}:
            await websocket.close(code=4403)
            return

    room = f"call:{call_id}"
    await manager.connect(room, websocket)
    try:
        while True:
            payload = await websocket.receive_json()
            if not isinstance(payload, dict):
                continue
            if payload.get("event") in {
                "OFFER",
                "ANSWER",
                "ICE",
                "CALL_ACCEPTED",
                "CALL_ENDED",
                "CALL_REJECTED",
            }:
                await manager.broadcast(room, payload)
    except WebSocketDisconnect:
        manager.disconnect(room, websocket)
