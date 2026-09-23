from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Conversation, ConversationMember, Message, User
from app.db.session import get_db
from app.websocket.manager import manager

router = APIRouter(prefix="/chat", tags=["chat"])

@router.post("/conversations")
async def create_conversation(
    member_ids: list[str],
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    member_set = set(member_ids) | {user.id}
    conversation = Conversation()
    db.add(conversation)
    await db.flush()
    for member_id in member_set:
        db.add(ConversationMember(conversation_id=conversation.id, user_id=member_id))
    await db.commit()
    return {"id": conversation.id, "member_ids": sorted(member_set)}

@router.get("/conversations/{conversation_id}/messages")
async def messages(
    conversation_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    membership = await db.get(ConversationMember, {"conversation_id": conversation_id, "user_id": user.id})
    if not membership:
        raise HTTPException(403, "Forbidden")
    result = await db.execute(
        select(Message)
        .where(Message.conversation_id == conversation_id)
        .order_by(Message.created_at.asc())
        .limit(200)
    )
    return [
        {"id": m.id, "sender_id": m.sender_id, "body": m.body, "created_at": m.created_at}
        for m in result.scalars()
    ]

@router.post("/conversations/{conversation_id}/messages")
async def send_message(
    conversation_id: str,
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    membership = await db.get(ConversationMember, {"conversation_id": conversation_id, "user_id": user.id})
    if not membership:
        raise HTTPException(403, "Forbidden")

    body = str(payload.get("body", "")).strip()
    if not body:
        raise HTTPException(400, "Empty message")

    message = Message(conversation_id=conversation_id, sender_id=user.id, body=body)
    db.add(message)
    await db.commit()
    await db.refresh(message)

    await manager.broadcast(
        conversation_id,
        {
            "event": "MESSAGE_NEW",
            "payload": {
                "id": message.id,
                "sender_id": message.sender_id,
                "body": message.body,
                "created_at": message.created_at.isoformat(),
            },
        },
    )
    return {"id": message.id}
