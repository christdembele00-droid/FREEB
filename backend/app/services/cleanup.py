from datetime import datetime, timezone

from sqlalchemy import delete

from app.db.models import Snap, SnapRecipient, Story
from app.db.session import SessionLocal

async def cleanup_expired() -> None:
    now = datetime.now(timezone.utc)
    async with SessionLocal() as db:
        await db.execute(delete(SnapRecipient).where(SnapRecipient.snap_id.in_(
            select(Snap.id).where(Snap.expires_at <= now)
        )))
        await db.execute(delete(Snap).where(Snap.expires_at <= now))
        await db.execute(delete(Story).where(Story.expires_at <= now))
        await db.commit()
