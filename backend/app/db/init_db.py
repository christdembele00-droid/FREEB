from sqlalchemy import text

from app.db.base import Base
from app.db.models import (
    Block,
    CallSession,
    Conversation,
    ConversationMember,
    Device,
    FriendRequest,
    Friendship,
    MediaAsset,
    Message,
    Report,
    Snap,
    SnapRecipient,
    Story,
    StoryReaction,
    StoryView,
    User,
)
from app.db.session import engine


async def init_db() -> None:
    async with engine.begin() as connection:
        await connection.execute(text("CREATE SCHEMA IF NOT EXISTS freeb"))
        await connection.run_sync(Base.metadata.create_all)
