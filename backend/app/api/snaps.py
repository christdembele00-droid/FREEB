from datetime import timedelta

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import FriendRequest, Friendship, MediaAsset, Snap, SnapRecipient, User, utc_now
from app.db.session import get_db

router = APIRouter(prefix="/snaps", tags=["snaps"])

@router.post("")
async def create_snap(
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    media_asset_id = str(payload.get("media_asset_id", "")).strip()
    recipient_ids = [str(x) for x in payload.get("recipient_ids", [])]

    asset = await db.get(MediaAsset, media_asset_id)
    if not asset or asset.user_id != user.id:
        raise HTTPException(404, "Media not found")
    if not recipient_ids:
        raise HTTPException(400, "No recipients")
    if len(recipient_ids) > 50:
        raise HTTPException(413, "Too many recipients")

    snap = Snap(
        sender_id=user.id,
        media_asset_id=asset.id,
        expires_at=utc_now() + timedelta(hours=24),
    )
    db.add(snap)
    await db.flush()

    for recipient_id in set(recipient_ids):
        if recipient_id == user.id:
            continue
        target = await db.get(User, recipient_id)
        if not target:
            continue

        a, b = (user.id, recipient_id) if user.id < recipient_id else (recipient_id, user.id)
        friendship = await db.get(Friendship, {"user_a": a, "user_b": b})
        if friendship:
            db.add(SnapRecipient(snap_id=snap.id, recipient_id=recipient_id))

    await db.commit()
    await db.refresh(snap)
    return {"id": snap.id, "expires_at": snap.expires_at}

@router.get("/inbox")
async def inbox(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Snap, MediaAsset, User, SnapRecipient)
        .join(SnapRecipient, SnapRecipient.snap_id == Snap.id)
        .join(MediaAsset, MediaAsset.id == Snap.media_asset_id)
        .join(User, User.id == Snap.sender_id)
        .where(
            SnapRecipient.recipient_id == user.id,
            Snap.expires_at > utc_now(),
            SnapRecipient.status.in_({"DELIVERED", "OPENED", "REPLAYED"}),
        )
        .order_by(Snap.created_at.desc())
        .limit(100)
    )

    return [
        {
            "id": snap.id,
            "sender_id": sender.id,
            "sender_display_name": sender.display_name,
            "media_url": asset.secure_url,
            "status": recipient.status,
            "opened_at": recipient.opened_at,
            "replayed_at": recipient.replayed_at,
            "created_at": snap.created_at,
        }
        for snap, asset, sender, recipient in result.all()
    ]

@router.post("/{snap_id}/open")
async def open_snap(
    snap_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(SnapRecipient).where(
            SnapRecipient.snap_id == snap_id,
            SnapRecipient.recipient_id == user.id,
        )
    )
    recipient = result.scalar_one_or_none()
    snap = await db.get(Snap, snap_id)

    if not recipient or not snap or snap.expires_at <= utc_now():
        raise HTTPException(404, "Snap not found")

    if recipient.status == "DELIVERED":
        recipient.status = "OPENED"
        recipient.opened_at = utc_now()
        await db.commit()

    return {"ok": True}
