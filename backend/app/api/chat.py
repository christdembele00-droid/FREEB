from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import desc, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Conversation, ConversationMember, Device, Message, User
from app.db.session import get_db
from app.services.push import send_push
from app.websocket.manager import manager

router = APIRouter(prefix="/chat", tags=["chat"])

@router.post("/conversations")
async def create_conversation(
    member_ids: list[str],
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    member_set = {str(x).strip() for x in member_ids if str(x).strip()} | {user.id}
    if len(member_set) < 2:
        raise HTTPException(400, "A conversation needs another user")
    if len(member_set) > 50:
        raise HTTPException(413, "Too many members")

    users = await db.execute(select(User.id).where(User.id.in_(member_set)))
    existing_ids = set(users.scalars())
    missing = member_set - existing_ids
    if missing:
        raise HTTPException(404, "One or more users do not exist")

    if len(member_set) == 2:
        existing = await db.execute(
            select(Conversation.id)
            .join(
                ConversationMember,
                ConversationMember.conversation_id == Conversation.id,
            )
            .where(
                ConversationMember.user_id.in_(member_set)
            )
            .group_by(Conversation.id)
            .having(
                func.count(ConversationMember.user_id) == 2
            )
            .limit(1)
        )
        conversation_id = existing.scalar_one_or_none()
        if conversation_id:
            return {"id": conversation_id, "member_ids": sorted(member_set)}

    conversation = Conversation()
    db.add(conversation)
    await db.flush()
    for member_id in member_set:
        db.add(
            ConversationMember(
                conversation_id=conversation.id,
                user_id=member_id,
            )
        )
    await db.commit()
    return {"id": conversation.id, "member_ids": sorted(member_set)}

@router.get("/conversations")
async def list_conversations(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    memberships = await db.execute(
        select(ConversationMember.conversation_id)
        .where(ConversationMember.user_id == user.id)
    )
    conversation_ids = list(memberships.scalars())
    if not conversation_ids:
        return []

    rows = []
    for conversation_id in conversation_ids:
        members_result = await db.execute(
            select(User)
            .join(
                ConversationMember,
                ConversationMember.user_id == User.id,
            )
            .where(
                ConversationMember.conversation_id == conversation_id,
                User.id != user.id,
            )
        )
        peer = members_result.scalars().first()

        latest = await db.execute(
            select(Message)
            .where(Message.conversation_id == conversation_id)
            .order_by(desc(Message.created_at))
            .limit(1)
        )
        message = latest.scalars().first()

        rows.append(
            {
                "id": conversation_id,
                "peer": (
                    {
                        "id": peer.id,
                        "username": peer.username,
                        "display_name": peer.display_name,
                        "avatar_asset_id": peer.avatar_asset_id,
                    }
                    if peer
                    else None
                ),
                "last_message": (
                    {
                        "body": message.body,
                        "sender_id": message.sender_id,
                        "created_at": message.created_at.isoformat(),
                    }
                    if message
                    else None
                ),
            }
        )

    rows.sort(
        key=lambda row: (row["last_message"]["created_at"] if row["last_message"] else ""),
        reverse=True,
    )
    return rows[:100]

@router.get("/conversations/{conversation_id}/messages")
async def messages(
    conversation_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    membership = await db.get(
        ConversationMember,
        {"conversation_id": conversation_id, "user_id": user.id},
    )
    if not membership:
        raise HTTPException(403, "Forbidden")
    result = await db.execute(
        select(Message)
        .where(Message.conversation_id == conversation_id)
        .order_by(Message.created_at.asc())
        .limit(200),
    )
    return [
        {
            "id": m.id,
            "sender_id": m.sender_id,
            "body": m.body,
            "created_at": m.created_at.isoformat(),
        }
        for m in result.scalars()
    ]

@router.post("/conversations/{conversation_id}/messages")
async def send_message(
    conversation_id: str,
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    membership = await db.get(
        ConversationMember,
        {"conversation_id": conversation_id, "user_id": user.id},
    )
    if not membership:
        raise HTTPException(403, "Forbidden")

    body = str(payload.get("body", "")).strip()
    if not body:
        raise HTTPException(400, "Empty message")
    if len(body) > 4000:
        raise HTTPException(413, "Message too long")

    message = Message(
        conversation_id=conversation_id,
        sender_id=user.id,
        body=body,
    )
    db.add(message)
    await db.commit()
    await db.refresh(message)

    recipient_result = await db.execute(
        select(Device.token)
        .join(ConversationMember, ConversationMember.user_id == Device.user_id)
        .where(
            ConversationMember.conversation_id == conversation_id,
            Device.active.is_(True),
            Device.user_id != user.id,
        )
    )
    send_push(
        list(recipient_result.scalars()),
        "FREEB",
        "Nouveau message",
        {"conversation_id": conversation_id},
    )

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
