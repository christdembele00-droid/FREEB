from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import and_, or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Block, FriendRequest, Friendship, User
from app.db.session import get_db

router = APIRouter(prefix="/friends", tags=["friends"])

def pair(a: str, b: str) -> tuple[str, str]:
    return (a, b) if a < b else (b, a)

@router.post("/request")
async def request_friend(
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    target_id = str(payload.get("user_id", "")).strip()
    if not target_id or target_id == user.id:
        raise HTTPException(400, "Invalid friend target")

    target = await db.get(User, target_id)
    if not target:
        raise HTTPException(404, "User not found")

    blocked = await db.execute(
        select(Block).where(
            or_(
                and_(Block.blocker_id == user.id, Block.blocked_id == target_id),
                and_(Block.blocker_id == target_id, Block.blocked_id == user.id),
            )
        )
    )
    if blocked.scalar_one_or_none():
        raise HTTPException(403, "Friend request unavailable")

    a, b = pair(user.id, target_id)
    friendship = await db.get(Friendship, {"user_a": a, "user_b": b})
    if friendship:
        return {"status": "ALREADY_FRIENDS"}

    existing = await db.execute(
        select(FriendRequest).where(
            or_(
                and_(FriendRequest.requester_id == user.id, FriendRequest.receiver_id == target_id),
                and_(FriendRequest.requester_id == target_id, FriendRequest.receiver_id == user.id),
            ),
            FriendRequest.status == "PENDING",
        )
    )
    current = existing.scalars().first()
    if current:
        return {"status": "PENDING", "request_id": current.id}

    request = FriendRequest(requester_id=user.id, receiver_id=target_id)
    db.add(request)
    await db.commit()
    await db.refresh(request)
    return {"status": "PENDING", "request_id": request.id}

@router.post("/request/{request_id}/accept")
async def accept_friend_request(
    request_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    request = await db.get(FriendRequest, request_id)
    if not request or request.receiver_id != user.id:
        raise HTTPException(404, "Request not found")
    if request.status != "PENDING":
        raise HTTPException(409, "Request is not pending")

    a, b = pair(request.requester_id, request.receiver_id)
    db.add(Friendship(user_a=a, user_b=b))
    request.status = "ACCEPTED"
    request.responded_at = datetime.now(timezone.utc)
    await db.commit()
    return {"ok": True}

@router.post("/request/{request_id}/reject")
async def reject_friend_request(
    request_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    request = await db.get(FriendRequest, request_id)
    if not request or request.receiver_id != user.id:
        raise HTTPException(404, "Request not found")
    request.status = "REJECTED"
    request.responded_at = datetime.now(timezone.utc)
    await db.commit()
    return {"ok": True}

@router.get("")
async def list_friends(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(User)
        .join(Friendship, or_(Friendship.user_a == User.id, Friendship.user_b == User.id))
        .where(
            or_(
                Friendship.user_a == user.id,
                Friendship.user_b == user.id,
            ),
            User.id != user.id,
        )
        .order_by(User.username.asc().nullslast(), User.id.asc())
    )
    return [
        {
            "id": friend.id,
            "username": friend.username,
            "display_name": friend.display_name,
            "avatar_asset_id": friend.avatar_asset_id,
        }
        for friend in result.scalars().unique()
    ]

@router.delete("/{friend_id}")
async def remove_friend(
    friend_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    a, b = pair(user.id, friend_id)
    friendship = await db.get(Friendship, {"user_a": a, "user_b": b})
    if not friendship:
        raise HTTPException(404, "Friendship not found")
    await db.delete(friendship)
    await db.commit()
    return {"ok": True}
