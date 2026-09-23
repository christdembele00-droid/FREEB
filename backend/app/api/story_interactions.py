from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Story, User, utc_now
from app.db.session import get_db

router = APIRouter(prefix="/stories", tags=["story-interactions"])

class StoryViewRow:
    pass

@router.post("/{story_id}/view")
async def view_story(
    story_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    story = await db.get(Story, story_id)
    if not story or story.expires_at <= utc_now():
        raise HTTPException(404, "Story not found")
    # A dedicated story_views table can be migrated later without changing the API.
    return {"ok": True, "story_id": story_id, "viewer_id": user.id}

@router.get("/{story_id}")
async def story_detail(
    story_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    story = await db.get(Story, story_id)
    if not story or story.expires_at <= utc_now():
        raise HTTPException(404, "Story not found")
    return {
        "id": story.id,
        "user_id": story.user_id,
        "media_asset_id": story.media_asset_id,
        "created_at": story.created_at,
        "expires_at": story.expires_at,
    }
