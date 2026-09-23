from datetime import timedelta

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import CallSession, User, utc_now
from app.db.session import get_db
from app.websocket.manager import manager
from app.db.models import Device
from app.services.push import send_push

router = APIRouter(prefix="/calls", tags=["calls"])

@router.post("")
async def create_call(
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    callee_id = str(payload.get("callee_id", "")).strip()
    kind = str(payload.get("kind", "VIDEO")).upper()

    if callee_id == user.id:
        raise HTTPException(400, "Cannot call yourself")
    if kind not in {"VIDEO", "AUDIO"}:
        raise HTTPException(400, "Unsupported call type")

    callee = await db.get(User, callee_id)
    if not callee:
        raise HTTPException(404, "User not found")

    call = CallSession(
        caller_id=user.id,
        callee_id=callee.id,
        kind=kind,
        status="RINGING",
        expires_at=utc_now() + timedelta(minutes=2),
    )
    db.add(call)
    await db.commit()
    await db.refresh(call)

    device_result = await db.execute(select(Device.token).where(Device.user_id == callee.id, Device.active.is_(True)))
    send_push(list(device_result.scalars()), "FREEB", "Incoming call", {"call_id": call.id, "kind": kind})

    await manager.broadcast(
        f"call:{callee.id}",
        {
            "event": "CALL_INCOMING",
            "payload": {
                "call_id": call.id,
                "caller_id": user.id,
                "kind": kind,
            },
        },
    )
    return {"call_id": call.id, "kind": kind, "status": call.status}

@router.post("/{call_id}/answer")
async def answer_call(
    call_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    call = await db.get(CallSession, call_id)
    if not call or user.id not in {call.caller_id, call.callee_id}:
        raise HTTPException(404, "Call not found")
    if call.expires_at <= utc_now():
        call.status = "EXPIRED"
        await db.commit()
        raise HTTPException(410, "Call expired")

    call.status = "ACTIVE"
    await db.commit()
    await manager.broadcast(
        f"call:{call.id}",
        {"event": "CALL_ACCEPTED", "payload": {"call_id": call.id, "user_id": user.id}},
    )
    return {"ok": True}

@router.post("/{call_id}/hangup")
async def hangup_call(
    call_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    call = await db.get(CallSession, call_id)
    if not call or user.id not in {call.caller_id, call.callee_id}:
        raise HTTPException(404, "Call not found")

    call.status = "ENDED"
    await db.commit()
    await manager.broadcast(
        f"call:{call.id}",
        {"event": "CALL_ENDED", "payload": {"call_id": call.id, "user_id": user.id}},
    )
    return {"ok": True}
