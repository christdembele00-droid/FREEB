from fastapi import FastAPI

from app.api.chat import router as chat_router
from app.api.devices import router as devices_router
from app.api.health import router as health_router
from app.api.media import router as media_router
from app.api.stories import router as stories_router
from app.api.users import router as users_router
from app.api.ws import router as ws_router

app = FastAPI(title="FREEB API", version="0.2.0")
app.include_router(health_router, prefix="/v1")
app.include_router(users_router, prefix="/v1")
app.include_router(devices_router, prefix="/v1")
app.include_router(media_router, prefix="/v1")
app.include_router(stories_router, prefix="/v1")
app.include_router(chat_router, prefix="/v1")
app.include_router(ws_router, prefix="/v1")

@app.get("/")
async def root():
    return {"service": "FREEB API", "status": "ok", "version": app.version}
