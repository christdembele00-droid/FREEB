from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Device, User
from app.db.session import get_db

router = APIRouter(prefix="/devices", tags=["devices"])

@router.post("")
async def register_device(
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    token = str(payload.get("token", "")).strip()
    platform = str(payload.get("platform", "android")).strip()
    if not token:
        return {"error": "missing_token"}

    result = await db.execute(select(Device).where(Device.token == token))
    device = result.scalar_one_or_none()
    if device is None:
        device = Device(user_id=user.id, token=token, platform=platform)
        db.add(device)
    else:
        device.user_id = user.id
        device.platform = platform
        device.active = True
    await db.commit()
    return {"ok": True}
