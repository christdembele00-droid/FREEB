from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from app.auth.firebase_auth import verify_id_token
from app.websocket.manager import manager

router = APIRouter(tags=["websocket"])

@router.websocket("/ws/{conversation_id}")
async def websocket_endpoint(websocket: WebSocket, conversation_id: str):
    token = websocket.query_params.get("token")
    if not token:
        await websocket.close(code=4401)
        return

    try:
        verify_id_token(token)
    except Exception:
        await websocket.close(code=4401)
        return

    await manager.connect(conversation_id, websocket)
    try:
        while True:
            payload = await websocket.receive_json()
            await manager.broadcast(conversation_id, payload)
    except WebSocketDisconnect:
        manager.disconnect(conversation_id, websocket)
