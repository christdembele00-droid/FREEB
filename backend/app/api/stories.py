from datetime import timedelta
from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import MediaAsset, Story, User, utc_now
from app.db.session import get_db

router = APIRouter(prefix="/stories", tags=["stories"])

@router.post("")
async def create_story(
    media_asset_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    asset = await db.get(MediaAsset, media_asset_id)
    if not asset or asset.user_id != user.id:
        return {"error": "media_not_owned"}

    story = Story(
        user_id=user.id,
        media_asset_id=asset.id,
        expires_at=utc_now() + timedelta(hours=24),
    )
    db.add(story)
    await db.commit()
    await db.refresh(story)
    return {"id": story.id, "expires_at": story.expires_at}

@router.get("/feed")
async def story_feed(
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(Story, MediaAsset)
        .join(MediaAsset, MediaAsset.id == Story.media_asset_id)
        .where(Story.expires_at > utc_now())
        .order_by(Story.created_at.desc())
        .limit(100)
    )
    return [
        {
            "id": story.id,
            "user_id": story.user_id,
            "media": {
                "url": asset.secure_url,
                "type": asset.resource_type,
                "format": asset.format,
            },
            "created_at": story.created_at,
            "expires_at": story.expires_at,
        }
        for story, asset in result.all()
    ]
