from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Block, Report, User
from app.db.session import get_db

router = APIRouter(prefix="/moderation", tags=["moderation"])

@router.post("/block")
async def block(
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    blocked_id = str(payload.get("user_id", "")).strip()
    if not blocked_id or blocked_id == user.id:
        raise HTTPException(400, "Invalid user")
    target = await db.get(User, blocked_id)
    if not target:
        raise HTTPException(404, "User not found")
    existing = await db.get(Block, {"blocker_id": user.id, "blocked_id": blocked_id})
    if not existing:
        db.add(Block(blocker_id=user.id, blocked_id=blocked_id))
        await db.commit()
    return {"ok": True}

@router.delete("/block/{user_id}")
async def unblock(
    user_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    block_row = await db.get(Block, {"blocker_id": user.id, "blocked_id": user_id})
    if block_row:
        await db.delete(block_row)
        await db.commit()
    return {"ok": True}

@router.post("/report")
async def report(
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    target_type = str(payload.get("target_type", "")).strip().upper()
    target_id = str(payload.get("target_id", "")).strip()
    reason = str(payload.get("reason", "")).strip().upper()
    description = str(payload.get("description", "")).strip()[:2000]

    if not target_type or not target_id or not reason:
        raise HTTPException(400, "Incomplete report")

    row = Report(
        reporter_id=user.id,
        target_type=target_type,
        target_id=target_id,
        reason=reason,
        description=description or None,
    )
    db.add(row)
    await db.commit()
    await db.refresh(row)
    return {"id": row.id, "status": row.status}
