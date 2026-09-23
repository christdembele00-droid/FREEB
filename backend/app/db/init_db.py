from app.db.base import Base
from app.db.models import Conversation, ConversationMember, Device, MediaAsset, Message, Story, User
from app.db.session import engine

async def init_db() -> None:
    async with engine.begin() as connection:
        await connection.run_sync(Base.metadata.create_all)
