from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import Story, StoryReaction, StoryView, User, utc_now
from app.db.session import get_db

router = APIRouter(prefix="/stories", tags=["story-interactions"])

@router.post("/{story_id}/view")
async def view_story(
    story_id: str,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    story = await db.get(Story, story_id)
    if not story or story.expires_at <= utc_now():
        raise HTTPException(404, "Story not found")

    key = {"story_id": story_id, "viewer_id": user.id}
    existing = await db.get(StoryView, key)
    if not existing:
        db.add(StoryView(story_id=story_id, viewer_id=user.id))
        await db.commit()

    return {"ok": True, "story_id": story_id, "viewer_id": user.id}

@router.post("/{story_id}/reaction")
async def react_story(
    story_id: str,
    payload: dict,
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    story = await db.get(Story, story_id)
    if not story or story.expires_at <= utc_now():
        raise HTTPException(404, "Story not found")

    reaction = str(payload.get("reaction", "")).strip().lower()[:32]
    if not reaction:
        raise HTTPException(400, "Missing reaction")

    key = {"story_id": story_id, "reactor_id": user.id}
    existing = await db.get(StoryReaction, key)
    if existing:
        existing.reaction = reaction
    else:
        db.add(StoryReaction(story_id=story_id, reactor_id=user.id, reaction=reaction))

    await db.commit()
    return {"ok": True}

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
