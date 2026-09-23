from fastapi import APIRouter, Depends, Query
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth.dependencies import get_current_user
from app.db.models import User
from app.db.session import get_db

router = APIRouter(prefix="/search", tags=["search"])

@router.get("/users")
async def search_users(
    q: str = Query(min_length=1, max_length=64),
    user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    term = q.strip().lower()
    if not term:
        return []

    result = await db.execute(
        select(User)
        .where(
            User.id != user.id,
            or_(
                User.username.ilike(f"%{term}%"),
                User.display_name.ilike(f"%{term}%"),
            ),
        )
        .order_by(User.username.asc().nullslast(), User.id.asc())
        .limit(30)
    )
    return [
        {
            "id": row.id,
            "username": row.username,
            "display_name": row.display_name,
            "avatar_asset_id": row.avatar_asset_id,
        }
        for row in result.scalars()
    ]
