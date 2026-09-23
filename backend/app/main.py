from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.call_ws import router as call_ws_router
from app.api.friends import router as friends_router
from app.api.moderation import router as moderation_router
from app.api.snaps import router as snaps_router
from app.api.story_interactions import router as story_interactions_router
from app.api.calls import router as calls_router
from app.api.chat import router as chat_router
from app.api.devices import router as devices_router
from app.api.health import router as health_router
from app.api.media import router as media_router
from app.api.stories import router as stories_router
from app.api.users import router as users_router
from app.api.ws import router as ws_router
from app.core.config import settings
from app.db.init_db import init_db

@asynccontextmanager
async def lifespan(app: FastAPI):
    if settings.auto_create_db:
        await init_db()
    yield

app = FastAPI(title="FREEB API", version="0.3.0", lifespan=lifespan)
app.include_router(health_router, prefix="/v1")
app.include_router(users_router, prefix="/v1")
app.include_router(devices_router, prefix="/v1")
app.include_router(media_router, prefix="/v1")
app.include_router(stories_router, prefix="/v1")
app.include_router(chat_router, prefix="/v1")
app.include_router(calls_router, prefix="/v1")
app.include_router(call_ws_router, prefix="/v1")
app.include_router(friends_router, prefix="/v1")
app.include_router(moderation_router, prefix="/v1")
app.include_router(snaps_router, prefix="/v1")
app.include_router(story_interactions_router, prefix="/v1")
app.include_router(ws_router, prefix="/v1")

@app.get("/")
async def root():
    return {"service": "FREEB API", "status": "ok", "version": app.version}
